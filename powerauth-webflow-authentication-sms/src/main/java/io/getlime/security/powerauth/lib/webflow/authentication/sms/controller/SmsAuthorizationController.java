/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.sms.controller;

import com.wultra.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.UserAccountStatusConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AfsResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthStepOptions;
import io.getlime.security.powerauth.lib.dataadapter.model.response.VerifyCertificateResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.AesEncryptionPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.encryption.NoPasswordProtection;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.*;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthResultDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthorizationOtpDeliveryResult;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.AuthInstrumentConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AfsIntegrationService;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.request.SmsAuthorizationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response.InitSmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response.ResendSmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response.SmsAuthorizationResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Controller which provides endpoints for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/sms")
public class SmsAuthorizationController extends AuthMethodController<SmsAuthorizationRequest, SmsAuthorizationResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(SmsAuthorizationController.class);

    private static final Integer OPERATION_CONFIG_TEMPLATE_LOGIN = 2;
    private static final Integer OPERATION_CONFIG_TEMPLATE_APPROVAL = 1;

    private final NextStepClient nextStepClient;
    private final DataAdapterClient dataAdapterClient;
    private final WebFlowServicesConfiguration configuration;
    private final AfsIntegrationService afsIntegrationService;
    private final HttpSession httpSession;

    private final AuthInstrumentConverter authInstrumentConverter = new AuthInstrumentConverter();
    private final UserAccountStatusConverter statusConverter = new UserAccountStatusConverter();

    /**
     * Controller constructor.
     * @param nextStepClient Next Step client.
     * @param dataAdapterClient Data Adapter client.
     * @param configuration Web Flow configuration.
     * @param afsIntegrationService Anti-fraud system integration service.
     * @param httpSession HTTP session.
     */
    @Autowired
    public SmsAuthorizationController(NextStepClient nextStepClient, DataAdapterClient dataAdapterClient, WebFlowServicesConfiguration configuration, AfsIntegrationService afsIntegrationService, HttpSession httpSession) {
        this.nextStepClient = nextStepClient;
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
    protected AuthResultDetail authenticate(SmsAuthorizationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
        try {
            if (operation.getUserId() == null || operation.getAccountStatus() != UserAccountStatus.ACTIVE) {
                // Fake OTP authentication, pretend 2FA authentication failure
                logger.info("Step authentication failed with fake SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                List<AuthInstrument> authInstruments = new ArrayList<>();
                authInstruments.add(AuthInstrument.OTP_KEY);
                authInstruments.add(AuthInstrument.CREDENTIAL);
                AuthOperationResponse response = failAuthorization(operation.getOperationId(), null, authInstruments, null, null);
                if (response.getAuthResult() == AuthResult.FAILED) {
                    // FAILED result instead of CONTINUE means the authentication method is failed
                    throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
                }
                throw new AuthenticationFailedException("Authentication failed", "login.authenticationFailed");
            }

            final String otpId = getOtpIdFromHttpSession();
            GetOrganizationDetailResponse organization = nextStepClient.getOrganizationDetail(operation.getOrganizationId()).getResponseObject();
            String otpName = organization.getDefaultOtpName();
            if (otpName == null) {
                logger.warn("Default OTP name is not configured for organization: " + operation.getOrganizationId());
                throw new AuthStepException("SMS delivery failed", "error.communication");
            }
            String operationId = operation.getOperationId();
            String userId = operation.getUserId();
            UserAccountStatus accountStatus = operation.getAccountStatus();

            String authCode = request.getAuthCode();
            AuthStepOptions authStepOptions = getAuthStepOptionsFromHttpSession();
            AuthenticationResult smsAuthorizationResult = null;
            Integer remainingAttempts = null;
            String errorMessage = null;
            boolean showRemainingAttempts = false;
            if (authStepOptions != null) {
                // Authentication step options have been derived from AFS response

                if (!authStepOptions.isSmsOtpRequired() && !authStepOptions.isPasswordRequired()) {
                    // No authentication is required, approve step
                    cleanHttpSession();
                    request.setAuthInstruments(Collections.emptyList());
                    logger.info("Step authentication succeeded (NO_FA), operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                    return new AuthResultDetail(operation.getUserId(), operation.getOrganizationId(), false, null);
                } else if (!authStepOptions.isPasswordRequired()) {
                    // Only SMS authorization is required, skip password verification
                    OtpAuthenticationResponse otpResponse = nextStepClient.authenticateWithOtp(otpId, operationId, authCode, true, authMethod).getResponseObject();
                    if (otpResponse.isOperationFailed()) {
                        logger.info("Step authentication maximum attempts reached (1FA) due to failed operation, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                        throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
                    }
                    smsAuthorizationResult = otpResponse.getAuthenticationResult();
                    request.setAuthInstruments(Collections.singletonList(AuthInstrument.OTP_KEY));
                    if (smsAuthorizationResult == AuthenticationResult.SUCCEEDED) {
                        cleanHttpSession();
                        logger.info("Step authentication succeeded (1FA), operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                        return new AuthResultDetail(operation.getUserId(), operation.getOrganizationId(), true, null);
                    }
                    remainingAttempts = otpResponse.getRemainingAttempts();
                    showRemainingAttempts = otpResponse.isShowRemainingAttempts();
                    errorMessage = otpResponse.getErrorMessage();
                }
            }
            if (smsAuthorizationResult == null) {
                // Otherwise 2FA authentication is performed

                List<AuthInstrument> authInstruments = new ArrayList<>();
                authInstruments.add(AuthInstrument.OTP_KEY);
                if (request.getSignedMessage() != null) {
                    authInstruments.add(AuthInstrument.QUALIFIED_CERTIFICATE);
                } else {
                    authInstruments.add(AuthInstrument.CREDENTIAL);
                }
                request.setAuthInstruments(authInstruments);

                // Handle authentication using qualified certificate first
                if (getApprovalByCertificateEnabledFromHttpSession() && authInstruments.contains(AuthInstrument.QUALIFIED_CERTIFICATE)) {
                    return handleAuthenticationUsingQualifiedCertificateAndOtp(request, operation, userId, authMethod);
                }

                PasswordProtectionType passwordProtectionType = configuration.getPasswordProtection();
                String cipherTransformation = configuration.getCipherTransformation();
                io.getlime.security.powerauth.lib.webflow.authentication.encryption.PasswordProtection passwordProtection;
                switch (passwordProtectionType) {
                    case NO_PROTECTION -> {
                        // Password is sent in plain text
                        passwordProtection = new NoPasswordProtection();
                        logger.info("No protection is used for protecting user password");
                    }
                    case PASSWORD_ENCRYPTION_AES -> {
                        // Encrypt user password in case password encryption is configured in Web Flow
                        passwordProtection = new AesEncryptionPasswordProtection(cipherTransformation, configuration.getPasswordEncryptionKey());
                        logger.info("User password is protected using transformation: {}", cipherTransformation);
                    }
                    default ->
                        // Unsupported authentication type
                            throw new InvalidRequestException("Invalid authentication type");
                }

                String protectedPassword = passwordProtection.protect(request.getPassword());
                CombinedAuthenticationResponse authResponse = nextStepClient.authenticateCombined(userId, protectedPassword, otpId, operationId, authCode, true, authMethod).getResponseObject();
                if (authResponse.isOperationFailed()) {
                    logger.info("Step authentication maximum attempts reached for credential and OTP verification (2FA) due to failed operation, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                    throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
                }
                if (authResponse.getAuthenticationResult() == AuthenticationResult.SUCCEEDED) {
                    cleanHttpSession();
                    logger.info("Step authentication succeeded (2FA), operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                    return new AuthResultDetail(operation.getUserId(), operation.getOrganizationId(), true, null);
                }
                remainingAttempts = authResponse.getRemainingAttempts();
                showRemainingAttempts = authResponse.isShowRemainingAttempts();
                errorMessage = authResponse.getErrorMessage();
            }

            if (errorMessage == null) {
                errorMessage = "login.authenticationFailed";
            }

            if (remainingAttempts != null && remainingAttempts == 0) {
                cleanHttpSession();
                throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
            }
            AuthenticationFailedException authEx = new AuthenticationFailedException("Authentication failed", errorMessage);
            if (showRemainingAttempts) {
                authEx.setRemainingAttempts(remainingAttempts);
            }
            authEx.setAccountStatus(accountStatus);
            throw authEx;
        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
            throw new AuthStepException("SMS authentication failed", ex, "error.communication");
        }
    }

    private AuthResultDetail handleAuthenticationUsingQualifiedCertificateAndOtp(SmsAuthorizationRequest request, GetOperationDetailResponse operation, String userId, AuthMethod authMethod) throws AuthStepException {
        final String signedMessage = request.getSignedMessage();
        final String authCode = request.getAuthCode();
        final FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
        final String organizationId = operation.getOrganizationId();
        final ApplicationContext applicationContext = operation.getApplicationContext();
        final OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formData, applicationContext);
        final AccountStatus accountStatusDA = statusConverter.fromUserAccountStatus(operation.getAccountStatus());
        try {
            return verifySignatureUsingQualifiedCertificateAndOtp(operation.getOperationId(), userId, organizationId, signedMessage, authCode, authMethod, accountStatusDA, operationContext);
        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
            throw new AuthStepException("Authentication failed in Next Step service", ex, "error.communication");
        } catch (DataAdapterClientErrorException ex) {
            logger.error("Error occurred in Data Adapter", ex);
            throw new AuthStepException("Authentication failed in Data Adapter service", ex, "error.communication");
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
     * Set OTP ID in HTTP session.
     * @param otpId OTP ID.
     */
    private void updateOtpIdInHttpSession(String otpId) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.OTP_ID, otpId);
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
     * Set authentication step options in HTTP session.
     */
    private void updateAuthStepOptionsInHttpSession(AuthStepOptions authStepOptions) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.AUTH_STEP_OPTIONS, authStepOptions);
        }
    }

    /**
     * Get OTP ID from HTTP session.
     * @return OTP ID.
     */
    private String getOtpIdFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (String) httpSession.getAttribute(HttpSessionAttributeNames.OTP_ID);
        }
    }

    /**
     * Get username from HTTP session.
     * @return Username.
     */
    private String getUsernameFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (String) httpSession.getAttribute(HttpSessionAttributeNames.USERNAME);
        }
    }

    /**
     * Get last message timestamp from HTTP session.
     * @return Last message timestamp.
     */
    private Long getLastMessageTimestampFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (Long) httpSession.getAttribute(HttpSessionAttributeNames.LAST_MESSAGE_TIMESTAMP);
        }
    }

    /**
     * Get initial message sent flag from HTTP session.
     * @return Whether initial message was sent.
     */
    private boolean getInitialMessageSentFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            Boolean initialMessageSent = (Boolean) httpSession.getAttribute(HttpSessionAttributeNames.INITIAL_MESSAGE_SENT);
            return initialMessageSent != null && initialMessageSent;
        }
    }

    /**
     * Get authentication step options from HTTP session.
     * @return Authentication step options.
     */
    private AuthStepOptions getAuthStepOptionsFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (AuthStepOptions) httpSession.getAttribute(HttpSessionAttributeNames.AUTH_STEP_OPTIONS);
        }
    }

    /**
     * Get whether approval by certificate is enabled from HTTP session.
     * @return Whether approval by certificate is enabled.
     */
    private boolean getApprovalByCertificateEnabledFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            Boolean certificateEnabled = (Boolean) httpSession.getAttribute(HttpSessionAttributeNames.APPROVAL_BY_CERTIFICATE_ENABLED);
            return certificateEnabled != null && certificateEnabled;
        }
    }

    /**
     * Get operation data external from HTTP session.
     * @return Operation data external.
     */
    private String getOperationDataExternalFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (String) httpSession.getAttribute(HttpSessionAttributeNames.OPERATION_DATA_EXTERNAL);
        }
    }

    /**
     * Clean HTTP session.
     */
    private void cleanHttpSession() {
        synchronized (httpSession.getServletContext()) {
            httpSession.removeAttribute(HttpSessionAttributeNames.OTP_ID);
            httpSession.removeAttribute(HttpSessionAttributeNames.LAST_MESSAGE_TIMESTAMP);
            httpSession.removeAttribute(HttpSessionAttributeNames.INITIAL_MESSAGE_SENT);
            httpSession.removeAttribute(HttpSessionAttributeNames.AUTH_STEP_OPTIONS);
            httpSession.removeAttribute(HttpSessionAttributeNames.USERNAME);
            httpSession.removeAttribute(HttpSessionAttributeNames.APPROVAL_BY_CERTIFICATE_ENABLED);
            httpSession.removeAttribute(HttpSessionAttributeNames.OPERATION_DATA_EXTERNAL);
        }
    }

    /**
     * Initializes the SMS authorization process by creating authorization SMS using Data Adapter.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @PostMapping("/init")
    public InitSmsAuthorizationResponse initSmsAuthorization() throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        logger.info("Init step started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
        InitSmsAuthorizationResponse initResponse = new InitSmsAuthorizationResponse();

        // By default enable both SMS authorization and password verification (2FA)
        initResponse.setSmsOtpEnabled(true);
        initResponse.setPasswordEnabled(true);

        // Enable authorization using certificate in case it has been enabled
        final boolean approvalWithCertificateEnabled = getApprovalByCertificateEnabledFromHttpSession();
        final String operationDataExternal = getOperationDataExternalFromHttpSession();
        initResponse.setCertificateEnabled(approvalWithCertificateEnabled);
        if (approvalWithCertificateEnabled) {
            initResponse.setSignatureDataBase64(resolveDataForSignature(operation.getOperationData(), operationDataExternal));
        }

        String username = null;
        if (authMethod == AuthMethod.LOGIN_SCA) {
            // Add username for LOGIN_SCA method
            username = getUsernameFromHttpSession();
            initResponse.setUsername(username);
        }

        if (operation.getUserId() == null || operation.getAccountStatus() != UserAccountStatus.ACTIVE) {
            // Operation is anonymous or user account is blocked, perform fake SMS authorization, return default response
            initResponse.setResendDelay(configuration.getSmsResendDelay());
            initResponse.setResult(AuthStepResult.CONFIRMED);
            return initResponse;
        }

        if (configuration.isAfsEnabled()) {

            AfsAction afsAction = determineAfsActionInit(authMethod, operation.getOperationName());

            if (afsAction != null) {
                // Execute an AFS action
                AfsResponse afsResponse = afsIntegrationService.executeInitAction(operation.getOperationId(), username, afsAction);

                // Save authentication step options derived from AFS response for authenticate step
                updateAuthStepOptionsInHttpSession(afsResponse.getAuthStepOptions());

                // Process AFS response
                if (afsResponse.isAfsResponseApplied()) {
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
        }

        try {
            if (initResponse.isSmsOtpEnabled()) {
                initResponse.setResendDelay(configuration.getSmsResendDelay());
                if (getInitialMessageSentFromHttpSession()) {
                    initResponse.setResult(AuthStepResult.CONFIRMED);
                    return initResponse;
                }
                AuthorizationOtpDeliveryResult result = sendAuthorizationSms(operation);
                if (result.isDelivered()) {
                    updateOtpIdInHttpSession(result.getOtpId());
                    updateLastMessageTimestampInHttpSession(System.currentTimeMillis());
                    updateInitialMessageSentInHttpSession(true);
                    initResponse.setResult(AuthStepResult.CONFIRMED);
                    logger.info("Init step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                } else {
                    initResponse.setResult(AuthStepResult.AUTH_FAILED);
                    initResponse.setMessage(result.getErrorMessage());
                    logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
                }
            }

            return initResponse;
        } catch (NextStepClientException ex) {
            logger.error("Error when sending SMS message.", ex);
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            initResponse.setMessage("smsAuthorization.deliveryFailed");
            return initResponse;
        }
    }

    /**
     * Resend the SMS using Data Adapter.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @PostMapping("/resend")
    public ResendSmsAuthorizationResponse resendSmsAuthorization() throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        logger.info("Resend step started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
        ResendSmsAuthorizationResponse resendResponse = new ResendSmsAuthorizationResponse();
        resendResponse.setResendDelay(configuration.getSmsResendDelay());
        if (operation.getUserId() == null) {
            // Operation is anonymous, e.g. for fake SMS authorization, return default response
            logger.info("Resend step result: CONFIRMED (fake SMS), operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            resendResponse.setResult(AuthStepResult.CONFIRMED);
            return resendResponse;
        }
        try {
            AuthorizationOtpDeliveryResult response = sendAuthorizationSms(operation);
            if (response.isDelivered()) {
                updateOtpIdInHttpSession(response.getOtpId());
                updateLastMessageTimestampInHttpSession(System.currentTimeMillis());
                resendResponse.setResult(AuthStepResult.CONFIRMED);
                logger.info("Resend step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            } else {
                resendResponse.setResult(AuthStepResult.AUTH_FAILED);
                resendResponse.setMessage(response.getErrorMessage());
                logger.info("Resend step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            }
            return resendResponse;
        } catch (NextStepClientException ex) {
            logger.error("Error when sending SMS message.", ex);
            resendResponse.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Resend step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            resendResponse.setMessage("smsAuthorization.deliveryFailed");
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
    @PostMapping("/authenticate")
    public SmsAuthorizationResponse authenticateHandler(@Valid @RequestBody SmsAuthorizationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName(operation);
        // Extract username for LOGIN_SCA
        final String username;
        if (authMethod == AuthMethod.LOGIN_SCA) {
            username = getUsernameFromHttpSession();
        } else {
            // In other methods user ID is already available
            username = null;
        }
        final AfsAction afsAction;
        if (configuration.isAfsEnabled()) {
            afsAction = determineAfsActionAuth(authMethod, operation.getOperationName());
        } else {
            afsAction = null;
        }

        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                final List<AfsAuthInstrument> authInstruments = authInstrumentConverter.fromAuthInstruments(request.getAuthInstruments());

                @Override
                public SmsAuthorizationResponse doneAuthentication(String userId) {
                    if (afsAction != null) {
                        afsIntegrationService.executeAuthAction(operation.getOperationId(), afsAction, username, authInstruments,  AuthStepResult.CONFIRMED);
                    }
                    authenticateCurrentBrowserSession();
                    final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    logger.info("Step result: CONFIRMED, authentication method: {}", authMethod);
                    return response;
                }

                @Override
                public SmsAuthorizationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", authMethod);
                    return response;
                }

                @Override
                public SmsAuthorizationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    if (afsAction != null) {
                        afsIntegrationService.executeAuthAction(operation.getOperationId(), afsAction, username, authInstruments, AuthStepResult.CONFIRMED);
                    }
                    final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, authMethod);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            logger.warn("Error occurred while verifying authorization code from SMS message: {}", e.getMessage());
            if (afsAction != null) {
                final List<AfsAuthInstrument> authInstruments = authInstrumentConverter.fromAuthInstruments(request.getAuthInstruments());
                if (e instanceof final AuthenticationFailedException authEx) {
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
            final SmsAuthorizationResponse response = new SmsAuthorizationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Step result: AUTH_FAILED, authentication method: {}", authMethod);
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
    @PostMapping("/cancel")
    public SmsAuthorizationResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            final AuthMethod authMethod = getAuthMethodName(operation);
            cleanHttpSession();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null, true);
            final SmsAuthorizationResponse cancelResponse = new SmsAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.CANCELED);
            cancelResponse.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            return cancelResponse;
        } catch (CommunicationFailedException ex) {
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
     * @return OTP ID for the message.
     * @throws AuthStepException In case OTP configuration is invalid.
     * @throws NextStepClientException In case SMS delivery fails.
     */
    private AuthorizationOtpDeliveryResult sendAuthorizationSms(GetOperationDetailResponse operation) throws NextStepClientException, AuthStepException {
        Long lastMessageTimestamp = getLastMessageTimestampFromHttpSession();
        if (lastMessageTimestamp != null && System.currentTimeMillis() - lastMessageTimestamp < configuration.getSmsResendDelay()) {
            // SMS delivery is not allowed
            AuthorizationOtpDeliveryResult result = new AuthorizationOtpDeliveryResult();
            result.setDelivered(false);
            result.setErrorMessage("smsAuthorization.deliveryFailed");
            return result;
        }
        GetOrganizationDetailResponse organization = nextStepClient.getOrganizationDetail(operation.getOrganizationId()).getResponseObject();
        String otpName = organization.getDefaultOtpName();
        String credentialName = organization.getDefaultCredentialName();
        if (otpName == null) {
            logger.warn("Default OTP name is not configured for organization: " + operation.getOrganizationId());
            throw new AuthStepException("SMS delivery failed", "error.communication");
        }
        String userId = operation.getUserId();
        // OTP data is taken from operation
        try {
            String language = LocaleContextHolder.getLocale().getLanguage();
            CreateAndSendOtpResponse otpResponse = nextStepClient.createAndSendOtp(userId, otpName, credentialName, null, operation.getOperationId(), language).getResponseObject();
            AuthorizationOtpDeliveryResult result = new AuthorizationOtpDeliveryResult();
            result.setDelivered(otpResponse.isDelivered());
            result.setOtpId(otpResponse.getOtpId());
            result.setErrorMessage(otpResponse.getErrorMessage());
            return result;
        } catch (NextStepClientException ex) {
            if (ex.getError() != null
                    && (CredentialNotActiveException.CODE.equals(ex.getError().getCode())
                        || UserNotActiveException.CODE.equals(ex.getError().getCode()))) {
                AuthorizationOtpDeliveryResult result = new AuthorizationOtpDeliveryResult();
                result.setDelivered(false);
                result.setErrorMessage("smsAuthorization.deliveryFailed");
                return result;
            }
            throw ex;
        }
    }

    /**
     * Determine AFS action during initialization.
     * @param authMethod Current authentication method.
     * @param operationName Operation name.
     * @return AFS action.
     * @throws AuthStepException In case of any failure.
     */
    private AfsAction determineAfsActionInit(AuthMethod authMethod, String operationName) throws AuthStepException {
        AfsAction afsAction;
        switch (authMethod) {
            case LOGIN_SCA -> afsAction = AfsAction.LOGIN_INIT;
            case APPROVAL_SCA -> afsAction = AfsAction.APPROVAL_INIT;
            case SMS_KEY -> {
                GetOperationConfigDetailResponse config = getOperationConfig(operationName);
                if (config == null) {
                    throw new OperationNotConfiguredException("Operation not configured, operation name: " + operationName);
                }
                if (OPERATION_CONFIG_TEMPLATE_LOGIN.equals(config.getTemplateId())) {
                    afsAction = AfsAction.LOGIN_INIT;
                } else if (OPERATION_CONFIG_TEMPLATE_APPROVAL.equals(config.getTemplateId())) {
                    afsAction = AfsAction.APPROVAL_INIT;
                } else {
                    // Unknown template, do not execute AFS action
                    afsAction = null;
                }
            }
            default -> afsAction = null;
        }
        return afsAction;
    }

    /**
     * Determine AFS action during authentication.
     * @param authMethod Current authentication method.
     * @param operationName Operation name.
     * @return AFS action.
     * @throws AuthStepException In case of any failure.
     */
    private AfsAction determineAfsActionAuth(AuthMethod authMethod, String operationName) throws AuthStepException {
        AfsAction afsAction;
        switch (authMethod) {
            case LOGIN_SCA -> afsAction = AfsAction.LOGIN_AUTH;
            case APPROVAL_SCA -> afsAction = AfsAction.APPROVAL_AUTH;
            case SMS_KEY -> {
                GetOperationConfigDetailResponse config = getOperationConfig(operationName);
                if (config == null) {
                    throw new OperationNotConfiguredException("Operation not configured, operation name: " + operationName);
                }
                if (OPERATION_CONFIG_TEMPLATE_LOGIN.equals(config.getTemplateId())) {
                    afsAction = AfsAction.LOGIN_AUTH;
                } else if (OPERATION_CONFIG_TEMPLATE_APPROVAL.equals(config.getTemplateId())) {
                    afsAction = AfsAction.APPROVAL_AUTH;
                } else {
                    // Unknown template, do not execute AFS action
                    afsAction = null;
                }
            }
            default -> afsAction = null;
        }
        return afsAction;
    }

    /**
     * Authenticate with signature created by qualified certificate using Data Adapter.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param signedMessage Signed message created using qualified certificate, including the certificate.
     * @param authCode OTP authorization code.
     * @param accountStatus Account status.
     * @param operationContext Operation context.
     * @return Whether authentication succeeded.
     * @throws DataAdapterClientErrorException In case communication with Data Adapter fails.
     * @throws NextStepClientException In case communication with Next Step service fails.
     * @throws AuthStepException In case step authentication fails.
     */
    private AuthResultDetail verifySignatureUsingQualifiedCertificateAndOtp(String operationId, String userId, String organizationId, String signedMessage, String authCode, AuthMethod authMethod, AccountStatus accountStatus, OperationContext operationContext) throws DataAdapterClientErrorException, NextStepClientException, AuthStepException {
        final List<AuthInstrument> authInstruments = Arrays.asList(AuthInstrument.OTP_KEY, AuthInstrument.QUALIFIED_CERTIFICATE);
        // Certificate parameter is null, qualified certificate is included in signedMessage, verification is done using Data Adapter
        final ObjectResponse<VerifyCertificateResponse> objectResponseCert = dataAdapterClient.verifyCertificate(userId, organizationId, null, signedMessage,
                AuthInstrument.QUALIFIED_CERTIFICATE, getAuthMethodName(), accountStatus, operationContext);
        final VerifyCertificateResponse certResponse = objectResponseCert.getResponseObject();
        final CertificateVerificationResult certificateVerificationResult = certResponse.getCertificateVerificationResult();

        // OTP verification is done using Next Step
        final String otpId = getOtpIdFromHttpSession();
        final OtpAuthenticationResponse otpResponse = nextStepClient.authenticateWithOtp(otpId, operationId, authCode, false, authMethod).getResponseObject();
        final AuthenticationResult otpAuthorizationResult = otpResponse.getAuthenticationResult();

        if (otpAuthorizationResult == AuthenticationResult.SUCCEEDED && certificateVerificationResult == CertificateVerificationResult.SUCCEEDED) {
            authorize(operationId, userId, organizationId, authInstruments, null, null);
            cleanHttpSession();
            logger.info("Step authentication succeeded for certificate and OTP verification (2FA), operation ID: {}, authentication method: {}", operationId, authMethod);
            return new AuthResultDetail(userId, organizationId, true, null);
        }
        logger.info("Step authentication failed for certificate and OTP verification (2FA), operation ID: {}, authentication method: {}, certificate verification result: {}, OTP verification result: {}", operationId, getAuthMethodName().toString(), certificateVerificationResult, otpAuthorizationResult);

        if (otpResponse.isOperationFailed()) {
            logger.info("Step authentication maximum attempts reached OTP verification (2FA) due to failed operation, operation ID: {}, authentication method: {}", operationId, authMethod);
            throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
        }

        final AuthOperationResponse response = failAuthorization(operationId, userId, Arrays.asList(AuthInstrument.OTP_KEY, AuthInstrument.QUALIFIED_CERTIFICATE), null, null);
        if (response.getAuthResult() == AuthResult.FAILED) {
            logger.info("Step authentication maximum attempts reached for certificate verification (2FA) due to failed operation, operation ID: {}, authentication method: {}", operationId, authMethod);
            // FAILED result instead of CONTINUE means the authentication method is failed
            throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
        }

        // Merge results of authentication using the two factors
        final Integer remainingAttemptsDA;
        if (certResponse.getRemainingAttempts() != null && otpResponse.getRemainingAttempts() != null) {
            remainingAttemptsDA = Math.min(certResponse.getRemainingAttempts(), otpResponse.getRemainingAttempts());
        } else if (certResponse.getRemainingAttempts() != null) {
            remainingAttemptsDA = certResponse.getRemainingAttempts();
        } else {
            remainingAttemptsDA = otpResponse.getRemainingAttempts();
        }
        final boolean showRemainingAttempts = certResponse.getShowRemainingAttempts() && otpResponse.isShowRemainingAttempts();
        final UserAccountStatus userAccountStatus = statusConverter.fromAccountStatus(certResponse.getAccountStatus());

        String errorMessage = "login.authenticationFailed";
        if (certResponse.getErrorMessage() != null) {
            errorMessage = certResponse.getErrorMessage();
        }
        if (otpResponse.getErrorMessage() != null) {
            errorMessage = otpResponse.getErrorMessage();
        }

        final AuthenticationFailedException authEx = new AuthenticationFailedException("Authentication failed", errorMessage);
        if (showRemainingAttempts) {
            final GetOperationDetailResponse updatedOperation = getOperation();
            final Integer remainingAttemptsNS = updatedOperation.getRemainingAttempts();
            final Integer remainingAttempts = resolveRemainingAttempts(remainingAttemptsDA, remainingAttemptsNS);
            authEx.setRemainingAttempts(remainingAttempts);
        }
        authEx.setAccountStatus(userAccountStatus);
        throw authEx;
    }

    /**
     * Resolve which data should be used for signature with certificate calculation.
     * @param operationData Operation data.
     * @param operationDataExternal Operation data external.
     * @return Signature data in Base-64 format.
     */
    private String resolveDataForSignature(String operationData, String operationDataExternal) {
        if (operationDataExternal != null) {
            // In case operation data external is present, use this data for calculating signature with certificate as is
            return operationDataExternal;
        }
        // Otherwise, convert operation data into Base-64 and use this data for calculating signature with certificate
        return Base64.getEncoder().encodeToString(operationData.getBytes(StandardCharsets.UTF_8));
    }
}
