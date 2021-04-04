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
import io.getlime.security.powerauth.lib.dataadapter.model.converter.UserAccountStatusConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAction;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAuthInstrument;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PasswordProtectionType;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AfsActionDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.AesEncryptionPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.NoPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.PasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.UsernamePasswordAuthRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.UsernamePasswordInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.UsernamePasswordAuthResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.UsernamePasswordInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthResultDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.model.OrganizationDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.AuthInstrumentConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.OrganizationConverter;
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

    private final NextStepClient nextStepClient;
    private final WebFlowServicesConfiguration configuration;
    private final AfsIntegrationService afsIntegrationService;

    private final OrganizationConverter organizationConverter = new OrganizationConverter();
    private final AuthInstrumentConverter authInstrumentConverter = new AuthInstrumentConverter();
    private final UserAccountStatusConverter statusConverter = new UserAccountStatusConverter();

    /**
     * Controller constructor.
     * @param nextStepClient Next Step client.
     * @param configuration Web Flow configuration.
     * @param afsIntegrationService AFS integration service.
     */
    @Autowired
    public FormLoginController(NextStepClient nextStepClient, WebFlowServicesConfiguration configuration, AfsIntegrationService afsIntegrationService) {
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
    protected AuthResultDetail authenticate(UsernamePasswordAuthRequest request) throws AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());

        try {
            GetOrganizationDetailResponse organization = nextStepClient.getOrganizationDetail(request.getOrganizationId()).getResponseObject();
            if (organization.getDefaultCredentialName() == null) {
                logger.warn("Default credential name is not configured for organization: " + request.getOrganizationId());
                throw new AuthStepException("User authentication failed", "error.communication");
            }
            String organizationId = organization.getOrganizationId();
            String credentialName = organization.getDefaultCredentialName();
            String username = request.getUsername();

            // Client certificate is not yet supported in non SCA login method
            LookupUserResponse lookupResponse;
            try {
                lookupResponse = nextStepClient.lookupUser(username, credentialName, operation.getOperationId()).getResponseObject();
            } catch (NextStepClientException ex) {
                if (ex.getNextStepError() != null && UserNotFoundException.CODE.equals(ex.getNextStepError().getCode())) {
                    // User ID not found using lookup
                    throw new AuthStepException("User authentication failed", "login.authenticationFailed");
                }
                throw ex;
            }

            GetUserDetailResponse userDetail = lookupResponse.getUser();
            String userId = userDetail.getUserId();
            UserIdentityStatus status = userDetail.getUserIdentityStatus();

            UserAccountStatus accountStatus = statusConverter.toUserAccountStatus(status);
            nextStepClient.updateOperationUser(operation.getOperationId(), userId, organizationId, accountStatus);
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

            if (status != UserIdentityStatus.ACTIVE) {
                throw new AuthStepException("User authentication failed", "login.authenticationFailed");
            }

            CredentialAuthenticationResponse authResponse = nextStepClient.authenticateWithCredential(credentialName, userId, protectedPassword, operation.getOperationId(), true, AuthMethod.USERNAME_PASSWORD_AUTH).getResponseObject();
            if (authResponse.isOperationFailed()) {
                logger.info("Step authentication failed due to failed operation, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
            }
            if (authResponse.getAuthenticationResult() == AuthenticationResult.SUCCEEDED) {
                logger.info("Step authentication succeeded, operation ID: {}, user ID: {}, authentication method: {}", operation.getOperationId(), authResponse.getUserId(), getAuthMethodName().toString());
                return new AuthResultDetail(userId, organizationId, true);
            } else {
                Integer remainingAttempts = authResponse.getRemainingAttempts();
                if (remainingAttempts != null && remainingAttempts == 0) {
                    throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
                }
                String errorMessage = "login.authenticationFailed";
                if (authResponse.getErrorMessage() != null) {
                    errorMessage = authResponse.getErrorMessage();
                }
                AuthenticationFailedException authEx = new AuthenticationFailedException("Authentication failed", errorMessage);
                if (authResponse.isShowRemainingAttempts()) {
                    authEx.setRemainingAttempts(remainingAttempts);
                }
                authEx.setAccountStatus(accountStatus);
                throw authEx;
            }
        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
            throw new AuthStepException("User authentication failed", ex, "error.communication");
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
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null, false);
            final UsernamePasswordAuthResponse response = new UsernamePasswordAuthResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (CommunicationFailedException ex) {
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
    public @ResponseBody UsernamePasswordInitResponse initLoginForm(@RequestBody UsernamePasswordInitRequest request) throws AuthStepException {
        if (request == null) {
            throw new AuthStepException("Invalid request", "error.invalidRequest");
        }
        final GetOperationDetailResponse operation = getOperation();
        logger.info("Init step started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        final UsernamePasswordInitResponse response = new UsernamePasswordInitResponse();
        try {
            ObjectResponse<GetOrganizationListResponse> nsObjectResponse = nextStepClient.getOrganizationList();
            List<GetOrganizationDetailResponse> nsResponseList = nsObjectResponse.getResponseObject().getOrganizations();
            for (GetOrganizationDetailResponse nsResponse: nsResponseList) {
                // Show only organizations which have a display name key set to avoid broken UI
                if (nsResponse.getDisplayNameKey() != null) {
                    OrganizationDetail organization = organizationConverter.fromNSOrganization(nsResponse);
                    response.addOrganization(organization);
                }
            }
        } catch (NextStepClientException ex) {
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
