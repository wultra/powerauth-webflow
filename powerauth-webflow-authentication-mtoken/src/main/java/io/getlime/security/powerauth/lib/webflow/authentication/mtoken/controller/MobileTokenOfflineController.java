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
import io.getlime.security.powerauth.http.PowerAuthHttpBody;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception.QRCodeInvalidDataException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.ActivationEntity;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.OfflineSignatureQrCode;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.QRCodeAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.QRCodeInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QRCodeAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QRCodeInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for offline authorization based on a QR code.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/token/offline")
public class MobileTokenOfflineController extends AuthMethodController<QRCodeAuthenticationRequest, QRCodeAuthenticationResponse, AuthStepException> {

    private final PowerAuthServiceClient powerAuthServiceClient;
    private final AuthMethodQueryService authMethodQueryService;

    @Autowired
    public MobileTokenOfflineController(PowerAuthServiceClient powerAuthServiceClient, AuthMethodQueryService authMethodQueryService) {
        this.powerAuthServiceClient = powerAuthServiceClient;
        this.authMethodQueryService = authMethodQueryService;
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
        if (request.getAuthCode() == null || !request.getAuthCode().matches("^[0-9]{8}-[0-9]{8}$")) {
            throw new AuthStepException("qrCode.invalidAuthCode", new IllegalArgumentException());
        }
        final GetOperationDetailResponse operation = getOperation();
        // nonce and dataHash are received from UI - they were stored together with the QR code
        String nonce = request.getNonce();
        String dataHash = request.getDataHash();
        String data = PowerAuthHttpBody.getSignatureBaseString("POST", "/operation/authorize/offline", BaseEncoding.base64().decode(nonce), BaseEncoding.base64().decode(dataHash));
        VerifyOfflineSignatureResponse signatureResponse = powerAuthServiceClient.verifyOfflineSignature(request.getActivationId(), data, request.getAuthCode(), SignatureType.POSSESSION_KNOWLEDGE);
        if (signatureResponse.isSignatureValid()) {
            String userId = operation.getUserId();
            if (signatureResponse.getUserId().equals(userId)) {
                return userId;
            }
        }
        // otherwise fail authorization
        try {
            UpdateOperationResponse response = failAuthorization(operation.getOperationId(), getOperation().getUserId(), null);
            if (response.getResult() == AuthResult.FAILED) {
                // FAILED result instead of CONTINUE means the authentication method is failed
                throw new AuthStepException("authentication.maxAttemptsExceeded", null);

            }
        } catch (NextStepServiceException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
        throw new AuthStepException("qrCode.invalidAuthCode", null);
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    /**
     * Generates the QR code to be displayed to the user.
     *
     * @return Response with QR code as String-based PNG image.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public QRCodeInitResponse initQRCode(@RequestBody QRCodeInitRequest request) throws QRCodeInvalidDataException, NextStepServiceException, AuthStepException {
        QRCodeInitResponse initResponse = new QRCodeInitResponse();
        final GetOperationDetailResponse operation = getOperation();

        String userId = operation.getUserId();

        // try to get activation from authMethod configuration
        String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(userId);

        if (configuredActivationId == null) {
            // unexpected state - activation is not set or configuration is invalid
            throw new QRCodeInvalidDataException("qrCode.noActivation");
        }

        if (request.getActivationId() != null && !request.getActivationId().equals(configuredActivationId)) {
            // unexpected state - UI requests different activationId than configured activationId
            throw new QRCodeInvalidDataException("qrCode.invalidActivation");

        }

        // get activation status
        GetActivationStatusResponse activationStatusResponse = powerAuthServiceClient.getActivationStatus(configuredActivationId);

        // if activation is not active, fail request
        if (activationStatusResponse.getActivationStatus() != ActivationStatus.ACTIVE) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("qrCode.activationNotActive");
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
        OfflineSignatureQrCode qrCode = generateQRCode(activationEntity);
        initResponse.setQRCode(qrCode.generateImage());
        initResponse.setNonce(qrCode.getNonce());
        initResponse.setDataHash(qrCode.getDataHash());
        initResponse.setChosenActivation(activationEntity);
        // currently the choice of activations is limited only to the configured activation, however list is kept in case we decide in future to re-enable the choice
        initResponse.setActivations(activationEntities);
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
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public QRCodeAuthenticationResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    return response;
                }

                @Override
                public QRCodeAuthenticationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    return response;
                }

                @Override
                public QRCodeAuthenticationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    /**
     * Cancels the QR code authorization.
     *
     * @return Authorization response.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public QRCodeAuthenticationResponse cancelAuthentication() throws AuthStepException {
        try {
            GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), null, OperationCancelReason.UNKNOWN, null);
            final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            return response;
        } catch (NextStepServiceException e) {
            final QRCodeAuthenticationResponse response = new QRCodeAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    /**
     * Generates the QR code based on operation data.
     *
     * @param activation Activation entity.
     * @return QR code as String-based PNG image.
     * @throws QRCodeInvalidDataException Thrown when data is invalid.
     */
    private OfflineSignatureQrCode generateQRCode(ActivationEntity activation) throws QRCodeInvalidDataException, AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        String operationData = operation.getOperationData();
        String messageText = operation.getFormData().getMessage().getValue();

        CreateOfflineSignaturePayloadResponse response = powerAuthServiceClient.createOfflineSignaturePayload(activation.getActivationId(), operationData, messageText);

        if (!response.getData().equals(operationData)) {
            throw new QRCodeInvalidDataException("qrCode.invalidData");
        }
        // do not check message, some sanitization could be done by PowerAuth server

        OfflineSignatureQrCode qrCode = new OfflineSignatureQrCode(250);
        qrCode.setDataHash(response.getDataHash());
        qrCode.setNonce(response.getNonce());
        qrCode.setMessage(response.getMessage());
        qrCode.setSignature(response.getSignature());
        return qrCode;
    }

}
