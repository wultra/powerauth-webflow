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

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAction;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.dataadapter.model.request.AfsRequestParameters;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AfsResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
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
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

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

    /**
     * Service constructor.
     * @param configuration Web Flow configuration.
     * @param nextStepClient Next Step client.
     * @param dataAdapterClient Data Adapter client.
     * @param operationSessionService Operation session service.
     */
    @Autowired
    public AfsIntegrationService(WebFlowServicesConfiguration configuration, NextStepClient nextStepClient, DataAdapterClient dataAdapterClient, OperationSessionService operationSessionService, HttpServletRequest httpServletRequest) {
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
     * @param afsAction AFS action to be executed.
     * @return Response from anti-fraud system.
     */
    public AfsResponse executeInitAction(String operationId, AfsAction afsAction) {
        return executeAfsAction(operationId, afsAction, Collections.emptyList(), 1, null, null);
    }

    /**
     * Execute an anti-fraud system action. This method variant is used during step authentication.
     * The response from AFS has no impact on Web Flow.
     *
     * @param operationId Operation ID.
     * @param afsAction AFS action to be executed.
     * @param authInstruments Authentication instruments used in this step.
     * @param stepIndex Index in current authentication step.
     * @param authStepResult Authentication step result.
     */
    public void executeAuthAction(String operationId, AfsAction afsAction, List<AuthInstrument> authInstruments, int stepIndex, AuthStepResult authStepResult) {
        executeAfsAction(operationId, afsAction, authInstruments, stepIndex, authStepResult, null);
    }

    /**
     * Execute an anti-fraud system action. This method variant is used during logout.
     * The response from AFS has no impact on Web Flow.
     *
     * @param operationId Operation ID.
     * @param operationTerminationReason Reason why operation was terminated.
     */
    public void executeLogoutAction(String operationId, OperationTerminationReason operationTerminationReason) {
        executeAfsAction(operationId, AfsAction.LOGOUT, Collections.emptyList(), 1, null, operationTerminationReason);
    }

    /**
     * Execute a generic anti-fraud system action and return response.
     *
     * @param operationId Operation ID.
     * @param afsAction AFS action to be executed.
     * @param authInstruments Authentication instruments used in this step.
     * @param stepIndex Index in current authentication step.
     * @param authStepResult Authentication step result.
     * @param operationTerminationReason Reason why operation was terminated.
     * @return Response from anti-fraud system.
     */
    private AfsResponse executeAfsAction(String operationId, AfsAction afsAction, List<AuthInstrument> authInstruments, int stepIndex, AuthStepResult authStepResult, OperationTerminationReason operationTerminationReason) {
        if (configuration.isAfsEnabled()) {
            logger.debug("AFS integration is enabled");
            try {
                // Retrieve operation
                ObjectResponse<GetOperationDetailResponse> operationDetail = nextStepClient.getOperationDetail(operationId);
                GetOperationDetailResponse operation = operationDetail.getResponseObject();
                ObjectResponse<GetOperationConfigDetailResponse> objectResponse = nextStepClient.getOperationConfigDetail(operation.getOperationName());
                GetOperationConfigDetailResponse config = objectResponse.getResponseObject();
                if (config.isAfsEnabled()) {
                    // Prepare all AFS request parameters
                    String userId = operation.getUserId();
                    String organizationId = operation.getOrganizationId();
                    FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
                    ApplicationContext applicationContext = operation.getApplicationContext();
                    OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData, applicationContext);
                    AfsType afsType = configuration.getAfsType();
                    String clientIp = operationSessionService.getOperationToSessionMapping(operation.getOperationId()).getClientIp();
                    Map<String, Object> extras = prepareExtrasForAfs();
                    // AuthStepResult is null due to init action
                    AfsRequestParameters afsRequestParameters = new AfsRequestParameters(afsType, afsAction, clientIp, stepIndex, authStepResult, operationTerminationReason);
                    logger.info("AFS action: {}, user ID: {}, operation ID: {}", afsAction, operation.getUserId(), operation.getOperationId());
                    ObjectResponse<AfsResponse> afsObjectResponse = dataAdapterClient.executeAfsAction(userId, organizationId, operationContext, afsRequestParameters, authInstruments, extras);
                    // TODO - save AFS response in Next Step
                    return afsObjectResponse.getResponseObject();
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
     * Prepare extras which are sent with request to AFS. These values are AFS type dependent.
     *
     * @return AFS extras.
     */
    private Map<String, Object> prepareExtrasForAfs() {
        Map<String, Object> extras = new LinkedHashMap<>();
        AfsType afsType = configuration.getAfsType();
        if (afsType == AfsType.THREAT_MARK) {
            // RequestContextHolder is used instead of autowiring because of WebSocketDisconnectListener code which
            // runs outside of DispatcherServlet.
            RequestAttributes attr = RequestContextHolder.getRequestAttributes();
            if (attr instanceof NativeWebRequest) {
                HttpServletRequest request = (HttpServletRequest) ((NativeWebRequest) attr).getNativeRequest();
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie: cookies) {
                        // TODO - use configuration
                        if (cookie.getName().equals("CoBNX2ZROo")) {
                            extras.put("tm_device_tag", cookie.getValue());
                        }
                        if (cookie.getName().equals("DV7mCBByG2")) {
                            extras.put("tm_session_sid", cookie.getValue());
                        }
                    }
                }
            }
        }
        return extras;
    }

}
