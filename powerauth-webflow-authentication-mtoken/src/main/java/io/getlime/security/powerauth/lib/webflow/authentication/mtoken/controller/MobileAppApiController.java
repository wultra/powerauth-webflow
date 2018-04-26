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

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.mtoken.model.entity.AllowedSignatureType;
import io.getlime.security.powerauth.lib.mtoken.model.request.OperationApproveRequest;
import io.getlime.security.powerauth.lib.mtoken.model.request.OperationRejectRequest;
import io.getlime.security.powerauth.lib.mtoken.model.response.OperationListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidActivationException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidRequestObjectException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.MobileAppApiException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.OperationExpiredException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.converter.OperationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.WebSocketMessageService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuth;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller presents endpoints that are consumed by the native mobile app,
 * not the web application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/token/app")
public class MobileAppApiController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    private final WebSocketMessageService webSocketMessageService;
    private final AuthMethodQueryService authMethodQueryService;

    /**
     * Controller constructor.
     * @param webSocketMessageService Web Socket message service.
     * @param authMethodQueryService Authentication method query service.
     */
    @Autowired
    public MobileAppApiController(WebSocketMessageService webSocketMessageService, AuthMethodQueryService authMethodQueryService) {
        this.webSocketMessageService = webSocketMessageService;
        this.authMethodQueryService = authMethodQueryService;
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
        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String activationId = apiAuthentication.getActivationId();
            String userId = apiAuthentication.getUserId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException();
            }

            // Get the list of operations for given user
            final List<GetOperationDetailResponse> operationList = getOperationListForUser(userId);

            // Prepare converter
            final OperationConverter converter = new OperationConverter();

            // At the moment, we only support 2FA signatures with both
            // knowledge and biometry factors
            AllowedSignatureType allowedSignatureType = new AllowedSignatureType();
            allowedSignatureType.setType(AllowedSignatureType.Type.MULTIFACTOR_2FA);
            allowedSignatureType.getVariants().add(PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE.toString());
            allowedSignatureType.getVariants().add(PowerAuthSignatureTypes.POSSESSION_BIOMETRY.toString());

            // Prepare converted result with operations
            OperationListResponse result = new OperationListResponse();
            for (GetOperationDetailResponse operation: operationList) {
                result.add(converter.fromOperationDetailResponse(operation, allowedSignatureType));
            }

            // Return response
            return new ObjectResponse<>(result);
        } else {
            throw new PowerAuthAuthenticationException();
        }
    }

    /**
     * Authorize an operation using Mobile Token.
     * @param request Mobile Token authorization request.
     * @param apiAuthentication API authentication.
     * @return Authorization response.
     * @throws NextStepServiceException Thrown when next step client fails.
     * @throws MobileAppApiException Thrown when signature verification fails.
     * @throws PowerAuthAuthenticationException Thrown in case PowerAuth authentication fails.
     * @throws AuthStepException Thrown when operation is invalid.
     */
    @RequestMapping(value = "/operation/authorize", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/authorize")
    public @ResponseBody Response verifySignature(@RequestBody ObjectRequest<OperationApproveRequest> request, PowerAuthApiAuthentication apiAuthentication) throws NextStepServiceException, MobileAppApiException, PowerAuthAuthenticationException, AuthStepException {

        if (request.getRequestObject() == null) {
            throw new InvalidRequestObjectException();
        }

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String activationId = apiAuthentication.getActivationId();
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException();
            }

            final GetOperationDetailResponse operation = getOperation(operationId);
            if (operation.isExpired()) {
                throw new OperationExpiredException();
            }
            if (operation.getOperationData().equals(request.getRequestObject().getData())
                    && operation.getUserId() != null
                    && operation.getUserId().equals(apiAuthentication.getUserId())) {
                final UpdateOperationResponse updateOperationResponse = authorize(operationId, userId);
                webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
                return new Response();
            } else {
                throw new PowerAuthAuthenticationException();
            }
        } else {
            throw new PowerAuthAuthenticationException();
        }

    }

    /**
     * Cancel an operation.
     * @param request Cancel request.
     * @param apiAuthentication API authentication.
     * @return Cancel response.
     * @throws MobileAppApiException Thrown when operation could not be canceled.
     * @throws NextStepServiceException Thrown when next step client fails.
     * @throws PowerAuthAuthenticationException Thrown in case PowerAuth authentication fails.
     * @throws AuthStepException Thrown when operation is invalid.
     */
    @RequestMapping(value = "/operation/cancel", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/cancel", signatureType = {PowerAuthSignatureTypes.POSSESSION})
    public @ResponseBody Response cancelOperation(@RequestBody ObjectRequest<OperationRejectRequest> request, PowerAuthApiAuthentication apiAuthentication) throws MobileAppApiException, NextStepServiceException, PowerAuthAuthenticationException, AuthStepException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String activationId = apiAuthentication.getActivationId();
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException();
            }

            final UpdateOperationResponse updateOperationResponse = cancelAuthorization(operationId, userId, OperationCancelReason.fromString(request.getRequestObject().getReason()), null);
            webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
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
        } catch (NextStepServiceException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not verify activationId", ex);
        }
        return false;
    }

}
