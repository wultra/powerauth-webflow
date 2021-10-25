/*
 * Copyright 2017 Wultra s.r.o.
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

import com.wultra.security.powerauth.client.model.enumeration.SignatureType;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.response.GetPAOperationMappingResponse;
import io.getlime.security.powerauth.lib.mtoken.model.entity.AllowedSignatureType;
import io.getlime.security.powerauth.lib.mtoken.model.request.OperationApproveRequest;
import io.getlime.security.powerauth.lib.mtoken.model.request.OperationRejectRequest;
import io.getlime.security.powerauth.lib.mtoken.model.response.OperationListResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.OperationIsAlreadyFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.OperationNotConfiguredException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidActivationException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidRequestObjectException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.MobileAppApiException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.converter.OperationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.PowerAuthOperationService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.WebSocketMessageService;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuth;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthToken;
import io.getlime.security.powerauth.rest.api.spring.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This controller presents endpoints that are consumed by the native mobile app,
 * not the web application.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/token/app")
public class MobileAppApiController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(MobileAppApiController.class);

    private final WebSocketMessageService webSocketMessageService;
    private final AuthMethodQueryService authMethodQueryService;
    private final DataAdapterClient dataAdapterClient;
    private final PowerAuthOperationService powerAuthOperationService;

    private final FormDataConverter formDataConverter = new FormDataConverter();

    /**
     * Controller constructor.
     * @param webSocketMessageService Web Socket message service.
     * @param authMethodQueryService Authentication method query service.
     * @param dataAdapterClient Data Adapter client.
     * @param powerAuthOperationService PowerAuth operation service.
     */
    @Autowired
    public MobileAppApiController(WebSocketMessageService webSocketMessageService, AuthMethodQueryService authMethodQueryService, DataAdapterClient dataAdapterClient, PowerAuthOperationService powerAuthOperationService) {
        this.webSocketMessageService = webSocketMessageService;
        this.authMethodQueryService = authMethodQueryService;
        this.dataAdapterClient = dataAdapterClient;
        this.powerAuthOperationService = powerAuthOperationService;
    }

    /**
     * Get current authentication name.
     * @return Current authentication name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    /**
     * List pending operations for Mobile Token authorization, authenticate using 1FA signature.
     * @param apiAuthentication API authentication.
     * @return Response with list of pending operations.
     * @throws InvalidActivationException Thrown in case activation is not valid.
     * @throws PowerAuthAuthenticationException Thrown in case PowerAuth authentication fails.
     */
    @RequestMapping(value = "/operation/list/signature", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/list/signature", signatureType = {PowerAuthSignatureTypes.POSSESSION})
    public @ResponseBody ObjectResponse<OperationListResponse> getOperationList(PowerAuthApiAuthentication apiAuthentication) throws InvalidActivationException, PowerAuthAuthenticationException {
        return getOperationListImpl(apiAuthentication);
    }

    /**
     * List pending operations for Mobile Token authorization, authenticate using simple token-based authentication.
     * @param apiAuthentication API authentication.
     * @return Response with list of pending operations.
     * @throws InvalidActivationException Thrown in case activation is not valid.
     * @throws PowerAuthAuthenticationException Thrown in case PowerAuth authentication fails.
     */
    @RequestMapping(value = "/operation/list", method = RequestMethod.POST)
    @PowerAuthToken(signatureType = {
            PowerAuthSignatureTypes.POSSESSION,
            PowerAuthSignatureTypes.POSSESSION_BIOMETRY,
            PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE,
            PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE_BIOMETRY
    })
    public @ResponseBody ObjectResponse<OperationListResponse> getOperationListTokens(PowerAuthApiAuthentication apiAuthentication) throws InvalidActivationException, PowerAuthAuthenticationException {
        return getOperationListImpl(apiAuthentication);
    }

    /**
     * List pending operations for Mobile Token authorization.
     * @param apiAuthentication API authentication.
     * @return Response with list of pending operations.
     * @throws InvalidActivationException Thrown in case activation is not valid.
     * @throws PowerAuthAuthenticationException Thrown in case PowerAuth authentication fails.
     */
    private ObjectResponse<OperationListResponse> getOperationListImpl(PowerAuthApiAuthentication apiAuthentication) throws InvalidActivationException, PowerAuthAuthenticationException {
        if (apiAuthentication != null && apiAuthentication.getUserId() != null && apiAuthentication.getActivationObject() != null) {
            String activationId = apiAuthentication.getActivationObject().getActivationId();
            String userId = apiAuthentication.getUserId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException(activationId);
            }

            // Get the list of operations for given user
            List<GetOperationDetailResponse> operationList;
            Map<String, GetOperationConfigDetailResponse> operationConfigs;
            try {
                operationList = getOperationListForUser(userId, true);
                operationConfigs = getOperationConfigs(operationList);
            } catch (AuthStepException e) {
                logger.error("Could not retrieve operation list", e);
                // Next step operation list failed, return empty operation list
                return new ObjectResponse<>(new OperationListResponse());
            }

            // Prepare converter
            final OperationConverter converter = new OperationConverter();

            // Prepare converted result with operations
            OperationListResponse result = new OperationListResponse();
            for (GetOperationDetailResponse operation: operationList) {
                final GetOperationConfigDetailResponse operationConfig = operationConfigs.get(operation.getOperationName());
                result.add(converter.fromOperationDetailResponse(operation, operationConfig.getMobileTokenMode()));
            }

            // Return response
            return new ObjectResponse<>(result);
        } else {
            throw new PowerAuthAuthenticationException();
        }
    }

    /**
     * Get map of all operation configurations (operation name -> operation configuration).
     * @param operations Operation list.
     * @return Map of operation configurations.
     * @throws AuthStepException Thrown in case operation configuration query fails.
     */
    private Map<String, GetOperationConfigDetailResponse> getOperationConfigs(List<GetOperationDetailResponse> operations) throws AuthStepException {
        final Map<String, GetOperationConfigDetailResponse> operationConfigs = new HashMap<>();
        final GetOperationConfigListResponse allConfigsResponse = getOperationConfigs();
        // Construct Map for all configured operations on server (operation name -> operation configuration)
        final Map<String, GetOperationConfigDetailResponse> allOperationConfigs = new HashMap<>();
        for (GetOperationConfigDetailResponse config: allConfigsResponse.getOperationConfigs()) {
            allOperationConfigs.put(config.getOperationName(), config);
        }
        // Go through operations for which a configuration was requested and construct response map
        for (GetOperationDetailResponse operation: operations) {
            String operationName = operation.getOperationName();
            if (!operationConfigs.containsKey(operationName)) {
                final GetOperationConfigDetailResponse operationConfig = allOperationConfigs.get(operationName);
                // In case the operation configuration is not found, throw an OperationNotConfiguredException to alert about misconfigured server
                if (operationConfig == null) {
                    throw new OperationNotConfiguredException("Operation not configured, operation name: " + operationName);
                }
                operationConfigs.put(operationName, operationConfig);
            }
        }
        return operationConfigs;
    }

    /**
     * Authorize an operation using Mobile Token.
     * @param request Mobile Token authorization request.
     * @param apiAuthentication API authentication.
     * @return Authorization response.
     * @throws MobileAppApiException Thrown when signature verification fails.
     * @throws PowerAuthAuthenticationException Thrown in case PowerAuth authentication fails.
     * @throws AuthStepException Thrown when operation is invalid.
     */
    @RequestMapping(value = "/operation/authorize", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/authorize", signatureType = {
            PowerAuthSignatureTypes.POSSESSION,
            PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE,
            PowerAuthSignatureTypes.POSSESSION_BIOMETRY
    })
    public @ResponseBody Response verifySignature(@RequestBody ObjectRequest<OperationApproveRequest> request, PowerAuthApiAuthentication apiAuthentication) throws MobileAppApiException, PowerAuthAuthenticationException, AuthStepException {

        if (request.getRequestObject() == null) {
            throw new InvalidRequestObjectException();
        }

        String operationId = request.getRequestObject().getId();
        final GetOperationDetailResponse operation = getOperation(operationId);

        if (apiAuthentication != null && apiAuthentication.getUserId() != null && apiAuthentication.getActivationObject() != null) {
            String activationId = apiAuthentication.getActivationObject().getActivationId();
            String userId = apiAuthentication.getUserId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException(activationId);
            }

            try {

                FormData formData = formDataConverter.fromOperationFormData(operation.getFormData());
                ApplicationContext applicationContext = operation.getApplicationContext();
                OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formData, applicationContext);
                GetPAOperationMappingResponse response = dataAdapterClient.getPAOperationMapping(userId, operation.getOrganizationId(), getAuthMethodName(operation), operationContext).getResponseObject();
                operation.setOperationName(response.getOperationName());
                operation.setOperationData(response.getOperationData());
                operation.setFormData(formDataConverter.fromFormData(response.getFormData()));

                // Check if signature type is allowed
                if (!isSignatureTypeAllowedForOperation(operation.getOperationName(), apiAuthentication.getAuthenticationContext().getSignatureType())) {
                    throw new PowerAuthAuthenticationException();
                }

                if (operation.getOperationData().equals(request.getRequestObject().getData())
                        && operation.getUserId() != null
                        && operation.getUserId().equals(apiAuthentication.getUserId())) {
                    final List<AuthInstrument> authInstruments = Collections.singletonList(AuthInstrument.POWERAUTH_TOKEN);
                    SignatureType signatureType = SignatureType.enumFromString(apiAuthentication.getAuthenticationContext().getSignatureType().toString());
                    boolean approvalSucceeded = powerAuthOperationService.approveOperation(operation, activationId, signatureType);
                    if (!approvalSucceeded) {
                        throw new OperationIsAlreadyFailedException("Operation approval has failed");
                    }
                    final AuthOperationResponse updateOperationResponse = authorize(operationId, userId, operation.getOrganizationId(), authInstruments, null);
                    webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getAuthResult());
                    return new Response();
                } else {
                    boolean approvalFailSucceeded = powerAuthOperationService.failApprovalForOperation(operation);
                    if (!approvalFailSucceeded) {
                        throw new OperationIsAlreadyFailedException("Operation fail approval has failed");
                    }
                    throw new PowerAuthAuthenticationException();
                }
            } catch (NextStepClientException | DataAdapterClientErrorException ex) {
                logger.error(ex.getMessage(), ex);
                throw new PowerAuthAuthenticationException();
            }
        } else {
            powerAuthOperationService.failApprovalForOperation(operation);
            throw new PowerAuthAuthenticationException();
        }
    }

    /**
     * Cancel an operation.
     * @param request Cancel request.
     * @param apiAuthentication API authentication.
     * @return Cancel response.
     * @throws MobileAppApiException Thrown when operation could not be canceled.
     * @throws PowerAuthAuthenticationException Thrown in case PowerAuth authentication fails.
     * @throws AuthStepException Thrown when operation is invalid.
     */
    @RequestMapping(value = "/operation/cancel", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/cancel", signatureType = {PowerAuthSignatureTypes.POSSESSION})
    public @ResponseBody Response cancelOperation(@RequestBody ObjectRequest<OperationRejectRequest> request, PowerAuthApiAuthentication apiAuthentication) throws MobileAppApiException, PowerAuthAuthenticationException, AuthStepException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null && apiAuthentication.getActivationObject() != null) {
            String activationId = apiAuthentication.getActivationObject().getActivationId();
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException(activationId);
            }

            final GetOperationDetailResponse operation = getOperation(operationId);
            boolean rejectSucceeded = powerAuthOperationService.rejectOperation(operation, activationId);
            final UpdateOperationResponse updateOperationResponse = cancelAuthorization(operationId, userId, OperationCancelReason.fromString(request.getRequestObject().getReason()), null, false);
            if (updateOperationResponse != null) {
                webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
            }
            if (!rejectSucceeded) {
                throw new OperationIsAlreadyFailedException("Operation reject has failed");
            }
            return new Response();

        } else {
            throw new PowerAuthAuthenticationException();
        }

    }

    /**
     * Verifies that activation ID matches configured activation ID for given user.
     * @param activationId Checked activation ID.
     * @param userId User ID.
     * @return Whether activation ID matches configured activation ID.
     */
    private boolean verifyActivationId(String activationId, String userId) {
        try {
            String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(userId);
            if (configuredActivationId != null && configuredActivationId.equals(activationId)) {
                return true;
            }
        } catch (NextStepClientException ex) {
            logger.error("Could not verify activationId", ex);
        }
        return false;
    }

    /**
     * Verify if provided PowerAuth signature type is allowed for operation with provided name.
     *
     * @param operationName Operation name.
     * @param signatureTypes Signature type that was returned from signature verification.
     * @return True if the signature type is allowed, false otherwise.
     */
    private boolean isSignatureTypeAllowedForOperation(String operationName, PowerAuthSignatureTypes signatureTypes)  {

        // Get configuration for operation with given name
        GetOperationConfigDetailResponse operationConfig;
        try {
            operationConfig = getOperationConfig(operationName);
        } catch (AuthStepException e) {
            logger.error("Could not retrieve operation configuration", e);
            // Next step request failed, cannot decide
            return false;
        }

        // Convert loose JSON format to AllowedSignatureType structure
        OperationConverter operationConverter = new OperationConverter();
        AllowedSignatureType allowedSignatureType = operationConverter.fromMobileTokenMode(operationConfig.getMobileTokenMode());

        // Evaluate various signature types
        if (allowedSignatureType != null) {
            switch (allowedSignatureType.getType()) {
                case MULTIFACTOR_1FA: {
                    // Is the signature correct 1FA type - "possession"?
                    // Also, allow any 2FA signature as they are more strict than 1FA signature, as a fallback
                    return PowerAuthSignatureTypes.POSSESSION.equals(signatureTypes)
                            || PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE.equals(signatureTypes)
                            || PowerAuthSignatureTypes.POSSESSION_BIOMETRY.equals(signatureTypes);
                }
                case MULTIFACTOR_2FA: {
                    // Is the signature correct 2FA type - "possession_knowledge" or "possession_biometry"?
                    // Check for "possession_knowledge" first
                    if (PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE.equals(signatureTypes)) {
                        return true;
                    }
                    // Is "possession_biometry" allowed for this 2FA request?
                    if (PowerAuthSignatureTypes.POSSESSION_BIOMETRY.equals(signatureTypes)) {
                        return allowedSignatureType.getVariants() != null && allowedSignatureType.getVariants().contains("possession_biometry");
                    }
                    break;
                }
                case ASYMMETRIC_ECDSA: {
                    // Not allowed at this moment
                    return false;
                }
            }
        }
        return false;
    }

}
