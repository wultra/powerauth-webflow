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
package io.getlime.security.powerauth.lib.webflow.authentication.sms.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AfsResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifySmsAndPasswordResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifySmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.AesEncryptionPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.NoPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthenticationResult;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AfsIntegrationService;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.request.SmsAuthorizationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response.InitSmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response.ResendSmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response.SmsAuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller which provides endpoints for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/sms")
public class SmsAuthorizationController extends AuthMethodController<SmsAuthorizationRequest, SmsAuthorizationResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(SmsAuthorizationController.class);

    private final DataAdapterClient dataAdapterClient;
    private final WebFlowServicesConfiguration configuration;
    private final AfsIntegrationService afsIntegrationService;
    private final HttpSession httpSession;

    /**
     * Controller constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param configuration Web Flow configuration.
     * @param afsIntegrationService Anti-fraud system integration service.
     * @param httpSession HTTP session.
     */
    @Autowired
    public SmsAuthorizationController(DataAdapterClient dataAdapterClient, WebFlowServicesConfiguration configuration, AfsIntegrationService afsIntegrationService, HttpSession httpSession) {
        this.dataAdapterClient = dataAdapterClient;
        this.configuration = configuration;
        this.afsIntegrationService = afsIntegrationService;
        this.httpSession = httpSession;
    }

    /**
     * Verifies the authorization code entered by user against code generated during initialization.
     *
     * @param request Request with authentication object information.
     * @return Authentication result with user ID and organization ID.
     * @throws AuthStepException Exception is thrown when authorization fails.
     */
    @Override
    protected AuthenticationResult authenticate(SmsAuthorizationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
        checkOperationExpiration(operation);
        final String messageId = getMessageIdFromHttpSession();
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            String operationId = operation.getOperationId();
            String operationName = operation.getOperationName();
            String operationData = operation.getOperationData();
            String userId = operation.getUserId();
            String organizationId = operation.getOrganizationId();
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operationId, operationName, operationData, formData, applicationContext);
            SmsAuthorizationResult smsAuthorizationResult;
            Integer remainingAttemptsDA;
            boolean showRemainingAttempts;
            String errorMessage;
            switch (authMethod) {
                case SMS_KEY: {
                    ObjectResponse<VerifySmsAuthorizationResponse> objectResponse = dataAdapterClient.verifyAuthorizationSms(messageId, request.getAuthCode(), userId, organizationId, operationContext);
                    VerifySmsAuthorizationResponse smsResponse = objectResponse.getResponseObject();
                    smsAuthorizationResult = smsResponse.getSmsAuthorizationResult();
                    if (smsAuthorizationResult == SmsAuthorizationResult.VERIFIED_SUCCEEDED) {
                        cleanHttpSession();
                        logger.info("Step authentication succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                        return new AuthenticationResult(operation.getUserId(), operation.getOrganizationId());
                    }
                    remainingAttemptsDA = smsResponse.getRemainingAttempts();
                    showRemainingAttempts = smsResponse.getShowRemainingAttempts();
                    errorMessage = smsResponse.getErrorMessage();
                    break;
                }

                case LOGIN_SCA:
                case APPROVAL_SCA: {
                    PasswordProtectionType passwordProtectionType = configuration.getPasswordProtection();
                    String cipherTransformation = configuration.getCipherTransformation();
                    io.getlime.security.powerauth.lib.webflow.authentication.encryption.PasswordProtection passwordProtection;
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
                            throw new InvalidRequestException("Invalid authentication type");
                    }

                    String protectedPassword = passwordProtection.protect(request.getPassword());
                    String authCode = request.getAuthCode();
                    AuthenticationContext authenticationContext = new AuthenticationContext(passwordProtectionType, cipherTransformation);
                    ObjectResponse<VerifySmsAndPasswordResponse> objectResponse = dataAdapterClient.verifyAuthorizationSmsAndPassword(messageId, authCode, userId, organizationId, protectedPassword, authenticationContext, operationContext);
                    VerifySmsAndPasswordResponse smsResponse = objectResponse.getResponseObject();
                    smsAuthorizationResult = smsResponse.getSmsAuthorizationResult();
                    if (smsAuthorizationResult == SmsAuthorizationResult.VERIFIED_SUCCEEDED && smsResponse.getUserAuthenticationResult() == UserAuthenticationResult.VERIFIED_SUCCEEDED) {
                        cleanHttpSession();
                        logger.info("Step authentication succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                        return new AuthenticationResult(operation.getUserId(), operation.getOrganizationId());
                    }
                    remainingAttemptsDA = smsResponse.getRemainingAttempts();
                    showRemainingAttempts = smsResponse.getShowRemainingAttempts();
                    errorMessage = smsResponse.getErrorMessage();
                    break;
                }

                default:
                    throw new InvalidRequestException("Invalid request");

            }
            try {
                UpdateOperationResponse response = failAuthorization(operation.getOperationId(), operation.getUserId(), null);
                if (response.getResult() == AuthResult.FAILED) {
                    cleanHttpSession();
                    // FAILED result instead of CONTINUE means the authentication method is failed
                    throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
                }
                if (showRemainingAttempts) {
                    GetOperationDetailResponse updatedOperation = getOperation();
                    Integer remainingAttemptsNS = updatedOperation.getRemainingAttempts();
                    AuthStepException authEx = new AuthStepException("SMS authorization failed", errorMessage);
                    Integer remainingAttempts = resolveRemainingAttempts(remainingAttemptsDA, remainingAttemptsNS);
                    authEx.setRemainingAttempts(remainingAttempts);
                    throw authEx;
                } else {
                    throw new AuthStepException("SMS authorization failed", errorMessage);
                }
            } catch (NextStepServiceException e) {
                logger.error("Error occurred in Next Step server", e);
                throw new AuthStepException(e.getError().getMessage(), e, "error.communication");
            }
        } catch (DataAdapterClientErrorException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    /**
     * Get current authentication method.
     * @return Current authentication method.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.SMS_KEY;
    }

    /**
     * Set message ID in HTTP session.
     * @param messageId Message ID.
     */
    private void updateMessageIdInHttpSession(String messageId) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.MESSAGE_ID, messageId);
        }
    }

    /**
     * Set last message timestamp in HTTP session.
     */
    private void updateLastMessageTimestampInHttpSession(Long timestamp) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.LAST_MESSAGE_TIMESTAMP, timestamp);
        }
    }

    /**
     * Set initial message sent flag in HTTP session.
     */
    private void updateInitialMessageSentInHttpSession(Boolean initialMessageSent) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.INITIAL_MESSAGE_SENT, initialMessageSent);
        }
    }

    /**
     * Get message ID from HTTP session.
     */
    private String getMessageIdFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (String) httpSession.getAttribute(HttpSessionAttributeNames.MESSAGE_ID);
        }
    }

    /**
     * Get username from HTTP session.
     */
    private String getUsernameFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (String) httpSession.getAttribute(HttpSessionAttributeNames.USERNAME);
        }
    }

    /**
     * Get last message timestamp from HTTP session.
     */
    private Long getLastMessageTimestampFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (Long) httpSession.getAttribute(HttpSessionAttributeNames.LAST_MESSAGE_TIMESTAMP);
        }
    }

    /**
     * Get initial message sent flag from HTTP session.
     */
    private Boolean getInitialMessageSentFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (Boolean) httpSession.getAttribute(HttpSessionAttributeNames.INITIAL_MESSAGE_SENT);
        }
    }
    /**
     * Clean HTTP session.
     */
    private void cleanHttpSession() {
        synchronized (httpSession.getServletContext()) {
            httpSession.removeAttribute(HttpSessionAttributeNames.MESSAGE_ID);
            httpSession.removeAttribute(HttpSessionAttributeNames.LAST_MESSAGE_TIMESTAMP);
            httpSession.removeAttribute(HttpSessionAttributeNames.INITIAL_MESSAGE_SENT);
            httpSession.removeAttribute(HttpSessionAttributeNames.USERNAME);
        }
    }

    /**
     * Initializes the SMS authorization process by creating authorization SMS using Data Adapter.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public @ResponseBody InitSmsAuthorizationResponse initSmsAuthorization() throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        logger.info("Init step started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
        checkOperationExpiration(operation);
        InitSmsAuthorizationResponse initResponse = new InitSmsAuthorizationResponse();

        // By default enable both SMS authorization and password verification (2FA)
        initResponse.setSmsOtpEnabled(true);
        initResponse.setPasswordEnabled(true);

        if (authMethod == AuthMethod.LOGIN_SCA) {
            // Add username for LOGIN_SCA method
            String username = getUsernameFromHttpSession();
            initResponse.setUsername(username);
        }

        if (authMethod == AuthMethod.LOGIN_SCA || authMethod == AuthMethod.APPROVAL_SCA) {

            // Choose current AFS action
            AfsAction afsAction;
            if (authMethod == AuthMethod.LOGIN_SCA) {
                afsAction = AfsAction.LOGIN_INIT;
            } else {
                afsAction = AfsAction.APPROVAL_INIT;
            }

            // Execute an AFS action
            AfsResponse afsResponse = afsIntegrationService.executeInitAction(operation, afsAction);

            // Process AFS response
            if (afsResponse.getApplyAfsResponse()) {
                if (afsResponse.getAuthStepOptions() != null) {
                    if (!afsResponse.getAuthStepOptions().isPasswordRequired()) {
                        logger.debug("Disabling password verification based on AFS response in INIT step of authentication method: {}, operation ID: {}", authMethod, operation.getOperationId());
                        // Step-down for password verification
                        initResponse.setPasswordEnabled(false);
                    }
                    if (!afsResponse.getAuthStepOptions().isSmsOtpRequired()) {
                        logger.debug("Disabling SMS authorization due based on AFS response in INIT step of authentication method: {}, operation ID: {}", authMethod, operation.getOperationId());
                        // Step-down for SMS authorization
                        initResponse.setSmsOtpEnabled(false);
                    }
                }
            }
        }

        try {
            if (initResponse.isSmsOtpEnabled()) {
                initResponse.setResendDelay(configuration.getSmsResendDelay());
                Boolean initialMessageSent = getInitialMessageSentFromHttpSession();
                if (initialMessageSent != null && initialMessageSent) {
                    initResponse.setResult(AuthStepResult.CONFIRMED);
                    return initResponse;
                }
                CreateSmsAuthorizationResponse response = sendAuthorizationSms(operation, false);
                String messageId = response.getMessageId();
                if (messageId != null) {
                    updateMessageIdInHttpSession(messageId);
                    updateInitialMessageSentInHttpSession(true);
                }
                if (SmsDeliveryResult.SUCCEEDED.equals(response.getSmsDeliveryResult())) {
                    initResponse.setResult(AuthStepResult.CONFIRMED);
                    logger.info("Init step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                } else {
                    initResponse.setResult(AuthStepResult.AUTH_FAILED);
                    initResponse.setMessage(response.getErrorMessage());
                    logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                }
            }

            return initResponse;
        } catch (DataAdapterClientErrorException e) {
            logger.error("Error when sending SMS message.", e);
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            initResponse.setMessage(e.getError().getMessage());
            return initResponse;
        }
    }

    /**
     * Resend the SMS using Data Adapter.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/resend", method = RequestMethod.POST)
    public @ResponseBody ResendSmsAuthorizationResponse resendSmsAuthorization() throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        logger.info("Resend step started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
        checkOperationExpiration(operation);
        ResendSmsAuthorizationResponse resendResponse = new ResendSmsAuthorizationResponse();
        resendResponse.setResendDelay(configuration.getSmsResendDelay());
        try {
            CreateSmsAuthorizationResponse response = sendAuthorizationSms(operation, true);
            String messageId = response.getMessageId();
            if (messageId != null) {
                updateMessageIdInHttpSession(messageId);
            }
            if (SmsDeliveryResult.SUCCEEDED.equals(response.getSmsDeliveryResult())) {
                resendResponse.setResult(AuthStepResult.CONFIRMED);
                logger.info("Resend step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            } else {
                resendResponse.setResult(AuthStepResult.AUTH_FAILED);
                resendResponse.setMessage(response.getErrorMessage());
                logger.info("Resend step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            }
            return resendResponse;
        } catch (DataAdapterClientErrorException e) {
            logger.error("Error when sending SMS message.", e);
            resendResponse.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Resend step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            resendResponse.setMessage(e.getError().getMessage());
            return resendResponse;
        }
    }

    /**
     * Performs the authorization and resolves the next step.
     *
     * @param request Authorization request which includes the authorization code.
     * @return Authorization response.
     * @throws AuthStepException In case authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody SmsAuthorizationResponse authenticateHandler(@RequestBody SmsAuthorizationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        // Choose current AFS action
        final AfsAction afsAction;
        final List<AuthInstrument> authInstruments = new ArrayList<>();
        if (authMethod == AuthMethod.LOGIN_SCA || authMethod == AuthMethod.APPROVAL_SCA) {
            if (authMethod == AuthMethod.LOGIN_SCA) {
                afsAction = AfsAction.LOGIN_AUTH;
            } else {
                afsAction = AfsAction.APPROVAL_AUTH;
            }
            if (request.getPassword() != null) {
                authInstruments.add(AuthInstrument.PASSWORD);
            }
            if (request.getAuthCode() != null) {
                authInstruments.add(AuthInstrument.SMS_KEY);
            }
        } else {
            afsAction = null;
        }

        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public SmsAuthorizationResponse doneAuthentication(String userId) {
                    if (afsAction != null) {
                        afsIntegrationService.executeAuthAction(operation, afsAction, authInstruments, 1, AuthStepResult.CONFIRMED);
                    }
                    authenticateCurrentBrowserSession();
                    final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    logger.info("Step result: CONFIRMED, authentication method: {}", authMethod.toString());
                    return response;
                }

                @Override
                public SmsAuthorizationResponse failedAuthentication(String userId, String failedReason) {
                    if (afsAction != null) {
                        afsIntegrationService.executeAuthAction(operation, afsAction, authInstruments, 1, AuthStepResult.AUTH_FAILED);
                    }
                    clearCurrentBrowserSession();
                    final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", authMethod.toString());
                    return response;
                }

                @Override
                public SmsAuthorizationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    if (afsAction != null) {
                        afsIntegrationService.executeAuthAction(operation, afsAction, authInstruments, 1, AuthStepResult.CONFIRMED);
                    }
                    final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, authMethod.toString());
                    return response;
                }
            });
        } catch (AuthStepException e) {
            logger.warn("Error occurred while verifying authorization code from SMS message: {}", e.getMessage());
            final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Step result: AUTH_FAILED, authentication method: {}", authMethod.toString());
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
     * Cancels the SMS authorization.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody SmsAuthorizationResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            final AuthMethod authMethod = getAuthMethodName(operation);
            cleanHttpSession();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final SmsAuthorizationResponse cancelResponse = new SmsAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.CANCELED);
            cancelResponse.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return cancelResponse;
        } catch (NextStepServiceException e) {
            logger.error("Error when canceling SMS message validation.", e);
            final SmsAuthorizationResponse cancelResponse = new SmsAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.AUTH_FAILED);
            cancelResponse.setMessage("error.communication");
            cleanHttpSession();
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return cancelResponse;
        }
    }

    /**
     * Send authorization SMS using data adapter. Check SMS resend delay to protect backends from spamming.
     * @param operation Current operation.
     * @param resend Whether SMS is being resent.
     * @return Response for sending authorization SMS message.
     * @throws DataAdapterClientErrorException In case SMS delivery fails.
     */
    private CreateSmsAuthorizationResponse sendAuthorizationSms(GetOperationDetailResponse operation, boolean resend) throws DataAdapterClientErrorException, AuthStepException {
        Long lastMessageTimestamp = getLastMessageTimestampFromHttpSession();
        if (lastMessageTimestamp != null && System.currentTimeMillis() - lastMessageTimestamp < configuration.getSmsResendDelay()) {
            CreateSmsAuthorizationResponse response = new CreateSmsAuthorizationResponse();
            response.setSmsDeliveryResult(SmsDeliveryResult.FAILED);
            response.setErrorMessage("smsAuthorization.deliveryFailed");
            return response;
        }
        String userId = operation.getUserId();
        String organizationId = operation.getOrganizationId();
        FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
        ApplicationContext applicationContext = operation.getApplicationContext();
        String operationId = operation.getOperationId();
        String operationName = operation.getOperationName();
        String operationData = operation.getOperationData();
        OperationContext operationContext = new OperationContext(operationId, operationName, operationData, formData, applicationContext);
        ObjectResponse<CreateSmsAuthorizationResponse> daResponse = dataAdapterClient.createAuthorizationSms(userId, organizationId, operationContext, LocaleContextHolder.getLocale().getLanguage(), resend);
        updateLastMessageTimestampInHttpSession(System.currentTimeMillis());
        return daResponse.getResponseObject();
    }

}
