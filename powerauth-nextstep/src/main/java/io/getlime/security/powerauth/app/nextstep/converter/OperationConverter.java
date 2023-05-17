/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2020 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.audit.base.Audit;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationAfsActionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converter for operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class OperationConverter {

    private static final Logger logger = LoggerFactory.getLogger(OperationConverter.class);

    private final Audit audit;
    private final ObjectMapper objectMapper;

    /**
     * Converter constructor.
     * @param audit Audit interface.
     * @param objectMapper Object mapper.
     */
    @Autowired
    public OperationConverter(Audit audit, ObjectMapper objectMapper) {
        this.audit = audit;
        this.objectMapper = objectMapper;
    }

    /**
     * Convert operation entity into operation detail.
     * @param operation Operation entity.
     * @return Operation detail.
     */
    public GetOperationDetailResponse fromEntity(OperationEntity operation) {
        final GetOperationDetailResponse operationDetail = new GetOperationDetailResponse();
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
        final String operationId = operation.getOperationId();
        final String operationName = operation.getOperationName();
        final String operationData = operation.getOperationData();
        final GetOperationDetailResponse operationDetail = fromEntity(operation);
        final FormData formData = new FormDataConverter().fromOperationFormData(operationDetail.getFormData());
        final ApplicationContext applicationContext = operationDetail.getApplicationContext();
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
                formData = objectMapper.readValue(operation.getOperationFormData(), OperationFormData.class);
            } catch (IOException ex) {
                logger.error("Error while deserializing operation display formData", ex);
                audit.error("Error while deserializing operation display formData", ex);
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
            final ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.setId(operation.getApplicationId());
            applicationContext.setName(operation.getApplicationName());
            applicationContext.setDescription(operation.getApplicationDescription());
            if (operation.getApplicationOriginalScopes() != null) {
                try {
                    final JavaType listType = objectMapper.getTypeFactory().constructParametricType(List.class, String.class);
                    final List<String> originalScopes = objectMapper.readValue(operation.getApplicationOriginalScopes(), listType);
                    applicationContext.getOriginalScopes().addAll(originalScopes);
                } catch (IOException ex) {
                    logger.error("Error while deserializing application scopes.", ex);
                    audit.error("Error while deserializing application scopes.", ex);
                }
            }
            if (operation.getApplicationExtras() != null) {
                try {
                    final JavaType mapType = objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
                    final Map<String, Object> extras = objectMapper.readValue(operation.getApplicationExtras(), mapType);
                    applicationContext.getExtras().putAll(extras);
                } catch (IOException ex) {
                    logger.error("Error while deserializing application extras.", ex);
                    audit.error("Error while deserializing application extras.", ex);
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
            final OperationHistory h = new OperationHistory();
            h.setAuthMethod(history.getRequestAuthMethod());
            h.setRequestAuthStepResult(history.getRequestAuthStepResult());
            h.setAuthStepResultDescription(history.getResponseResultDescription());
            h.setAuthResult(history.getResponseResult());
            h.setMobileTokenActive(history.isMobileTokenActive());
            h.setPowerAuthOperationId(history.getPowerAuthOperationId());
            assignAuthenticationContext(h, history);
            response.getHistory().add(h);
        }
        // set chosen authentication method
        final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory != null) {
            response.setChosenAuthMethod(currentHistory.getChosenAuthMethod());
        }
    }

    /**
     * In case operation history entity has serialized PowerAuth operation context, attempt to deserialize the
     * object and assign it to the operation history in response.
     * @param history Operation history response object.
     * @param historyEntity Operation history entity.
     */
    private void assignAuthenticationContext(OperationHistory history, OperationHistoryEntity historyEntity) {
        if (history != null && historyEntity.getPowerAuthAuthenticationContext() != null) {
            try {
                final PAAuthenticationContext authenticationContext = objectMapper.readValue(historyEntity.getPowerAuthAuthenticationContext(), PAAuthenticationContext.class);
                history.setPaAuthenticationContext(authenticationContext);
            } catch (IOException ex) {
                logger.error("Error while deserializing authentication context", ex);
                audit.error("Error while deserializing authentication context", ex);
            }
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
            final AfsActionDetail action = new AfsActionDetail();
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
            final TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(extras, typeRef);
        } catch (IOException e) {
            logger.error("Error occurred while deserializing data", e);
            audit.error("Error occurred while deserializing data", e);
            return new HashMap<>();
        }
    }
}
