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

package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.controller;

import com.google.common.io.BaseEncoding;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.v3.ActivationStatus;
import com.wultra.security.powerauth.client.v3.CreatePersonalizedOfflineSignaturePayloadResponse;
import com.wultra.security.powerauth.client.v3.GetActivationStatusResponse;
import com.wultra.security.powerauth.client.v3.VerifyOfflineSignatureResponse;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.http.PowerAuthHttpBody;
import io.getlime.security.powerauth.lib.mtoken.model.entity.AllowedSignatureType;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.converter.OperationTextNormalizer;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.OperationIsAlreadyFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthResultDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.*;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.converter.OperationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.ActivationEntity;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.OfflineSignatureQrCode;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.QrCodeAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.QrCodeInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QrCodeAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QrCodeInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.PushMessageService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.PowerAuthOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for offline authorization based on a QR code.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/token/offline")
public class MobileTokenOfflineController extends AuthMethodController<QrCodeAuthenticationRequest, QrCodeAuthenticationResponse, AuthStepException> {

    private final Logger logger = LoggerFactory.getLogger(MobileTokenOfflineController.class);

    // See: https://developers.wultra.com/docs/develop/powerauth-webflow/Off-line-Signatures-QR-Code#flags
    private static final String OFFLINE_MODE_ALLOW_BIOMETRY = "B";
    private static final int QR_CODE_SIZE = 250;

    private final PowerAuthClient powerAuthClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final WebFlowServicesConfiguration webFlowServicesConfiguration;
    private final PushMessageService pushMessageService;
    private final PowerAuthOperationService powerAuthOperationService;
    private final HttpSession httpSession;

    /**
     * Controller constructor.
     * @param powerAuthClient PowerAuth 2.0 service client.
     * @param authMethodQueryService Authentication method query service.
     * @param webFlowServicesConfiguration Web Flow configuration.
     * @param pushMessageService Push message service.
     * @param powerAuthOperationService PowerAuth operation service.
     * @param httpSession HTTP session.
     */
    @Autowired
    public MobileTokenOfflineController(PowerAuthClient powerAuthClient, AuthMethodQueryService authMethodQueryService, WebFlowServicesConfiguration webFlowServicesConfiguration, PushMessageService pushMessageService, PowerAuthOperationService powerAuthOperationService, HttpSession httpSession) {
        this.powerAuthClient = powerAuthClient;
        this.authMethodQueryService = authMethodQueryService;
        this.webFlowServicesConfiguration = webFlowServicesConfiguration;
        this.pushMessageService = pushMessageService;
        this.powerAuthOperationService = powerAuthOperationService;
        this.httpSession = httpSession;
    }

    /**
     * Verifies the authorization code.
     *
     * @param request Request with authentication object information.
     * @return Authentication result with user ID and organization ID.
     * @throws AuthStepException Thrown when authorization step fails.
     */
    @Override
    protected AuthResultDetail authenticate(@RequestBody QrCodeAuthenticationRequest request) throws AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        if (request.getAuthCode() == null || !request.getAuthCode().matches("^[0-9]{8}-[0-9]{8}$")) {
            throw new OfflineModeInvalidAuthCodeException("Authorization code is invalid");
        }
        final GetOperationDetailResponse operation = getOperation();
        final String operationName = operation.getOperationName();
        final AuthMethod authMethod = getAuthMethodName(operation);
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
        // nonce is received from the UI - it was stored together with the QR code
        String nonce = request.getNonce();
        // data for signature is {OPERATION_ID}&{OPERATION_DATA}
        String data = operation.getOperationId() + '&' + operation.getOperationData();
        String signatureBaseString = PowerAuthHttpBody.getSignatureBaseString("POST", "/operation/authorize/offline", BaseEncoding.base64().decode(nonce), data.getBytes());
        // determine whether biometry is allowed in offline mode
        boolean biometryAllowed = isBiometryAllowedInOfflineMode(operationName);
        VerifyOfflineSignatureResponse signatureResponse;
        try {
            signatureResponse = powerAuthClient.verifyOfflineSignature(request.getActivationId(), signatureBaseString, request.getAuthCode(), biometryAllowed);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new OfflineModeInvalidAuthCodeException("Offline signature verification failed, reason: " + ex.getMessage());
        }
        if (signatureResponse.isSignatureValid()) {
            String userId = operation.getUserId();
            if (signatureResponse.getUserId().equals(userId)) {
                boolean approvalSucceeded = powerAuthOperationService.approveOperation(operation, signatureResponse.getActivationId(), signatureResponse.getSignatureType().toString());
                if (!approvalSucceeded) {
                    throw new OperationIsAlreadyFailedException("Operation approval has failed");
                }
                cleanHttpSession();
                logger.info("Step authentication succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                return new AuthResultDetail(userId, operation.getOrganizationId(), false);
            }
        } else {
            boolean approvalFailSucceeded = powerAuthOperationService.failApprovalForOperation(operation);
            if (!approvalFailSucceeded) {
                throw new OperationIsAlreadyFailedException("Operation failed approval has failed");
            }
        }
        BigInteger remainingAttemptsPAObj = signatureResponse.getRemainingAttempts();
        Integer remainingAttemptsPA = null;
        if (remainingAttemptsPAObj != null) {
            remainingAttemptsPA = remainingAttemptsPAObj.intValue();
        }
        // otherwise fail authorization
        Integer remainingAttemptsNS;
        try {
            AuthOperationResponse response = failAuthorization(operation.getOperationId(), getOperation().getUserId(), request.getAuthInstruments(), null);
            if (response.getAuthResult() == AuthResult.FAILED) {
                // FAILED result instead of CONTINUE means the authentication method is failed
                cleanHttpSession();
                throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
            }
            GetOperationDetailResponse updatedOperation = getOperation();
            remainingAttemptsNS = updatedOperation.getRemainingAttempts();
        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
            throw new CommunicationFailedException("Authorization failed due to communication error");
        }
        OfflineModeInvalidAuthCodeException authEx = new OfflineModeInvalidAuthCodeException("Authorization code is invalid");
        Integer remainingAttempts = resolveRemainingAttempts(remainingAttemptsPA, remainingAttemptsNS);
        authEx.setRemainingAttempts(remainingAttempts);
        throw authEx;
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    /**
     * Generates the QR code to be displayed to the user.
     * @param request QR code init request.
     * @return Response with QR code as String-based PNG image.
     * @throws AuthStepException In case authorization fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public QrCodeInitResponse initQrCode(@RequestBody QrCodeInitRequest request) throws AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        QrCodeInitResponse initResponse = new QrCodeInitResponse();
        final GetOperationDetailResponse operation = getOperation();
        final AuthMethod authMethod = getAuthMethodName();
        logger.info("Init step started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());

        String userId = operation.getUserId();

        // try to get activation from authMethod configuration
        String configuredActivationId;
        try {
            configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(userId);
        } catch (NextStepClientException ex) {
            logger.error(ex.getMessage(), ex);
            throw new OfflineModeMissingActivationException("Activation configuration is not available");
        }

        if (configuredActivationId == null) {
            // unexpected state - activation is not set or configuration is invalid
            throw new OfflineModeMissingActivationException("Activation is not configured");
        }

        if (request.getActivationId() != null && !request.getActivationId().equals(configuredActivationId)) {
            // unexpected state - UI requests different activationId than configured activationId
            throw new OfflineModeInvalidActivationException("Activation is invalid");
        }

        // get activation status
        GetActivationStatusResponse activationStatusResponse;
        try {
            activationStatusResponse = powerAuthClient.getActivationStatus(configuredActivationId);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("offlineMode.noActivation");
            return initResponse;
        }

        // if activation is not active, fail request
        if (activationStatusResponse.getActivationStatus() != ActivationStatus.ACTIVE) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("offlineMode.activationNotActive");
            logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return initResponse;
        }

        // transfer activation into ActivationEntity list
        List<ActivationEntity> activationEntities = new ArrayList<>();
        ActivationEntity activationEntity = new ActivationEntity();
        activationEntity.setActivationId(activationStatusResponse.getActivationId());
        activationEntity.setActivationName(activationStatusResponse.getActivationName());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date timestampLastUsed = activationStatusResponse.getTimestampLastUsed().toGregorianCalendar().getTime();
        activationEntity.setTimestampLastUsed(formatter.format(timestampLastUsed));
        activationEntities.add(activationEntity);

        // generating of QR code
        OfflineSignatureQrCode qrCode = generateQrCode(activationEntity);
        initResponse.setQrCode(qrCode.generateImage());
        initResponse.setNonce(qrCode.getNonce());
        initResponse.setChosenActivation(activationEntity);
        // currently the choice of activations is limited only to the configured activation, however list is kept in case we decide in future to re-enable the choice
        initResponse.setActivations(activationEntities);
        logger.debug("Init step succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
        return initResponse;
    }

    /**
     * Performs the authorization and resolves the next step.
     *
     * @param request Request to verify authorization code based on QR code.
     * @return Authorization response.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    @ResponseBody
    public QrCodeAuthenticationResponse verifyAuthCode(@RequestBody QrCodeAuthenticationRequest request) throws AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        AuthMethod authMethod = getAuthMethodName(operation);
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public QrCodeAuthenticationResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final QrCodeAuthenticationResponse response = new QrCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
                    cleanHttpSession();
                    logger.info("Step result: CONFIRMED, authentication method: {}", authMethod.toString());
                    return response;
                }

                @Override
                public QrCodeAuthenticationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final QrCodeAuthenticationResponse response = new QrCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", authMethod.toString());
                    return response;
                }

                @Override
                public QrCodeAuthenticationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final QrCodeAuthenticationResponse response = new QrCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
                    cleanHttpSession();
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, authMethod.toString());
                    return response;
                }
            });
        } catch (AuthStepException e) {
            logger.warn("Error occurred while verifying offline authorization code: {}", e.getMessage());
            final QrCodeAuthenticationResponse response = new QrCodeAuthenticationResponse();
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
     * Cancels the QR code authorization.
     *
     * @return Authorization response.
     * @throws AuthStepException In case authorization fails.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public QrCodeAuthenticationResponse cancelAuthentication() throws AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        try {
            GetOperationDetailResponse operation = getOperation();
            AuthMethod authMethod = getAuthMethodName(operation);
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null, true);
            final QrCodeAuthenticationResponse response = new QrCodeAuthenticationResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
            cleanHttpSession();
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return response;
        } catch (CommunicationFailedException ex) {
            final QrCodeAuthenticationResponse response = new QrCodeAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            cleanHttpSession();
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Generates the QR code based on operation data.
     *
     * @param activation Activation entity.
     * @return QR code as String-based PNG image.
     * @throws AuthStepException In case QR code generation fails.
     */
    private OfflineSignatureQrCode generateQrCode(ActivationEntity activation) throws AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        GetOperationDetailResponse operation = getOperation();
        String operationId = operation.getOperationId();
        String operationData = operation.getOperationData();
        String operationName = operation.getOperationName();

        OperationTextNormalizer operationTextNormalizer = new OperationTextNormalizer();

        String title = operationTextNormalizer.normalizeText(operation.getFormData().getTitle().getMessage());
        String message = operationTextNormalizer.normalizeText(operation.getFormData().getSummary().getMessage());

        String flags = "";
        if (isBiometryAllowedInOfflineMode(operationName)) {
            flags = OFFLINE_MODE_ALLOW_BIOMETRY;
        }

        // Construct offline signature data payload as {OPERATION_ID}\n{TITLE}\n{MESSAGE}\n{OPERATION_DATA}\n{FLAGS}
        String data = operationId+"\n"+title+"\n"+message+"\n"+operationData+"\n"+flags;

        CreatePersonalizedOfflineSignaturePayloadResponse response;
        try {
            response = powerAuthClient.createPersonalizedOfflineSignaturePayload(activation.getActivationId(), data);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new OfflineModeInvalidDataException("Could not generate QR code");
        }

        return new OfflineSignatureQrCode(QR_CODE_SIZE, response.getOfflineData(), response.getNonce());
    }

    /**
     * Determine whether biometry is allowed in offline mode.
     * @param operationName Operation name.
     * @return Whether biometry is allowed in offline mode.
     * @throws AuthStepException In case communication with Next Step service fails.
     */
    private boolean isBiometryAllowedInOfflineMode(String operationName) throws AuthStepException {
        GetOperationConfigDetailResponse operationConfig = getOperationConfig(operationName);
        if (operationConfig != null) {
            // Convert mobile token mode to AllowedSignatureType object
            OperationConverter operationConverter = new OperationConverter();
            AllowedSignatureType allowedSignatureType = operationConverter.fromMobileTokenMode(operationConfig.getMobileTokenMode());
            // Return whether biometry is allowed in offline mode based on signature type variants
            return allowedSignatureType != null && allowedSignatureType.getVariants() != null
                    && allowedSignatureType.getVariants().contains(PowerAuthSignatureTypes.POSSESSION_BIOMETRY.toString());
        }
        return false;
    }

    /**
     * Clean HTTP session.
     */
    private void cleanHttpSession() {
        synchronized (httpSession.getServletContext()) {
            httpSession.removeAttribute(HttpSessionAttributeNames.USERNAME);
        }
    }
}
