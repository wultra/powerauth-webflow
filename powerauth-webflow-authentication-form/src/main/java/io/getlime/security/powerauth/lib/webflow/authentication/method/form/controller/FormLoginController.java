/*
 * Copyright 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.method.form.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserAuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AfsActionDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.AesEncryptionPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.NoPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.PasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.UsernamePasswordInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.UsernamePasswordAuthRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.UsernamePasswordInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.UsernamePasswordAuthResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthenticationResult;
import io.getlime.security.powerauth.lib.webflow.authentication.model.OrganizationDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.AuthInstrumentConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.OrganizationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.UserAccountStatusConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AfsIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller for username / password authentication step.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/form")
public class FormLoginController extends AuthMethodController<UsernamePasswordAuthRequest, UsernamePasswordAuthResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(FormLoginController.class);

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;
    private final WebFlowServicesConfiguration configuration;
    private final AfsIntegrationService afsIntegrationService;

    private final OrganizationConverter organizationConverter = new OrganizationConverter();
    private final UserAccountStatusConverter statusConverter = new UserAccountStatusConverter();
    private final AuthInstrumentConverter authInstrumentConverter = new AuthInstrumentConverter();
    private final UserAccountStatusConverter userAccountStatusConverter = new UserAccountStatusConverter();

    /**
     * Controller constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param nextStepClient Next Step client.
     * @param configuration Web Flow configuration.
     * @param afsIntegrationService AFS integration service.
     */
    @Autowired
    public FormLoginController(DataAdapterClient dataAdapterClient, NextStepClient nextStepClient, WebFlowServicesConfiguration configuration, AfsIntegrationService afsIntegrationService) {
        this.dataAdapterClient = dataAdapterClient;
        this.nextStepClient = nextStepClient;
        this.configuration = configuration;
        this.afsIntegrationService = afsIntegrationService;
    }

    /**
     * Authenticate using username / password authentication.
     * @param request Authentication request.
     * @return Authentication result with user ID and organization ID.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @Override
    protected AuthenticationResult authenticate(UsernamePasswordAuthRequest request) throws AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        checkOperationExpiration(operation);

        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData, applicationContext);
            ObjectResponse<UserDetailResponse> lookupResponse = dataAdapterClient.lookupUser(request.getUsername(), request.getOrganizationId(), operationContext);

            String userId = lookupResponse.getResponseObject().getId();
            String organizationId = lookupResponse.getResponseObject().getOrganizationId();
            AccountStatus accountStatus = lookupResponse.getResponseObject().getAccountStatus();

            nextStepClient.updateOperationUser(operation.getOperationId(), userId, organizationId, statusConverter.fromAccountStatus(accountStatus));
            if (configuration.isAfsEnabled() && !afsLoginAuthAlreadyExecuted(operation)) {
                // Trigger LOGIN_INIT action for the first time
                AfsAction afsAction = AfsAction.LOGIN_INIT;
                afsIntegrationService.executeInitAction(operation.getOperationId(), request.getUsername(), afsAction);
                // Currently the AFS call is only informational, there is no step-down implemented
            }

            PasswordProtectionType passwordProtectionType = configuration.getPasswordProtection();
            String cipherTransformation = configuration.getCipherTransformation();
            PasswordProtection passwordProtection;
            switch (passwordProtectionType) {
                case NO_PROTECTION:
                    // Password is sent in plain text
                    passwordProtection = new NoPasswordProtection();
                    logger.info("No protection is used for protecting user password");
                    break;

                case PASSWORD_ENCRYPTION_AES:
                    // Encrypt user password in case password encryption is configured in Web Flow
                    passwordProtection = new AesEncryptionPasswordProtection(cipherTransformation, configuration.getPasswordEncryptionKey());
                    logger.info("User password is protected using transformation: {}", cipherTransformation);
                    break;

                default:
                    // Unsupported authentication type
                    throw new AuthStepException("Invalid authentication type", "error.invalidRequest");
            }

            String protectedPassword = passwordProtection.protect(request.getPassword());

            if (accountStatus != AccountStatus.ACTIVE) {
                throw new AuthStepException("User authentication failed", "login.authenticationFailed");
            }

            AuthenticationContext authenticationContext = new AuthenticationContext(passwordProtectionType, cipherTransformation);

            ObjectResponse<UserAuthenticationResponse> objectResponse = dataAdapterClient.authenticateUser(userId, organizationId, protectedPassword, authenticationContext, operationContext);
            UserAuthenticationResponse authResponse = objectResponse.getResponseObject();
            if (authResponse.getAuthenticationResult() == UserAuthenticationResult.SUCCEEDED) {
                logger.info("Step authentication succeeded, operation ID: {}, user ID: {}, authentication method: {}", operation.getOperationId(), authResponse.getUserDetail().getId(), getAuthMethodName().toString());
                return new AuthenticationResult(authResponse.getUserDetail().getId(), authResponse.getUserDetail().getOrganizationId());
            } else {
                if ("login.authenticationFailed".equals(authResponse.getErrorMessage())) {
                    UpdateOperationResponse response = failAuthorization(operation.getOperationId(), null, request.getAuthInstruments(), null);
                    if (response.getResult() == AuthResult.FAILED) {
                        // FAILED result instead of CONTINUE means the authentication method is failed
                        throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
                    }
                }
                Integer remainingAttemptsDA = authResponse.getRemainingAttempts();
                boolean showRemainingAttempts = authResponse.getShowRemainingAttempts();
                String errorMessage = authResponse.getErrorMessage();
                UserAccountStatus userAccountStatus = userAccountStatusConverter.fromAccountStatus(authResponse.getAccountStatus());

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
        } catch (NextStepServiceException e) {
            logger.error("Error occurred in Next Step server", e);
            throw new AuthStepException(e.getError().getMessage(), e, "error.communication");
        } catch (DataAdapterClientErrorException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    /**
     * Get current authentication method name.
     * @return Current authentication method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.USERNAME_PASSWORD_AUTH;
    }

    /**
     * Handle the user authentication based on username and password.
     *
     * @param request Authentication request using username and password.
     * @return Authentication response.
     * @throws AuthStepException Thrown in case authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody UsernamePasswordAuthResponse authenticateHandler(@RequestBody UsernamePasswordAuthRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final String username = request.getUsername();
        final AfsAction afsAction;
        if (configuration.isAfsEnabled()) {
            afsAction = AfsAction.LOGIN_AUTH;
        } else {
            afsAction = null;
        }
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                final List<AfsAuthInstrument> authInstruments = authInstrumentConverter.fromAuthInstruments(request.getAuthInstruments());

                @Override
                public UsernamePasswordAuthResponse doneAuthentication(String userId) {
                    if (afsAction != null) {
                        afsIntegrationService.executeAuthAction(operation.getOperationId(), afsAction, username, authInstruments,  AuthStepResult.CONFIRMED);
                    }
                    authenticateCurrentBrowserSession();
                    final UsernamePasswordAuthResponse response = new UsernamePasswordAuthResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    logger.info("Step result: CONFIRMED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public UsernamePasswordAuthResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final UsernamePasswordAuthResponse response = new UsernamePasswordAuthResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public UsernamePasswordAuthResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    if (afsAction != null) {
                        afsIntegrationService.executeAuthAction(operation.getOperationId(), afsAction, username, authInstruments, AuthStepResult.CONFIRMED);
                    }
                    final UsernamePasswordAuthResponse response = new UsernamePasswordAuthResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
                    return response;
                }
            });
        } catch (AuthStepException e) {
            logger.warn("Error occurred while authenticating user: {}", e.getMessage());
            if (afsAction != null) {
                final List<AfsAuthInstrument> authInstruments = authInstrumentConverter.fromAuthInstruments(request.getAuthInstruments());
                if (e instanceof AuthenticationFailedException) {
                    AuthenticationFailedException authEx = (AuthenticationFailedException) e;
                    if (authEx.getAccountStatus() != UserAccountStatus.ACTIVE) {
                        // notify AFS about failed authentication method due to the fact that user account is not active
                        afsIntegrationService.executeAuthAction(operation.getOperationId(), afsAction, username, authInstruments, AuthStepResult.AUTH_METHOD_FAILED);
                    } else {
                        // notify AFS about failed authentication
                        afsIntegrationService.executeAuthAction(operation.getOperationId(), afsAction, username, authInstruments, AuthStepResult.AUTH_FAILED);
                    }
                } else if (e instanceof MaxAttemptsExceededException) {
                    // notify AFS about failed authentication method due to last attempt
                    afsIntegrationService.executeAuthAction(operation.getOperationId(), afsAction, username, authInstruments, AuthStepResult.AUTH_METHOD_FAILED);
                    // notify AFS about logout
                    afsIntegrationService.executeLogoutAction(operation.getOperationId(), OperationTerminationReason.FAILED);
                }
            }
            final UsernamePasswordAuthResponse response = new UsernamePasswordAuthResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            if (e.getMessageId() != null) {
                // prefer localized message over regular message string
                response.setMessage(e.getMessageId());
            } else {
                response.setMessage(e.getMessage());
            }
            response.setRemainingAttempts(e.getRemainingAttempts());
            return response;
        }

    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody
    UsernamePasswordAuthResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final UsernamePasswordAuthResponse response = new UsernamePasswordAuthResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (NextStepServiceException e) {
            logger.error("Error occurred in Next Step server", e);
            final UsernamePasswordAuthResponse response = new UsernamePasswordAuthResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
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
    public @ResponseBody
    UsernamePasswordInitResponse initLoginForm(@RequestBody UsernamePasswordInitRequest request) throws AuthStepException {
        if (request == null) {
            throw new AuthStepException("Invalid request", "error.invalidRequest");
        }
        final GetOperationDetailResponse operation = getOperation();
        logger.info("Init step started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        checkOperationExpiration(operation);
        final UsernamePasswordInitResponse response = new UsernamePasswordInitResponse();
        try {
            ObjectResponse<GetOrganizationListResponse> nsObjectResponse = nextStepClient.getOrganizationList();
            List<GetOrganizationDetailResponse> nsResponseList = nsObjectResponse.getResponseObject().getOrganizations();
            for (GetOrganizationDetailResponse nsResponse: nsResponseList) {
                OrganizationDetail organization = organizationConverter.fromNSOrganization(nsResponse);
                response.addOrganization(organization);
            }
        } catch (NextStepServiceException e) {
            throw new CommunicationFailedException("Organization is not available");
        }
        logger.debug("Init step succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        return response;
    }

    /**
     * Determine whether LOGIN_AUTH action was already executed.
     * @param operation Operation.
     * @return Whether LOGIN_AUTH action was already executed.
     */
    private boolean afsLoginAuthAlreadyExecuted(GetOperationDetailResponse operation) {
        if (operation.getAfsActions().isEmpty()) {
            return false;
        }
        for (AfsActionDetail detail: operation.getAfsActions()) {
            if (AfsAction.LOGIN_AUTH.toString().equals(detail.getAction())) {
                return true;
            }
        }
        return false;
    }
}
