/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.springframework.beans.factory.annotation.Autowired;
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
    private final OperationConverter operationConverter;

    private final UserAccountStatusConverter statusConverter = new UserAccountStatusConverter();

    /**
     * Authentication customization service constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param operationConverter Operation converter.
     */
    @Autowired
    public AuthenticationCustomizationService(DataAdapterClient dataAdapterClient, OperationConverter operationConverter) {
        this.dataAdapterClient = dataAdapterClient;
        this.operationConverter = operationConverter;
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
