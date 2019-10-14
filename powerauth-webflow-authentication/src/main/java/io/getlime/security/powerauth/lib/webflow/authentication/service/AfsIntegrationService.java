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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAction;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAuthInstrument;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.dataadapter.model.request.AfsRequestParameters;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AfsResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AfsActionDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for integration of anti-fraud system.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AfsIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(AfsIntegrationService.class);

    private final WebFlowServicesConfiguration configuration;
    private final NextStepClient nextStepClient;
    private final DataAdapterClient dataAdapterClient;
    private final OperationSessionService operationSessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Service constructor.
     * @param configuration Web Flow configuration.
     * @param nextStepClient Next Step client.
     * @param dataAdapterClient Data Adapter client.
     * @param operationSessionService Operation session service.
     */
    @Autowired
    public AfsIntegrationService(WebFlowServicesConfiguration configuration, NextStepClient nextStepClient, DataAdapterClient dataAdapterClient, OperationSessionService operationSessionService) {
        this.configuration = configuration;
        this.nextStepClient = nextStepClient;
        this.dataAdapterClient = dataAdapterClient;
        this.operationSessionService = operationSessionService;
    }


    /**
     * Execute an anti-fraud system action. This method variant is used during step initialization.
     * The response from AFS is applied in Web Flow.
     *
     * @param operationId Operation ID.
     * @param username Username filled in by the user. Use null in case user is already authenticated.
     * @param afsAction AFS action to be executed.
     * @return Response from anti-fraud system.
     */
    public AfsResponse executeInitAction(String operationId, String username, AfsAction afsAction) {
        return executeAfsAction(operationId, afsAction, Collections.emptyList(), null, username, null);
    }

    /**
     * Execute an anti-fraud system action. This method variant is used during step authentication.
     * The response from AFS has no impact on Web Flow.
     *
     * @param operationId Operation ID.
     * @param afsAction AFS action to be executed.
     * @param username Username filled in by the user. Use null in case user is already authenticated.
     * @param authInstruments Authentication instruments used in this step.
     * @param authStepResult Authentication step result.
     */
    public void executeAuthAction(String operationId, AfsAction afsAction, String username, List<AfsAuthInstrument> authInstruments, AuthStepResult authStepResult) {
        executeAfsAction(operationId, afsAction, authInstruments, authStepResult, username, null);
    }

    /**
     * Execute an anti-fraud system action. This method variant is used during logout.
     * The response from AFS has no impact on Web Flow.
     *
     * @param operationId Operation ID.
     * @param operationTerminationReason Reason why operation was terminated.
     */
    public void executeLogoutAction(String operationId, OperationTerminationReason operationTerminationReason) {
        executeAfsAction(operationId, AfsAction.LOGOUT, Collections.emptyList(), null, null, operationTerminationReason);
    }

    /**
     * Execute a generic anti-fraud system action and return response.
     *
     * @param operationId Operation ID.
     * @param afsAction AFS action to be executed.
     * @param authInstruments Authentication instruments used in this step.
     * @param authStepResult Authentication step result.
     * @param operationTerminationReason Reason why operation was terminated.
     * @return Response from anti-fraud system.
     */
    private AfsResponse executeAfsAction(String operationId, AfsAction afsAction, List<AfsAuthInstrument> authInstruments, AuthStepResult authStepResult, String username, OperationTerminationReason operationTerminationReason) {
        if (configuration.isAfsEnabled()) {
            logger.debug("AFS integration is enabled");
            try {
                // Retrieve operation
                ObjectResponse<GetOperationDetailResponse> operationDetail = nextStepClient.getOperationDetail(operationId);
                GetOperationDetailResponse operation = operationDetail.getResponseObject();
                ObjectResponse<GetOperationConfigDetailResponse> objectResponse = nextStepClient.getOperationConfigDetail(operation.getOperationName());
                GetOperationConfigDetailResponse config = objectResponse.getResponseObject();
                if (config.isAfsEnabled()) {
                    logger.debug("AFS integration is enabled for operation name: {}", operation.getOperationName());
                    // Check that at least one previous AFS operation was triggered before executing LOGOUT action
                    if (afsAction == AfsAction.LOGOUT && !canExecuteLogout(operation)) {
                        logger.debug("AFS action for LOGOUT event is not executed because previous LOGIN_AUTH action is not available for operation: {}", operationId);
                        return new AfsResponse();
                    }
                    // Prepare all AFS request parameters
                    String userId = operation.getUserId();
                    String organizationId = operation.getOrganizationId();
                    FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
                    ApplicationContext applicationContext = operation.getApplicationContext();
                    OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData, applicationContext);
                    AfsType afsType = configuration.getAfsType();
                    String clientIpAddress = operationSessionService.getOperationToSessionMapping(operation.getOperationId()).getClientIp();
                    int stepIndex = deriveStepIndex(operation, afsAction);
                    Map<String, Object> requestAfsExtras = prepareExtrasForAfs(operation);
                    // AuthStepResult is null due to init action
                    AfsRequestParameters afsRequestParameters = new AfsRequestParameters(afsType, afsAction, clientIpAddress, stepIndex, username, authInstruments, authStepResult, operationTerminationReason);
                    logger.info("Executing AFS action: {}, user ID: {}, operation ID: {}", afsAction, operation.getUserId(), operation.getOperationId());
                    ObjectResponse<AfsResponse> afsObjectResponse = dataAdapterClient.executeAfsAction(userId, organizationId, operationContext, afsRequestParameters, requestAfsExtras);
                    AfsResponse response = afsObjectResponse.getResponseObject();
                    // Save ASF request and response in Next Step
                    String requestExtras = convertExtrasToString(requestAfsExtras);
                    String responseExtras = convertExtrasToString(response.getExtras());
                    nextStepClient.createAfsAction(operationId, afsAction.toString(), stepIndex, requestExtras, response.getAfsLabel(), response.isAfsResponseApplied(), responseExtras);
                    logger.debug("AFS action succeeded: {}, user ID: {}, operation ID: {}", afsAction, operation.getUserId(), operation.getOperationId());
                    return response;
                } else {
                    logger.debug("AFS integration is disabled for operation name: {}", operation.getOperationName());
                }

            // AFS errors are not critical, Web Flow falls back to 2FA
            } catch (NextStepServiceException e) {
                logger.error("Error when obtaining operation configuration", e);
            } catch (DataAdapterClientErrorException e) {
                logger.error("Error when calling anti-fraud service", e);
            }
        } else {
            logger.debug("AFS integration is disabled");
        }
        // The default response is not applied
        return new AfsResponse();
    }

    /**
     * Determine whether LOGOUT AFS action is meaningful, a LOGIN_AUTH AFS action within same operation must be already present.
     * @param operation Operation.
     * @return Whether LOGOUT AFS action can be executed.
     */
    private boolean canExecuteLogout(GetOperationDetailResponse operation) {
        if (operation.getAfsActions().isEmpty()) {
            return false;
        }
        for (AfsActionDetail detail: operation.getAfsActions()) {
            if (AfsAction.LOGIN_AUTH.toString().equals(detail.getAction())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Derive step index for current AFS action.
     * @param operation Operation.
     * @param afsAction AFS action.
     * @return Step index of this AFS action.
     */
    private int deriveStepIndex(GetOperationDetailResponse operation, AfsAction afsAction) {
        int stepIndex = 1;
        if (operation.getAfsActions().isEmpty()) {
            return stepIndex;
        }
        for (AfsActionDetail detail: operation.getAfsActions()) {
            if (afsAction.toString().equals(detail.getAction())) {
                stepIndex++;
            }
        }
        return stepIndex;
    }

    /**
     * Prepare extras which are sent with request to AFS. These values are AFS type dependent.
     *
     * @return AFS extras.
     */
    private Map<String, Object> prepareExtrasForAfs(GetOperationDetailResponse operation) {
        Map<String, Object> extras = new LinkedHashMap<>();
        AfsType afsType = configuration.getAfsType();
        if (afsType == AfsType.THREAT_MARK) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (!(requestAttributes instanceof ServletRequestAttributes)) {
                // The action is not dispatched using DispatcherServlet. This occurs in case of processing of the Web
                // Socket close session event. Obtain AFS parameters from last regular request and reuse them.
                List<AfsActionDetail> afsActions = operation.getAfsActions();
                if (!afsActions.isEmpty()) {
                    AfsActionDetail lastAction = afsActions.get(afsActions.size() - 1);
                    // Reuse extras from previous request
                    return lastAction.getRequestExtras();
                }
            } else {
                HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                Cookie[] cookies = servletRequest.getCookies();
                String deviceTagCookie = configuration.getTmDeviceTagCookie();
                String sessionSidCookie = configuration.getTmSessionSidCookie();
                if (cookies != null && deviceTagCookie != null && sessionSidCookie != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals(deviceTagCookie)) {
                            extras.put("tm_device_tag", cookie.getValue());
                        }
                        if (cookie.getName().equals(sessionSidCookie)) {
                            extras.put("tm_session_sid", cookie.getValue());
                        }
                    }
                }
            }
        }
        return extras;
    }

    /**
     * Convert extras map to String.
     * @param extras Extras map.
     * @return String value of extras.
     */
    private String convertExtrasToString(Map<String, Object> extras) {
        try {
            return objectMapper.writeValueAsString(extras);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while serializing data", e);
            return null;
        }
    }

}
