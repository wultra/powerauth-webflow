/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
import io.getlime.powerauth.soap.*;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.http.PowerAuthHttpBody;
import io.getlime.security.powerauth.lib.mtoken.model.entity.AllowedSignatureType;
import io.getlime.security.powerauth.lib.nextstep.model.converter.OperationTextNormalizer;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.OfflineModeDisabledException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.OfflineModeInvalidActivationException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.OfflineModeInvalidAuthCodeException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.OfflineModeMissingActivationException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.converter.OperationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.ActivationEntity;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.OfflineSignatureQRCode;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.QRCodeAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.QRCodeInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QRCodeAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QRCodeInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.PushMessageService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for offline authorization based on a QR code.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/token/offline")
public class MobileTokenOfflineController extends AuthMethodController<QRCodeAuthenticationRequest, QRCodeAuthenticationResponse, AuthStepException> {

    // See: https://github.com/lime-company/powerauth-webflow/wiki/Off-line-Signatures-QR-Code#flags
    private static final String OFFLINE_MODE_ALLOW_BIOMETRY = "B";
    private static final int QR_CODE_SIZE = 250;

    private final PowerAuthServiceClient powerAuthServiceClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final WebFlowServicesConfiguration webFlowServicesConfiguration;
    private final PushMessageService pushMessageService;

    /**
     * Controller constructor.
     * @param powerAuthServiceClient PowerAuth 2.0 service client.
     * @param authMethodQueryService Authentication method query service.
     * @param webFlowServicesConfiguration Web Flow configuration.
     * @param pushMessageService Push message service.
     */
    @Autowired
    public MobileTokenOfflineController(PowerAuthServiceClient powerAuthServiceClient, AuthMethodQueryService authMethodQueryService, WebFlowServicesConfiguration webFlowServicesConfiguration, PushMessageService pushMessageService) {
        this.powerAuthServiceClient = powerAuthServiceClient;
        this.authMethodQueryService = authMethodQueryService;
        this.webFlowServicesConfiguration = webFlowServicesConfiguration;
        this.pushMessageService = pushMessageService;
    }

    /**
     * Verifies the authorization code.
     *
     * @param request Request with authentication object information.
     * @return User ID if successfully authorized, otherwise null.
     * @throws AuthStepException Thrown when authorization step fails.
     */
    @Override
    protected String authenticate(@RequestBody QRCodeAuthenticationRequest request) throws AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        if (request.getAuthCode() == null || !request.getAuthCode().matches("^[0-9]{8}-[0-9]{8}$")) {
            throw new OfflineModeInvalidAuthCodeException("Authorization code is invalid");
        }
        final GetOperationDetailResponse operation = getOperation();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authentication started, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
        checkOperationExpiration(operation);
        // nonce and dataHash are received from UI - they were stored together with the QR code
        String nonce = request.getNonce();
        // data for signature is {OPERATION_ID}&{OPERATION_DATA}
        String data = operation.getOperationId() + '&' + operation.getOperationData();
        String signatureBaseString = PowerAuthHttpBody.getSignatureBaseString("POST", "/operation/authorize/offline", BaseEncoding.base64().decode(nonce), data.getBytes());
        VerifyOfflineSignatureResponse signatureResponse = powerAuthServiceClient.verifyOfflineSignature(request.getActivationId(), signatureBaseString, request.getAuthCode(), SignatureType.POSSESSION_KNOWLEDGE);
        if (signatureResponse.isSignatureValid()) {
            String userId = operation.getUserId();
            if (signatureResponse.getUserId().equals(userId)) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authentication succeeded, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
                return userId;
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
            UpdateOperationResponse response = failAuthorization(operation.getOperationId(), getOperation().getUserId(), null);
            if (response.getResult() == AuthResult.FAILED) {
                // FAILED result instead of CONTINUE means the authentication method is failed
                throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
            }
            GetOperationDetailResponse updatedOperation = getOperation();
            remainingAttemptsNS = updatedOperation.getRemainingAttempts();
        } catch (NextStepServiceException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
        OfflineModeInvalidAuthCodeException authEx = new OfflineModeInvalidAuthCodeException("Authorization code is invalid.");
        Integer remainingAttempts = resolveRemainingAttempts(remainingAttemptsPA, remainingAttemptsNS);
        authEx.setRemainingAttempts(remainingAttempts);
        throw authEx;
    }

    /**
     * Get current method name.
     * @return Current method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    /**
     * Generates the QR code to be displayed to the user.
     * @param request QR code init request.
     * @return Response with QR code as String-based PNG image.
     * @throws NextStepServiceException In case communication with Next Step service fails.
     * @throws AuthStepException In case authorization fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public QRCodeInitResponse initQRCode(@RequestBody QRCodeInitRequest request) throws NextStepServiceException, AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        QRCodeInitResponse initResponse = new QRCodeInitResponse();
        final GetOperationDetailResponse operation = getOperation();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step started, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
        checkOperationExpiration(operation);

        String userId = operation.getUserId();

        // try to get activation from authMethod configuration
        String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(userId);

        if (configuredActivationId == null) {
            // unexpected state - activation is not set or configuration is invalid
            throw new OfflineModeMissingActivationException("Activation is not configured");
        }

        if (request.getActivationId() != null && !request.getActivationId().equals(configuredActivationId)) {
            // unexpected state - UI requests different activationId than configured activationId
            throw new OfflineModeInvalidActivationException("Activation is invalid");
        }

        // get activation status
        GetActivationStatusResponse activationStatusResponse = powerAuthServiceClient.getActivationStatus(configuredActivationId);

        // if activation is not active, fail request
        if (activationStatusResponse.getActivationStatus() != ActivationStatus.ACTIVE) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("offlineMode.activationNotActive");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: AUTH_FAILED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), getAuthMethodName().toString()});
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
        OfflineSignatureQRCode qrCode = generateQRCode(activationEntity);
        initResponse.setQRCode(qrCode.generateImage());
        initResponse.setNonce(qrCode.getNonce());
        initResponse.setChosenActivation(activationEntity);
        // currently the choice of activations is limited only to the configured activation, however list is kept in case we decide in future to re-enable the choice
        initResponse.setActivations(activationEntities);
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Init step succeeded, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
        return initResponse;
    }

    /**
     * Performs the authorization and resolves the next step.
     *
     * @param request Request to verify authorization code based on QR code.
     * @return Authorization response.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    @ResponseBody
    public QRCodeAuthenticationResponse verifyAuthCode(@RequestBody QRCodeAuthenticationRequest request) {
        try {
            GetOperationDetailResponse operation = getOperation();
            checkOperationExpiration(operation);
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public QRCodeAuthenticationResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), getAuthMethodName());
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CONFIRMED, authentication method: {0}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public QRCodeAuthenticationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public QRCodeAuthenticationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), getAuthMethodName());
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CONFIRMED, operation ID: {0}, authentication method: {1}", new String[]{operationId, getAuthMethodName().toString()});
                    return response;
                }
            });
        } catch (AuthStepException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error occurred while verifying offline authorization code: {0}", e.getMessage());
            final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
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
    public QRCodeAuthenticationResponse cancelAuthentication() throws AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        try {
            GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), getAuthMethodName());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CANCELED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), getAuthMethodName().toString()});
            return response;
        } catch (NextStepServiceException e) {
            final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
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
    private OfflineSignatureQRCode generateQRCode(ActivationEntity activation) throws AuthStepException {
        if (!webFlowServicesConfiguration.isOfflineModeAvailable()) {
            throw new OfflineModeDisabledException("Offline mode is disabled");
        }
        GetOperationDetailResponse operation = getOperation();
        checkOperationExpiration(operation);
        String operationId = operation.getOperationId();
        String operationData = operation.getOperationData();
        String operationName = operation.getOperationName();

        OperationTextNormalizer operationTextNormalizer = new OperationTextNormalizer();

        String title = operationTextNormalizer.normalizeText(operation.getFormData().getTitle().getMessage());
        String message = operationTextNormalizer.normalizeText(operation.getFormData().getSummary().getMessage());

        // Convert mobile token mode to AllowedSignatureType object
        GetOperationConfigResponse operationConfig = getOperationConfig(operationName);
        OperationConverter operationConverter = new OperationConverter();
        AllowedSignatureType allowedSignatureType = operationConverter.fromMobileTokenMode(operationConfig.getMobileTokenMode());

        // Set flags based on signature type variants
        String flags = "";
        if (allowedSignatureType != null && allowedSignatureType.getVariants() != null
                && allowedSignatureType.getVariants().contains(PowerAuthSignatureTypes.POSSESSION_BIOMETRY.toString())) {
            flags = OFFLINE_MODE_ALLOW_BIOMETRY;
        }

        // Construct offline signature data payload as {OPERATION_ID}\n{TITLE}\n{MESSAGE}\n{OPERATION_DATA}\n{FLAGS}
        String data = operationId+"\n"+title+"\n"+message+"\n"+operationData+"\n"+flags;

        CreatePersonalizedOfflineSignaturePayloadResponse response = powerAuthServiceClient.createPersonalizedOfflineSignaturePayload(activation.getActivationId(), data);

        return new OfflineSignatureQRCode(QR_CODE_SIZE, response.getOfflineData(), response.getNonce());
    }

}
