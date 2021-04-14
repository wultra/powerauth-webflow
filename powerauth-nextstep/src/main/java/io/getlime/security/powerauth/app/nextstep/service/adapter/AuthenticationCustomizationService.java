/*
 * Copyright 2021 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.app.nextstep.service.adapter;

import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.converter.UserAccountStatusConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsAuthorizationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.UserAuthenticationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserAuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifySmsAndPasswordResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifySmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.response.CombinedAuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.CredentialAuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.OtpAuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service handles customization of authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthenticationCustomizationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationCustomizationService.class);

    private final DataAdapterClient dataAdapterClient;

    private final OperationConverter operationConverter = new OperationConverter();
    private final UserAccountStatusConverter statusConverter = new UserAccountStatusConverter();

    /**
     * Authentication customization service constructor.
     * @param dataAdapterClient Data Adapter client.
     */
    public AuthenticationCustomizationService(DataAdapterClient dataAdapterClient) {
        this.dataAdapterClient = dataAdapterClient;
    }

    /**
     * Authenticate with a credential using Data Adapter.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param credentialValue Credential value.
     * @param operation Operation entity.
     * @return Credential authentication response.
     */
    public CredentialAuthenticationResponse authenticateWithCredential(String userId, String organizationId, String credentialValue, OperationEntity operation,
                                                                       AuthenticationContext authenticationContext) {
        final OperationContext operationContext = operationConverter.toOperationContext(operation);
        try {
            final UserAuthenticationResponse authResponse = dataAdapterClient.authenticateUser(userId, organizationId, credentialValue, authenticationContext, operationContext).getResponseObject();
            final CredentialAuthenticationResponse response = new CredentialAuthenticationResponse();
            response.setUserId(userId);
            response.setUserIdentityStatus(UserIdentityStatus.ACTIVE);
            if (authResponse.getAuthenticationResult() == UserAuthenticationResult.SUCCEEDED) {
                response.setAuthenticationResult(AuthenticationResult.SUCCEEDED);
            } else {
                response.setAuthenticationResult(AuthenticationResult.FAILED);
                response.setErrorMessage(authResponse.getErrorMessage());
                if (authResponse.getAccountStatus() != null && authResponse.getAccountStatus() != AccountStatus.ACTIVE) {
                    response.setUserIdentityStatus(UserIdentityStatus.BLOCKED);
                }
            }
            response.setRemainingAttempts(authResponse.getRemainingAttempts());
            response.setShowRemainingAttempts(authResponse.getShowRemainingAttempts());
            return response;
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
            final CredentialAuthenticationResponse response = new CredentialAuthenticationResponse();
            response.setUserId(userId);
            response.setAuthenticationResult(AuthenticationResult.FAILED);
            return response;
        }
    }

    /**
     * Authenticate with an OTP using Data Adapter.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operation Operation.
     * @return OTP authentication response.
     */
    public OtpAuthenticationResponse authenticateWithOtp(String otpId, String otpValue, String userId, String organizationId, OperationEntity operation) {
        final OperationContext operationContext = operationConverter.toOperationContext(operation);
        final AccountStatus accountStatus = statusConverter.fromUserAccountStatus(operation.getUserAccountStatus());
        try {
            final VerifySmsAuthorizationResponse authResponse = dataAdapterClient.verifyAuthorizationSms(otpId, otpValue, userId, organizationId, accountStatus, operationContext).getResponseObject();
            final OtpAuthenticationResponse response = new OtpAuthenticationResponse();
            response.setUserId(userId);
            response.setUserIdentityStatus(UserIdentityStatus.ACTIVE);
            if (authResponse.getSmsAuthorizationResult() == SmsAuthorizationResult.SUCCEEDED) {
                response.setAuthenticationResult(AuthenticationResult.SUCCEEDED);
            } else {
                response.setAuthenticationResult(AuthenticationResult.FAILED);
                response.setErrorMessage(authResponse.getErrorMessage());
            }
            response.setRemainingAttempts(authResponse.getRemainingAttempts());
            response.setShowRemainingAttempts(authResponse.getShowRemainingAttempts());
            return response;
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
            final OtpAuthenticationResponse response = new OtpAuthenticationResponse();
            response.setUserId(userId);
            response.setAuthenticationResult(AuthenticationResult.FAILED);
            return response;
        }
    }

    /**
     * Authenticate using OTP and credential using Data Adapter.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param credentialValue Credential value.
     * @param operation Operation.
     * @return Combined authentication response.
     */
    public CombinedAuthenticationResponse authenticateCombined(String otpId, String otpValue, String userId, String organizationId, String credentialValue, OperationEntity operation,
                                                               AuthenticationContext authenticationContext) {
        final OperationContext operationContext = operationConverter.toOperationContext(operation);
        final AccountStatus accountStatus = statusConverter.fromUserAccountStatus(operation.getUserAccountStatus());
        try {
            final VerifySmsAndPasswordResponse authResponse = dataAdapterClient.verifyAuthorizationSmsAndPassword(otpId, otpValue, userId, organizationId, accountStatus, credentialValue, authenticationContext, operationContext).getResponseObject();
            final CombinedAuthenticationResponse response = new CombinedAuthenticationResponse();
            response.setUserId(userId);
            response.setUserIdentityStatus(UserIdentityStatus.ACTIVE);
            if (authResponse.getUserAuthenticationResult() == UserAuthenticationResult.SUCCEEDED) {
                response.setCredentialAuthenticationResult(AuthenticationResult.SUCCEEDED);
            } else {
                response.setCredentialAuthenticationResult(AuthenticationResult.FAILED);
            }
            if (authResponse.getSmsAuthorizationResult() == SmsAuthorizationResult.SUCCEEDED) {
                response.setOtpAuthenticationResult(AuthenticationResult.SUCCEEDED);
            } else {
                response.setOtpAuthenticationResult(AuthenticationResult.FAILED);
            }
            if (authResponse.getUserAuthenticationResult() == UserAuthenticationResult.SUCCEEDED && authResponse.getSmsAuthorizationResult() == SmsAuthorizationResult.SUCCEEDED) {
                response.setAuthenticationResult(AuthenticationResult.SUCCEEDED);
            } else {
                response.setAuthenticationResult(AuthenticationResult.FAILED);
            }
            if (authResponse.getAccountStatus() != null && authResponse.getAccountStatus() != AccountStatus.ACTIVE) {
                response.setUserIdentityStatus(UserIdentityStatus.BLOCKED);
            }
            response.setErrorMessage(authResponse.getErrorMessage());
            response.setRemainingAttempts(authResponse.getRemainingAttempts());
            response.setShowRemainingAttempts(authResponse.getShowRemainingAttempts());
            return response;
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
            final CombinedAuthenticationResponse response = new CombinedAuthenticationResponse();
            response.setUserId(userId);
            response.setAuthenticationResult(AuthenticationResult.FAILED);
            response.setOtpAuthenticationResult(AuthenticationResult.FAILED);
            response.setCredentialAuthenticationResult(AuthenticationResult.FAILED);
            return response;
        }
    }

}
