/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.UserAccountStatusConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.CertificateAuthenticationMode;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.CertificateVerificationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.response.InitAuthMethodResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifyCertificateResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.*;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.request.ApprovalScaAuthRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.request.ApprovalScaInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.response.ApprovalScaAuthResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.response.ApprovalScaInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.CertificateVerificationRepository;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.CertificateVerificationEntity;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

/**
 * Controller for initialization of SCA approval.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/approval-sca")
public class ApprovalScaController extends AuthMethodController<ApprovalScaAuthRequest, ApprovalScaAuthResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalScaController.class);

    private final String FIELD_BANK_ACCOUNT_CHOICE_DISABLED = "operation.bankAccountChoice.disabled";

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final AuthenticationManagementService authenticationManagementService;
    private final CertificateVerificationRepository certificateVerificationRepository;
    private final HttpSession httpSession;

    private final UserAccountStatusConverter statusConverter = new UserAccountStatusConverter();

    /**
     * Controller constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param nextStepClient Next Step client.
     * @param authMethodQueryService Service for querying authentication methods.
     * @param authenticationManagementService Authentication management service.
     * @param certificateVerificationRepository Certificate verification repository.
     * @param httpSession HTTP session.
     */
    @Autowired
    public ApprovalScaController(DataAdapterClient dataAdapterClient, NextStepClient nextStepClient, AuthMethodQueryService authMethodQueryService, AuthenticationManagementService authenticationManagementService, CertificateVerificationRepository certificateVerificationRepository, HttpSession httpSession) {
        this.dataAdapterClient = dataAdapterClient;
        this.nextStepClient = nextStepClient;
        this.authMethodQueryService = authMethodQueryService;
        this.authenticationManagementService = authenticationManagementService;
        this.certificateVerificationRepository = certificateVerificationRepository;
        this.httpSession = httpSession;
    }

    /**
     * Authenticate SCA approval.
     * @param request Authentication request.
     * @return SCA approval authentication response.
     * @throws AuthStepException In case SCA approval authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ApprovalScaAuthResponse authenticateScaApproval(@RequestBody ApprovalScaAuthRequest request) throws AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        String userId = operation.getUserId();
        if (userId == null) {
            // At this point user ID must be known, method cannot continue
            throw new InvalidRequestException("User ID is missing");
        }
        String organizationId = operation.getOrganizationId();

        if (isCertificateUsedForAuthentication(operation.getOperationId())) {
            String clientCertificate = getClientCertificateFromHttpSession();
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            AccountStatus accountStatus = statusConverter.fromUserAccountStatus(operation.getAccountStatus());
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formData, applicationContext);
            try {
                boolean userAuthenticatedUsingCertificate = verifyClientCertificate(operation.getOperationId(), userId, organizationId, clientCertificate, accountStatus, operationContext);
                if (userAuthenticatedUsingCertificate) {
                    logger.info("Step authentication succeeded with client certificate, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                }
            } catch (NextStepClientException | DataAdapterClientErrorException ex) {
                logger.error(ex.getMessage(), ex);
                // Send error to client
                ApprovalScaAuthResponse response = new ApprovalScaAuthResponse();
                response.setResult(AuthStepResult.AUTH_FAILED);
                if (ex instanceof DataAdapterClientErrorException) {
                    DataAdapterClientErrorException ex2 = (DataAdapterClientErrorException) ex;
                    response.setRemainingAttempts(ex2.getError().getRemainingAttempts());
                    response.setMessage(ex2.getError().getMessage());
                } else {
                    response.setMessage("error.communication");
                }
                return response;
            }
        }

        ApprovalScaAuthResponse response = new ApprovalScaAuthResponse();

        boolean mobileTokenEnabled = false;
        try {
            // Disable bank account choice
            operation.getFormData().getUserInput().put(FIELD_BANK_ACCOUNT_CHOICE_DISABLED, "true");
            nextStepClient.updateOperationFormData(operation.getOperationId(), operation.getFormData());

            // Upgrade operation to SCA
            authenticationManagementService.upgradeToStrongCustomerAuthentication();

            // Find out whether mobile token is enabled
            if (authMethodQueryService.isMobileTokenAvailable(userId, operation.getOperationId())) {
                nextStepClient.updateMobileToken(operation.getOperationId(), true);
                mobileTokenEnabled = true;
            }
        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
        }
        response.setMobileTokenEnabled(mobileTokenEnabled);
        if (mobileTokenEnabled) {
            response.setResult(AuthStepResult.CONFIRMED);
            logger.debug("Step authentication succeeded with mobile token, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } else {
            response.setResult(AuthStepResult.CONFIRMED);
            logger.debug("Step authentication succeeded with SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Initialize SCA approval.
     * @param request SCA approval initialization request.
     * @return SCA approval initialization response.
     * @throws AuthStepException In case SCA approval initialization fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ApprovalScaInitResponse initScaApproval(@RequestBody ApprovalScaInitRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        try {
            logger.info("Step init started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            String userId = operation.getUserId();
            if (userId == null) {
                // At this point user ID must be known, method cannot continue
                throw new InvalidRequestException("User ID is missing");
            }

            // Set chosen authentication method to APPROVAL_SCA
            nextStepClient.updateChosenAuthMethod(operation.getOperationId(), AuthMethod.APPROVAL_SCA);

            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formData, applicationContext);
            ObjectResponse<InitAuthMethodResponse> objectResponse = dataAdapterClient.initAuthMethod(operation.getUserId(), operation.getOrganizationId(), AuthMethod.LOGIN_SCA, operationContext);
            InitAuthMethodResponse initResponse = objectResponse.getResponseObject();
            // In case client TLS certificate was used during SCA login, use the client TLS certificate for authentication during payment
            if (initResponse.getCertificateAuthenticationMode() == CertificateAuthenticationMode.ENABLED
                    && isCertificateUsedForAuthentication(operation.getOperationId())) {
                String certificateVerificationUrl = initResponse.getCertificateVerificationUrl();
                logger.debug("Step init succeeded with client certificate, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                return new ApprovalScaInitResponse(true, certificateVerificationUrl);
            }

        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
            throw new CommunicationFailedException("Communication with Next Step service failed");
        } catch (DataAdapterClientErrorException ex) {
            logger.error("Error occurred in Data Adapter", ex);
            throw new CommunicationFailedException("Communication with Data Adapter service failed");
        }

        logger.debug("Step init succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        return new ApprovalScaInitResponse();
    }


    /**
     * Get current authentication method name.
     * @return Current authentication method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.APPROVAL_SCA;
    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public AuthStepResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null, true);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (CommunicationFailedException ex) {
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Whether client TLS certificate is used for authentication.
     * @param operationId Operation ID.
     * @return Whether client TLS certificate is used for authentication.
     */
    private boolean isCertificateUsedForAuthentication(String operationId) {
        CertificateVerificationEntity.CertificateVerificationKey key = new CertificateVerificationEntity.CertificateVerificationKey(operationId, AuthMethod.LOGIN_SCA);
        return certificateVerificationRepository.findByCertificateVerificationKey(key).isPresent();
    }

    /**
     * Get client TLS certificate from HTTP session.
     * @return Client certificate.
     */
    private String getClientCertificateFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (String) httpSession.getAttribute(HttpSessionAttributeNames.CLIENT_CERTIFICATE);
        }
    }

    /**
     * Authenticate client TLS certificate using Data Adapter.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param clientCertificate Client TLS certificate.
     * @param accountStatus Account status.
     * @param operationContext Operation context.
     * @return Whether authentication using client TLS certificate succeeded.
     * @throws DataAdapterClientErrorException In case communication with Data Adapter fails.
     * @throws NextStepClientException In case communication with Next Step service fails.
     * @throws AuthStepException In case step authentication fails.
     */
    private boolean verifyClientCertificate(String operationId, String userId, String organizationId, String clientCertificate, AccountStatus accountStatus, OperationContext operationContext) throws DataAdapterClientErrorException, NextStepClientException, AuthStepException {
        ObjectResponse<VerifyCertificateResponse> objectResponseCert = dataAdapterClient.verifyClientCertificate(userId, organizationId, clientCertificate, getAuthMethodName(), accountStatus, operationContext);
        VerifyCertificateResponse certResponse = objectResponseCert.getResponseObject();
        CertificateVerificationResult verificationResult = certResponse.getCertificateVerificationResult();
        if (verificationResult == CertificateVerificationResult.SUCCEEDED) {
            return true;
        }
        logger.debug("Step authentication failed with client certificate, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
        List<AuthInstrument> authInstruments = Collections.singletonList(AuthInstrument.CLIENT_CERTIFICATE);
        AuthOperationResponse response = failAuthorization(operationId, userId, authInstruments, null);
        if (response.getAuthResult() == AuthResult.FAILED) {
            // FAILED result instead of CONTINUE means the authentication method is failed
            throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
        }
        Integer remainingAttemptsDA = certResponse.getRemainingAttempts();
        boolean showRemainingAttempts = certResponse.getShowRemainingAttempts();
        UserAccountStatus userAccountStatus = statusConverter.fromAccountStatus(certResponse.getAccountStatus());

        String errorMessage = "login.authenticationFailed";
        if (certResponse.getErrorMessage() != null) {
            errorMessage = certResponse.getErrorMessage();
        }

        AuthenticationFailedException authEx = new AuthenticationFailedException("Authentication failed", errorMessage);
        if (showRemainingAttempts) {
            GetOperationDetailResponse updatedOperation = getOperation();
            Integer remainingAttemptsNS = updatedOperation.getRemainingAttempts();
            Integer remainingAttempts = resolveRemainingAttempts(remainingAttemptsDA, remainingAttemptsNS);
            authEx.setRemainingAttempts(remainingAttempts);
        }
        authEx.setAccountStatus(userAccountStatus);
        throw authEx;
    }

}
