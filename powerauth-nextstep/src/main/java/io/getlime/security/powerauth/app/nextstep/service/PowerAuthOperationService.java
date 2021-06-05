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
package io.getlime.security.powerauth.app.nextstep.service;

import com.wultra.core.audit.base.Audit;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.request.OperationCreateRequest;
import com.wultra.security.powerauth.client.model.request.OperationDetailRequest;
import com.wultra.security.powerauth.client.model.response.OperationDetailResponse;
import com.wultra.security.powerauth.client.v3.ActivationStatus;
import com.wultra.security.powerauth.client.v3.GetActivationStatusResponse;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.response.GetPAOperationMappingResponse;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for managing operations in PowerAuth server.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class PowerAuthOperationService {

    private static final Logger logger = LoggerFactory.getLogger(PowerAuthOperationService.class);

    private static final String PARAMETER_OPERATION_DATA = "operation_data";

    private final NextStepServerConfiguration nextStepServerConfiguration;
    private final PowerAuthClient powerAuthClient;
    private final DataAdapterClient dataAdapterClient;
    private final Audit audit;

    private final OperationConverter operationConverter = new OperationConverter();

    /**
     * Service constructor.
     * @param nextStepServerConfiguration Next Step server configuration.
     * @param powerAuthClient PowerAuth client.
     * @param dataAdapterClient Data Adapter client.
     * @param audit Audit interface.
     */
    @Autowired
    public PowerAuthOperationService(NextStepServerConfiguration nextStepServerConfiguration, PowerAuthClient powerAuthClient, DataAdapterClient dataAdapterClient, Audit audit) {
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.powerAuthClient = powerAuthClient;
        this.dataAdapterClient = dataAdapterClient;
        this.audit = audit;
    }

    /**
     * Create a PowerAuth operation.
     * @param operation Next Step operation entity.
     * @param activationId Activation ID.
     */
    public String createOperation(OperationEntity operation, String activationId) {
        final boolean operationEnabled = nextStepServerConfiguration.isPowerAuthOperationSupportEnabled();
        if (!operationEnabled) {
            return null;
        }
        final String userId = operation.getUserId();
        String organizationId = null;
        if (operation.getOrganization() != null) {
            organizationId = operation.getOrganization().getOrganizationId();
        }
        try {
            // Check activation status
            final GetActivationStatusResponse status = powerAuthClient.getActivationStatus(activationId);
            if (status.getActivationStatus() != ActivationStatus.ACTIVE) {
                return null;
            }

            // Get operation mapping from Data Adapter
            final OperationContext operationContext = operationConverter.toOperationContext(operation);
            final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
            if (currentHistory == null) {
                throw new OperationNotValidException("Operation is missing history");
            }
            final AuthMethod authMethod = currentHistory.getChosenAuthMethod();
            final GetPAOperationMappingResponse mappingResponse = dataAdapterClient.getPAOperationMapping(userId, organizationId, authMethod, operationContext).getResponseObject();
            final OperationCreateRequest request = new OperationCreateRequest();
            request.setUserId(operation.getUserId());
            request.setApplicationId(status.getApplicationId());
            request.setExternalId(operation.getOperationId());
            request.setTemplateName(mappingResponse.getTemplateName());
            final Map<String, String> parameters = new LinkedHashMap<>();
            parameters.put(PARAMETER_OPERATION_DATA, mappingResponse.getOperationData());
            request.setParameters(parameters);

            // Create PowerAuth operation and store PA operation ID
            final OperationDetailResponse paResponse = powerAuthClient.createOperation(request);
            return paResponse.getId();
        } catch (PowerAuthClientException | DataAdapterClientErrorException | OperationNotValidException ex) {
            logger.warn(ex.getMessage(), ex);
            audit.warn(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Get PowerAuth operation detail.
     * @param operation Operation entity.
     * @return PowerAuth operation detail.
     */
    public OperationDetailResponse getOperationDetail(OperationEntity operation) {
        final boolean operationEnabled = nextStepServerConfiguration.isPowerAuthOperationSupportEnabled();
        if (!operationEnabled) {
            return null;
        }
        final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            // Cannot occur unless data in database is manually manipulated
            return null;
        }
        final boolean mobileTokenActive = currentHistory.isMobileTokenActive();
        final String paOperationId = currentHistory.getPowerAuthOperationId();
        if (!mobileTokenActive || paOperationId == null) {
            return null;
        }

        try {
            final OperationDetailRequest request = new OperationDetailRequest();
            request.setOperationId(paOperationId);

            return powerAuthClient.operationDetail(request);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            audit.warn(ex.getMessage(), ex);
        }
        return null;
    }

}