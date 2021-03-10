/*
 * Copyright 2020 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationAfsActionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AfsActionDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converter for operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationConverter {

    private static final Logger logger = LoggerFactory.getLogger(OperationConverter.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert operation entity into operation detail.
     * @param operation Operation entity.
     * @return Operation detail.
     */
    public GetOperationDetailResponse fromEntity(OperationEntity operation) {
        GetOperationDetailResponse operationDetail = new GetOperationDetailResponse();
        operationDetail.setOperationId(operation.getOperationId());
        operationDetail.setOperationName(operation.getOperationName());
        operationDetail.setUserId(operation.getUserId());
        if (operation.getOrganization() != null) {
            operationDetail.setOrganizationId(operation.getOrganization().getOrganizationId());
        }
        operationDetail.setAccountStatus(operation.getUserAccountStatus());
        operationDetail.setExternalTransactionId(operation.getExternalTransactionId());
        operationDetail.setOperationData(operation.getOperationData());
        if (operation.getResult() != null) {
            operationDetail.setResult(operation.getResult());
        }
        assignFormData(operationDetail, operation);
        assignApplicationContext(operationDetail, operation);
        assignOperationHistory(operationDetail, operation);
        assignAfsActions(operationDetail, operation);
        operationDetail.setTimestampCreated(operation.getTimestampCreated());
        operationDetail.setTimestampExpires(operation.getTimestampExpires());
        return operationDetail;
    }

    /**
     * Convert operation entity to operation context.
     * @param operation Operation entity.
     * @return Operation context.
     */
    public OperationContext toOperationContext(OperationEntity operation) {
        String operationId = operation.getOperationId();
        String operationName = operation.getOperationName();
        String operationData = operation.getOperationData();
        GetOperationDetailResponse operationDetail = fromEntity(operation);
        FormData formData = new FormDataConverter().fromOperationFormData(operationDetail.getFormData());
        ApplicationContext applicationContext = operationDetail.getApplicationContext();
        final String externalTransactionId = operation.getExternalTransactionId();
        return new OperationContext(operationId, operationName, operationData, externalTransactionId, formData, applicationContext);
    }

    /**
     * In case operation entity has serialized form data, attempt to deserialize the
     * object and assign it to the response with operation detail.
     * @param response Response to be enriched by operation detail.
     * @param operation Database entity representing operation.
     */
    private void assignFormData(GetOperationDetailResponse response, OperationEntity operation) {
        if (operation.getOperationFormData() != null) {
            OperationFormData formData = null;
            try {
                formData = new ObjectMapper().readValue(operation.getOperationFormData(), OperationFormData.class);
            } catch (IOException ex) {
                logger.error("Error while deserializing operation display formData", ex);
            }
            response.setFormData(formData);
        }
    }

    /**
     * In case operation entity has an application context, assign it to the operation.
     * The application extras are deserialized from JSON.
     * @param response Response to be enriched by application context.
     * @param operation Database entity representing operation.
     */
    private void assignApplicationContext(GetOperationDetailResponse response, OperationEntity operation) {
        if (operation.getApplicationId() != null) {
            ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.setId(operation.getApplicationId());
            applicationContext.setName(operation.getApplicationName());
            applicationContext.setDescription(operation.getApplicationDescription());
            if (operation.getApplicationOriginalScopes() != null) {
                try {
                    JavaType listType = objectMapper.getTypeFactory().constructParametricType(List.class, String.class);
                    List<String> originalScopes = objectMapper.readValue(operation.getApplicationOriginalScopes(), listType);
                    applicationContext.getOriginalScopes().addAll(originalScopes);
                } catch (IOException ex) {
                    logger.error("Error while deserializing application scopes.", ex);
                }
            }
            if (operation.getApplicationExtras() != null) {
                try {
                    JavaType mapType = objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
                    Map<String, Object> extras = objectMapper.readValue(operation.getApplicationExtras(), mapType);
                    applicationContext.getExtras().putAll(extras);
                } catch (IOException ex) {
                    logger.error("Error while deserializing application extras.", ex);
                }
            }
            response.setApplicationContext(applicationContext);
        }
    }

    /**
     * Assign operation history to operation.
     * @param response Response to be enriched by operation history.
     * @param operation Database entity representing operation.
     */
    private void assignOperationHistory(GetOperationDetailResponse response, OperationEntity operation) {
        // add operation history
        for (OperationHistoryEntity history: operation.getOperationHistory()) {
            OperationHistory h = new OperationHistory();
            h.setAuthMethod(history.getRequestAuthMethod());
            h.setRequestAuthStepResult(history.getRequestAuthStepResult());
            h.setAuthResult(history.getResponseResult());
            response.getHistory().add(h);
        }
        // set chosen authentication method
        OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory != null) {
            response.setChosenAuthMethod(currentHistory.getChosenAuthMethod());
        }
    }

    /**
     * Assign AFS actions to operation.
     * @param response Response to be enriched by AFS actions.
     * @param operation Database entity representing operation.
     */
    private void assignAfsActions(GetOperationDetailResponse response, OperationEntity operation) {
        // add AFS actions
        for (OperationAfsActionEntity afsAction: operation.getAfsActions()) {
            AfsActionDetail action = new AfsActionDetail();
            action.setAction(afsAction.getAfsAction());
            action.setStepIndex(afsAction.getStepIndex());
            action.getRequestExtras().putAll(convertExtrasToMap(afsAction.getRequestAfsExtras()));
            action.setAfsResponseApplied(afsAction.isAfsResponseApplied());
            action.setAfsLabel(afsAction.getAfsLabel());
            action.getResponseExtras().putAll(convertExtrasToMap(afsAction.getResponseAfsExtras()));
            response.getAfsActions().add(action);
        }
    }

    /**
     * Convert extras String to map.
     * @param extras String with extras.
     * @return Extras map.
     */
    private Map<String, Object> convertExtrasToMap(String extras) {
        try {
            TypeReference<Map<String, Object>> typeRef
                    = new TypeReference<Map<String, Object>>() {};
            return objectMapper.readValue(extras, typeRef);
        } catch (IOException e) {
            logger.error("Error occurred while deserializing data", e);
            return new HashMap<>();
        }
    }
}
