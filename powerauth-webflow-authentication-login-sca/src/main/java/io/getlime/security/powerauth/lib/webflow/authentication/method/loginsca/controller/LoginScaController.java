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

package io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.CertificateVerificationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifyCertificateResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.request.LoginScaAuthRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.request.LoginScaInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.response.LoginScaAuthResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.response.LoginScaInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.model.OrganizationDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.OrganizationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.UserAccountStatusConverter;
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
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;


/**
 * Controller for initialization of SCA login.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/login-sca")
public class LoginScaController extends AuthMethodController<LoginScaAuthRequest, LoginScaAuthResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(LoginScaController.class);

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final AuthenticationManagementService authenticationManagementService;
    private final HttpSession httpSession;
    private final WebFlowServicesConfiguration config;

    private final OrganizationConverter organizationConverter = new OrganizationConverter();
    private final UserAccountStatusConverter userAccountStatusConverter = new UserAccountStatusConverter();

    /**
     * Controller constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param nextStepClient Next Step client.
     * @param authMethodQueryService Service for querying authentication methods.
     * @param authenticationManagementService Authentication management service.
     * @param httpSession HTTP session.
     * @param config Web Flow services configuration.
     */
    @Autowired
    public LoginScaController(DataAdapterClient dataAdapterClient, NextStepClient nextStepClient, AuthMethodQueryService authMethodQueryService, AuthenticationManagementService authenticationManagementService, HttpSession httpSession, WebFlowServicesConfiguration config) {
        this.dataAdapterClient = dataAdapterClient;
        this.nextStepClient = nextStepClient;
        this.authMethodQueryService = authMethodQueryService;
        this.authenticationManagementService = authenticationManagementService;
        this.httpSession = httpSession;
        this.config = config;
    }

    /**
     * Initialize SCA login for given username.
     * @param request Initialization request.
     * @return SCA login initialization response.
     * @throws AuthStepException In case SCA login initialization fails.
     * @throws NextStepServiceException In case communication with Next Step service fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public LoginScaAuthResponse authenticateScaLogin(@Valid @RequestBody LoginScaAuthRequest request) throws AuthStepException, NextStepServiceException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData, applicationContext);
            String userId = operation.getUserId();
            String organizationId = request.getOrganizationId();
            AccountStatus accountStatus = userAccountStatusConverter.fromUserAccountStatus(operation.getAccountStatus());
            boolean userIdAlreadyAvailable;
            boolean userAuthenticatedUsingCertificate = false;
            if (userId == null) {
                // First time invocation, user ID is not available yet
                userIdAlreadyAvailable = false;
                String clientCertificate = getClientCertificateFromHttpSession();
                String username = request.getUsername();
                // Check that either certificate or username is available
                if (clientCertificate == null && username == null) {
                    logger.warn("Both username and client certificate are unknown");
                    LoginScaAuthResponse response = new LoginScaAuthResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage("login.userNotFound");
                    return response;
                }
                ObjectResponse<UserDetailResponse> objectResponse = dataAdapterClient.lookupUser(username, organizationId, clientCertificate, operationContext);
                UserDetailResponse userDetailResponse = objectResponse.getResponseObject();
                userId = userDetailResponse.getId();
                accountStatus = userDetailResponse.getAccountStatus();
                if (clientCertificate != null) {
                    userAuthenticatedUsingCertificate = verifyClientCertificate(operation.getOperationId(), userId, organizationId, clientCertificate, accountStatus, operationContext);
                } else {
                    updateUsernameInHttpSession(username);
                }
            } else {
                // User ID is already set, this can happen when the user refreshes the page or another authentication method set the user ID
                userIdAlreadyAvailable = true;
            }
            LoginScaAuthResponse response = new LoginScaAuthResponse();
            if (!userIdAlreadyAvailable && userId != null) {
                // User ID lookup succeeded, update user ID in operation so that Push Server can deliver the personal push message
                authenticationManagementService.updateAuthenticationWithUserDetails(userId, organizationId);
                authenticationManagementService.upgradeToStrongCustomerAuthentication();
                nextStepClient.updateOperationUser(operation.getOperationId(), userId, organizationId, userAccountStatusConverter.fromAccountStatus(accountStatus));
            }
            if (userAuthenticatedUsingCertificate) {
                logger.debug("Step authentication succeeded with client certificate, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                return authenticateStepUsingClientCertificate(operation.getOperationId(), userId, organizationId);
            }
            if (userId == null || accountStatus != AccountStatus.ACTIVE) {
                // User ID is not available or user account is not ACTIVE, mock SMS and password fallback to avoid fishing for active accounts
                response.setResult(AuthStepResult.CONFIRMED);
                response.setMobileTokenEnabled(false);
                logger.debug("Step authentication succeeded with fake SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            } else {
                // Find out whether mobile token is enabled
                boolean mobileTokenEnabled = false;
                try {
                    if (authMethodQueryService.isMobileTokenAvailable(userId, operation.getOperationId())) {
                        nextStepClient.updateMobileToken(operation.getOperationId(), true);
                        mobileTokenEnabled = true;
                    }
                } catch (NextStepServiceException ex) {
                    logger.error("Error occurred in Next Step server", ex);
                }
                response.setMobileTokenEnabled(mobileTokenEnabled);
                response.setResult(AuthStepResult.CONFIRMED);
                if (mobileTokenEnabled) {
                    logger.debug("Step authentication succeeded with mobile token, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                } else {
                    logger.debug("Step authentication succeeded with SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                }
            }
            return response;
        } catch (DataAdapterClientErrorException ex) {
            logger.error(ex.getMessage(), ex);
            // Send error to client
            LoginScaAuthResponse response = new LoginScaAuthResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setRemainingAttempts(ex.getError().getRemainingAttempts());
            response.setMessage(ex.getError().getMessage());
            return response;
        }
    }

    /**
     * Prepare login form data.
     * @param request Prepare login form data request.
     * @return Prepare login form response.
     * @throws AuthStepException Thrown when request is invalid or communication with Next Step fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public LoginScaInitResponse initScaLogin(@RequestBody LoginScaInitRequest request) throws AuthStepException {
        final LoginScaInitResponse response = new LoginScaInitResponse();
        final GetOperationDetailResponse operation = getOperation();

        try {
            logger.info("Step init started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            // Set chosen authentication method to LOGIN_SCA
            nextStepClient.updateChosenAuthMethod(operation.getOperationId(), AuthMethod.LOGIN_SCA);

            if (config.isClientCertificateAuthenticationEnabled()) {
                response.setClientCertificateAuthenticationEnabled(true);
                response.setClientCertificateVerificationUrl(config.getCertificateVerificationUrlForLogin());
            }
            if (operation.getUserId() != null && operation.getOrganizationId() != null) {
                // Username form can be skipped
                response.setUserAlreadyKnown(true);
                // Find out whether mobile token is enabled
                boolean mobileTokenEnabled = false;
                try {
                    if (authMethodQueryService.isMobileTokenAvailable(operation.getUserId(), operation.getOperationId())) {
                        mobileTokenEnabled = true;
                    }
                } catch (NextStepServiceException ex) {
                    logger.error("Error occurred in Next Step server", ex);
                }
                response.setMobileTokenEnabled(mobileTokenEnabled);
                logger.info("Step init skipped, user and organization is already known, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                return response;
            }
            ObjectResponse<GetOrganizationListResponse> nsObjectResponse = nextStepClient.getOrganizationList();
            List<GetOrganizationDetailResponse> nsResponseList = nsObjectResponse.getResponseObject().getOrganizations();
            for (GetOrganizationDetailResponse nsResponse: nsResponseList) {
                OrganizationDetail organization = organizationConverter.fromNSOrganization(nsResponse);
                response.addOrganization(organization);
            }
        } catch (NextStepServiceException ex) {
            logger.error("Error occurred in Next Step server", ex);
            throw new CommunicationFailedException("Communication with Next Step service failed");
        }
        logger.info("Step authentication succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        return response;
    }

    /**
     * Get current authentication method name.
     * @return Current authentication method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.LOGIN_SCA;
    }

    /**
     * Set username in HTTP session.
     * @param username Username supplied by user.
     */
    private void updateUsernameInHttpSession(String username) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.USERNAME, username);
        }
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
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public AuthStepResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (NextStepServiceException ex) {
            logger.error("Error occurred in Next Step server", ex);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
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
     * @throws NextStepServiceException In case communication with Next Step service fails.
     * @throws AuthStepException In case step authentication fails.
     */
    private boolean verifyClientCertificate(String operationId, String userId, String organizationId, String clientCertificate, AccountStatus accountStatus, OperationContext operationContext) throws DataAdapterClientErrorException, NextStepServiceException, AuthStepException {
        ObjectResponse<VerifyCertificateResponse> objectResponseCert = dataAdapterClient.verifyClientCertificate(userId, organizationId, clientCertificate, getAuthMethodName(), accountStatus, operationContext);
        VerifyCertificateResponse certResponse = objectResponseCert.getResponseObject();
        CertificateVerificationResult verificationResult = certResponse.getCertificateVerificationResult();
        if (verificationResult == CertificateVerificationResult.SUCCEEDED) {
            return true;
        }
        logger.debug("Step authentication failed with client certificate, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
        List<AuthInstrument> authInstruments = Collections.singletonList(AuthInstrument.CLIENT_CERTIFICATE);
        UpdateOperationResponse response = failAuthorization(operationId, userId, authInstruments, null);
        if (response.getResult() == AuthResult.FAILED) {
            // FAILED result instead of CONTINUE means the authentication method is failed
            throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
        }
        Integer remainingAttemptsDA = certResponse.getRemainingAttempts();
        boolean showRemainingAttempts = certResponse.getShowRemainingAttempts();
        String errorMessage = certResponse.getErrorMessage();
        UserAccountStatus userAccountStatus = userAccountStatusConverter.fromAccountStatus(certResponse.getAccountStatus());

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

    /**
     * User was successfully authenticated using client TLS certificate. Move user to the next step.
     * @return Login SCA authentication response.
     */
    private LoginScaAuthResponse authenticateStepUsingClientCertificate(String operationId, String userId, String organizationId) {
        List<AuthInstrument> authInstruments = Collections.singletonList(AuthInstrument.CLIENT_CERTIFICATE);
        try {
            UpdateOperationResponse updateResponse = authorize(operationId, userId, organizationId, authInstruments, null);
            final LoginScaAuthResponse response = new LoginScaAuthResponse();
            response.setResult(AuthStepResult.CONFIRMED);
            response.setMessage("authentication.success");
            response.getNext().addAll(updateResponse.getSteps());
            logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, AuthMethod.LOGIN_SCA);
            return response;
        } catch (AuthStepException ex) {
            logger.error("Error while building authorization response for client TLS certificate verification", ex);
            final LoginScaAuthResponse response = new LoginScaAuthResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("authentication.fail");
            return response;
        } catch (NextStepServiceException ex) {
            logger.error("Error while communicating with Next Step service", ex);
            final LoginScaAuthResponse response = new LoginScaAuthResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            return response;
        }
    }

}
