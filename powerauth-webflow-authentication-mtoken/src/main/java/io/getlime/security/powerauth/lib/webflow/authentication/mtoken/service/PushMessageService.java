/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.enumeration.ActivationStatus;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.response.GetActivationStatusResponse;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import com.wultra.push.client.PushServerClient;
import com.wultra.push.client.PushServerClientException;
import com.wultra.push.model.entity.PushMessage;
import com.wultra.push.model.entity.PushMessageBody;
import com.wultra.push.model.entity.PushMessageSendResult;
import io.getlime.security.powerauth.app.webflow.i18n.I18NService;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.ActivationNotActiveException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.ActivationNotConfiguredException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending push messages.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class PushMessageService {

    private final Logger logger = LoggerFactory.getLogger(PushMessageService.class);

    private static final String PUSH_MESSAGE_TYPE = "messageType";
    private static final String PUSH_MESSAGE_TYPE_MTOKEN_INIT = "mtoken.operationInit";
    private static final String PUSH_MESSAGE_TYPE_MTOKEN_FINISHED = "mtoken.operationFinished";
    private static final String PUSH_MESSAGE_FINISHED_RESULT_INFO = "mtokenOperationResult";
    private static final String PUSH_MESSAGE_OPERATION_ID = "operationId";
    private static final String PUSH_MESSAGE_OPERATION_NAME = "operationName";
    private static final String PUSH_MESSAGE_SOUND = "default";

    private final PushServerClient pushServerClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final PowerAuthClient powerAuthClient;
    private final I18NService i18nService;

    /**
     * Service constructor.
     * @param pushServerClient Push server client.
     * @param authMethodQueryService Authentication method query service.
     * @param powerAuthClient PowerAuth service client.
     * @param i18nService I18n service.
     */
    @Autowired
    public PushMessageService(PushServerClient pushServerClient, AuthMethodQueryService authMethodQueryService, PowerAuthClient powerAuthClient, I18NService i18nService) {
        this.pushServerClient = pushServerClient;
        this.authMethodQueryService = authMethodQueryService;
        this.powerAuthClient = powerAuthClient;
        this.i18nService = i18nService;
    }

    /**
     * Send push message when authentication step is initialized.
     * @param operation Operation.
     * @param authMethod Authentication method.
     * @return Mobile token init response.
     */
    public MobileTokenInitResponse sendStepInitPushMessage(GetOperationDetailResponse operation, AuthMethod authMethod) {
        final MobileTokenInitResponse initResponse = new MobileTokenInitResponse();
        String activationId;
        String applicationId;
        try {
            activationId = getActivationId(operation);
        } catch (NextStepClientException | ActivationNotConfiguredException ex) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.noActivation");
            logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            return initResponse;
        }

        try {
            applicationId = getApplicationId(activationId);
        } catch (ActivationNotActiveException ex) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.activationNotActive");
            logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            return initResponse;
        }

        try {
            final PushMessage message = createAuthStepInitPushMessage(operation, activationId, authMethod);
            logger.info("Send init push message, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            final ObjectResponse<PushMessageSendResult> response = pushServerClient.sendPushMessage(applicationId, message);
            if (response.getStatus().equals(Response.Status.OK)) {
                initResponse.setResult(AuthStepResult.CONFIRMED);
                logger.info("Init step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            } else {
                initResponse.setResult(AuthStepResult.AUTH_FAILED);
                initResponse.setMessage("pushMessage.fail");
                logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            }
        } catch (PushServerClientException ex) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.fail");
            logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
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
            String applicationId = getApplicationId(activationId);
            logger.info("Send step finished push message, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
            pushServerClient.sendPushMessage(applicationId, message);
        } catch (PushServerClientException ex) {
            logger.info("Sending step finish push message failed for operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
        } catch (Exception ex) {
            // Exception which occurs when push message is sent is not critical, only log warning.
            logger.warn("Error occurred in Mobile Token API component", ex);
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
        logger.info("Create init push message, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
        PushMessage message = new PushMessage();
        message.setUserId(operation.getUserId());
        message.setActivationId(activationId);
        message.getAttributes().setPersonal(true);

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
        logger.info("Create step finished push message, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod);
        PushMessage message = new PushMessage();
        message.setUserId(operation.getUserId());
        message.setActivationId(activationId);
        message.getAttributes().setPersonal(true);
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
     * @throws NextStepClientException Throw when communication with Next Step service fails.
     * @throws ActivationNotConfiguredException Thrown when activation is not configured.
     */
    private String getActivationId(GetOperationDetailResponse operation) throws NextStepClientException, ActivationNotConfiguredException {
        String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(operation.getUserId());
        if (configuredActivationId == null || configuredActivationId.isEmpty()) {
            throw new ActivationNotConfiguredException();
        }
        return configuredActivationId;
    }

    /**
     * Resolve application ID from activation ID.
     * @param activationId Activation ID.
     * @return Application ID.
     * @throws ActivationNotActiveException Thrown when activation is not active.
     */
    private String getApplicationId(String activationId) throws ActivationNotActiveException {
        GetActivationStatusResponse activationStatusResponse;
        try {
            activationStatusResponse = powerAuthClient.getActivationStatus(activationId);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new ActivationNotActiveException(activationId);
        }
        if (activationStatusResponse.getActivationStatus() != ActivationStatus.ACTIVE) {
            throw new ActivationNotActiveException(activationId);
        }
        return activationStatusResponse.getApplicationId();
    }

}
