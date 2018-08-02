package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.powerauth.soap.ActivationStatus;
import io.getlime.powerauth.soap.GetActivationStatusResponse;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.security.powerauth.app.webflow.i18n.I18NService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.ActivationNotActiveException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.ActivationNotAvailableException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for sending push messages.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Component
public class PushMessageService {

    private static final String PUSH_MESSAGE_TYPE = "messageType";
    private static final String PUSH_MESSAGE_TYPE_MTOKEN_INIT = "mtoken.operationInit";
    private static final String PUSH_MESSAGE_TYPE_MTOKEN_FINISHED = "mtoken.operationFinished";
    private static final String PUSH_MESSAGE_FINISHED_RESULT_INFO = "mtokenOperationResult";
    private static final String PUSH_MESSAGE_OPERATION_ID = "operationId";
    private static final String PUSH_MESSAGE_OPERATION_NAME = "operationName";
    private static final String PUSH_MESSAGE_SOUND = "default";

    private final PushServerClient pushServerClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final PowerAuthServiceClient powerAuthServiceClient;
    private final I18NService i18nService;

    /**
     * Service constructor.
     * @param pushServerClient Push server client.
     * @param authMethodQueryService Authentication method query service.
     * @param powerAuthServiceClient PowerAuth service client.
     * @param i18nService I18n service.
     */
    @Autowired
    public PushMessageService(PushServerClient pushServerClient, AuthMethodQueryService authMethodQueryService, PowerAuthServiceClient powerAuthServiceClient, I18NService i18nService) {
        this.pushServerClient = pushServerClient;
        this.authMethodQueryService = authMethodQueryService;
        this.powerAuthServiceClient = powerAuthServiceClient;
        this.i18nService = i18nService;
    }

    /**
     * Send push message when authentication step is initialized.
     * @param operation Operation.
     * @param authMethod Authentication method.
     * @return Mobile token init response.
     * @throws NextStepServiceException Thrown when communication with Next Step service fails.
     */
    public MobileTokenInitResponse sendStepInitPushMessage(GetOperationDetailResponse operation, AuthMethod authMethod) throws NextStepServiceException {
        final MobileTokenInitResponse initResponse = new MobileTokenInitResponse();
        String activationId;
        Long applicationId;
        try {
            activationId = getActivationId(operation);
        } catch (ActivationNotAvailableException e) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.noActivation");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: AUTH_FAILED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), authMethod.toString()});
            return initResponse;
        }

        try {
            applicationId = getApplicationId(activationId);
        } catch (ActivationNotActiveException e) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.activationNotActive");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: AUTH_FAILED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), authMethod.toString()});
            return initResponse;
        }

        try {
            final PushMessage message = createAuthStepInitPushMessage(operation, activationId, authMethod);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Send init push message, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), authMethod.toString()});
            final ObjectResponse<PushMessageSendResult> response = pushServerClient.sendPushMessage(applicationId, message);
            if (response.getStatus().equals(Response.Status.OK)) {
                initResponse.setResult(AuthStepResult.CONFIRMED);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: CONFIRMED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), authMethod.toString()});
            } else {
                initResponse.setResult(AuthStepResult.AUTH_FAILED);
                initResponse.setMessage("pushMessage.fail");
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: AUTH_FAILED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), authMethod.toString()});
            }
        } catch (PushServerClientException ex) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.fail");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: AUTH_FAILED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), authMethod.toString()});
        }
        return initResponse;
    }

    /**
     * Send push message when authentication step is finished.
     * @param operation Operation.
     * @param statusMessage Status message.
     * @param authMethod Authentication method.
     */
    public void sendAuthStepFinishedPushMessage(GetOperationDetailResponse operation, String statusMessage, AuthMethod authMethod) {
        try {
            String activationId = getActivationId(operation);
            PushMessage message = createAuthStepFinishedPushMessage(operation, activationId, statusMessage, authMethod);
            Long applicationId = getApplicationId(activationId);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Send step finished push message, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), authMethod.toString()});
            pushServerClient.sendPushMessage(applicationId, message);
        } catch (Exception ex) {
            // Exception which occurs when push message is sent is not critical, return regular response.
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", ex);
        }
    }

    /**
     * Create an initial push message.
     * @param operation Operation.
     * @param activationId Activation ID.
     * @param authMethod Authentication method.
     * @return Constructed push message.
     */
    private PushMessage createAuthStepInitPushMessage(GetOperationDetailResponse operation, String activationId, AuthMethod authMethod) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Create init push message, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), authMethod.toString()});
        PushMessage message = new PushMessage();
        message.setUserId(operation.getUserId());
        message.setActivationId(activationId);
        message.getAttributes().setPersonal(true);
        message.getAttributes().setEncrypted(true);

        final OperationFormData formData = operation.getFormData();

        PushMessageBody body = new PushMessageBody();
        if (formData != null) {
            body.setTitle(formData.getTitle().getMessage());
            body.setBody(formData.getSummary().getMessage());
        } else {
            AbstractMessageSource messageSource = i18nService.getMessageSource();
            String[] operationData = new String[]{operation.getOperationData()};
            body.setTitle(messageSource.getMessage("push.confirmOperation", null, LocaleContextHolder.getLocale()));
            body.setBody(messageSource.getMessage("push.data", operationData, LocaleContextHolder.getLocale()));
        }
        body.setSound(PUSH_MESSAGE_SOUND);
        body.setCategory(operation.getOperationName());

        // Add information about operation
        Map<String, Object> extras = new HashMap<>();
        extras.put(PUSH_MESSAGE_TYPE, PUSH_MESSAGE_TYPE_MTOKEN_INIT);
        extras.put(PUSH_MESSAGE_OPERATION_ID, operation.getOperationId());
        extras.put(PUSH_MESSAGE_OPERATION_NAME, operation.getOperationName());
        body.setExtras(extras);

        message.setBody(body);
        return message;
    }

    /**
     * Create cancel push message.
     * @param operation Operation.
     * @param activationId Activation ID.
     * @param statusMessage Status message.
     * @param authMethod Authentication method.
     * @return Constructed push message.
     */
    private PushMessage createAuthStepFinishedPushMessage(GetOperationDetailResponse operation, String activationId, String statusMessage, AuthMethod authMethod) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Create step finished push message, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), authMethod.toString()});
        PushMessage message = new PushMessage();
        message.setUserId(operation.getUserId());
        message.setActivationId(activationId);
        message.getAttributes().setPersonal(true);
        message.getAttributes().setEncrypted(true);
        message.getAttributes().setSilent(true);

        // Add information about operation
        Map<String, Object> extras = new HashMap<>();
        extras.put(PUSH_MESSAGE_TYPE, PUSH_MESSAGE_TYPE_MTOKEN_FINISHED);
        extras.put(PUSH_MESSAGE_FINISHED_RESULT_INFO, statusMessage);
        extras.put(PUSH_MESSAGE_OPERATION_ID, operation.getOperationId());
        extras.put(PUSH_MESSAGE_OPERATION_NAME, operation.getOperationName());

        PushMessageBody body = new PushMessageBody();
        body.setExtras(extras);
        body.setCategory(operation.getOperationName());

        message.setBody(body);
        return message;
    }

    /**
     * Resolve activation ID from operation.
     * @param operation Operation.
     * @return Activation ID.
     * @throws NextStepServiceException Throw when communication with Next Step service fails.
     * @throws ActivationNotAvailableException Thrown when activation is not configured.
     */
    private String getActivationId(GetOperationDetailResponse operation) throws NextStepServiceException, ActivationNotAvailableException {
        String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(operation.getUserId());
        if (configuredActivationId == null || configuredActivationId.isEmpty()) {
            throw new ActivationNotAvailableException();
        }
        return configuredActivationId;
    }

    /**
     * Resolve application ID from activation ID.
     * @param activationId Activation ID.
     * @return Application ID.
     * @throws ActivationNotActiveException Thrown when activation is not active.
     */
    private Long getApplicationId(String activationId) throws ActivationNotActiveException {
        GetActivationStatusResponse activationStatusResponse = powerAuthServiceClient.getActivationStatus(activationId);
        if (activationStatusResponse.getActivationStatus() != ActivationStatus.ACTIVE) {
            throw new ActivationNotActiveException();
        }
        return activationStatusResponse.getApplicationId();
    }

}
