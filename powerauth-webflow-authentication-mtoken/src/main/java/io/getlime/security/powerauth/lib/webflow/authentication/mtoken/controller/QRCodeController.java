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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.BaseEncoding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import io.getlime.powerauth.soap.CreateOfflineSignaturePayloadResponse;
import io.getlime.powerauth.soap.GetActivationListForUserResponse;
import io.getlime.powerauth.soap.SignatureType;
import io.getlime.powerauth.soap.VerifyOfflineSignatureResponse;
import io.getlime.security.powerauth.http.PowerAuthHttpBody;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.QRCodeAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QRCodeAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.QRCodeInitResponse;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for offline authorization based on a QR code.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/qr")
public class QRCodeController extends AuthMethodController<QRCodeAuthenticationRequest, QRCodeAuthenticationResponse, AuthStepException> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PowerAuthServiceClient powerAuthServiceClient;

    // TODO - store in form
    private static String nonce = null;
    private static String dataHashed = null;

    /**
     * Verifies the authorization code.
     *
     * @param request Request with authentication object information.
     * @return User ID if successfully authorized, otherwise null.
     * @throws AuthStepException Thrown when authorization step fails.
     */
    @Override
    protected String authenticate(QRCodeAuthenticationRequest request) throws AuthStepException {
        // TODO - activation choice, for now we use first activation found
        // TODO - filter out inactive activations
        List<GetActivationListForUserResponse.Activations> activations = powerAuthServiceClient.getActivationListForUser(getOperation().getUserId());
        GetActivationListForUserResponse.Activations activation = activations.get(0);
        String data = PowerAuthHttpBody.getSignatureBaseString("POST", "/operation/authorize/offline", BaseEncoding.base64().decode(nonce), BaseEncoding.base64().decode(dataHashed));
        VerifyOfflineSignatureResponse signatureResponse = powerAuthServiceClient.verifyOfflineSignature(activation.getActivationId(), data, request.getAuthCode(), SignatureType.POSSESSION_KNOWLEDGE);
        if (signatureResponse.isSignatureValid()) {
            String userId = getOperation().getUserId();
            if (signatureResponse.getUserId().equals(userId)) {
                return userId;
            }
        }
        // otherwise fail authorization
        try {
            UpdateOperationResponse response = failAuthorization(getOperation().getOperationId(), getOperation().getUserId(), null);
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
     * @throws IOException Thrown when generating QR code fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public @ResponseBody
    QRCodeInitResponse initQRCode() throws IOException {
        QRCodeInitResponse initResponse = new QRCodeInitResponse();
        initResponse.setQRCode(generateQRCode());
        return initResponse;
    }

    /**
     * Performs the authorization and resolves the next step.
     *
     * @param request Request to verify authorization code based on QR code.
     * @return Authorization response.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody
    QRCodeAuthenticationResponse verifyAuthCode(@RequestBody QRCodeAuthenticationRequest request) {
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
    public @ResponseBody
    QRCodeAuthenticationResponse cancelAuthentication() {
        try {
            cancelAuthorization(getOperation().getOperationId(), null, OperationCancelReason.UNKNOWN, null);
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
     * @return QR code as String-based PNG image.
     * @throws IOException Thrown when generating QR code fails.
     */
    private String generateQRCode() throws IOException {
        // TODO - activation choice, for now we use first activation found
        List<GetActivationListForUserResponse.Activations> activations = powerAuthServiceClient.getActivationListForUser(getOperation().getUserId());
        GetActivationListForUserResponse.Activations activation = activations.get(0);

        GetOperationDetailResponse operation = getOperation();
        String operationData = operation.getOperationData();
        String messageText = generateMessageText(operation.getFormData());

        CreateOfflineSignaturePayloadResponse response = powerAuthServiceClient.createOfflineSignaturePayload(activation.getActivationId(), operationData, messageText);

        // TODO - check operationData, it should match

        // TODO - save into form as hidden field
        nonce = response.getNonce();
        dataHashed = response.getDataHash();

        String qrCodeData = generateJsonDataForQRCode(response.getDataHash(), response.getNonce(), response.getMessage(), response.getSignature());
        return encodeQRCode(qrCodeData, 400);
    }

    /**
     * Generates the localized message for operation data.
     *
     * @param formData Operation form data.
     * @return Localized message.
     * @throws IOException Thrown when generating message fails.
     */
    private String generateMessageText(OperationFormData formData) throws IOException {
        BigDecimal amount = null;
        String currency = null;
        String account = null;
        for (OperationFormAttribute attribute: formData.getParameters()) {
            switch (attribute.getType()) {
                case AMOUNT:
                    OperationAmountAttribute amountAttribute = (OperationAmountAttribute) attribute;
                    amount = amountAttribute.getAmount();
                    currency = amountAttribute.getCurrency();
                    break;
                case KEY_VALUE:
                    OperationKeyValueAttribute keyValueAttribute = (OperationKeyValueAttribute) attribute;
                    if ("To Account".equals(keyValueAttribute.getLabel())) {
                        account = keyValueAttribute.getValue();
                    }
                    break;
            }
        }
        if (amount==null || amount.doubleValue()<=0) {
            throw new IllegalStateException("Invalid amount in formData: "+amount);
        }
        if (currency==null || currency.isEmpty()) {
            throw new IllegalStateException("Missing currency in formData.");
        }
        if (account==null || account.isEmpty()) {
            throw new IllegalStateException("Missing account in formData.");
        }
        String[] messageArgs = {amount.toPlainString(), currency, account};
        return messageSource().getMessage("qrCode.messageText", messageArgs, LocaleContextHolder.getLocale());
    }

    /**
     * Generates data for the QR code.
     *
     * @param operationDataHash Hash of operation data.
     * @param randomBytes       Random bytes.
     * @param messageText       Message based on the operation data.
     * @param signature         Signature of the QR code.
     * @return Data for the QR code.
     */
    private String generateJsonDataForQRCode(String operationDataHash, String randomBytes, String messageText, String signature) throws JsonProcessingException {
        ObjectNode qrNode = JsonNodeFactory.instance.objectNode();
        qrNode.put("dt", operationDataHash);
        qrNode.put("rnd", randomBytes);
        qrNode.put("msg", messageText);
        qrNode.put("sig", signature);
        return objectMapper.writeValueAsString(qrNode);
    }

    /**
     * Encodes the QR code data into a String-based PNG image.
     *
     * @param qrCodeData QR code data.
     * @param qrCodeSize Image width and height.
     * @return Encoded QR code as image.
     */
    private String encodeQRCode(String qrCodeData, int qrCodeSize) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(qrCodeData.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE,
                    qrCodeSize,
                    qrCodeSize);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + BaseEncoding.base64().encode(bytes);
        } catch (WriterException | IOException e) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE,
                    "Error occurred while generating QR code",
                    e
            );
        }
        return null;
    }

    /**
     * Get MessageSource with i18n data for authorizations SMS messages.
     *
     * @return MessageSource.
     */
    @Bean
    private MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/static/resources/messages");
        return messageSource;
    }

}
