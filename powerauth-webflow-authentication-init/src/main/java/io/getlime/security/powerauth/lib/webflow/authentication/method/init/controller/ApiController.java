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
package io.getlime.security.powerauth.lib.webflow.authentication.method.init.controller;

import io.getlime.security.powerauth.app.webflow.oauth.OAuthBasicContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.data.OperationDataBuilder;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.OperationNotAvailableException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.request.InitOperationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.response.InitOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller that handles the initialization of the authentication flow.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/init")
public class ApiController extends AuthMethodController<InitOperationRequest, InitOperationResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final OperationSessionService operationSessionService;

    /**
     * Controller constructor.
     * @param operationSessionService Operation session service.
     */
    @Autowired
    public ApiController(OperationSessionService operationSessionService) {
        this.operationSessionService = operationSessionService;
    }

    /**
     * Initialize a new authentication flow, by creating an operation. In case operation ID is already
     * included in the request, it initializes context with the operation with this ID and starts authentication
     * step sequence.
     *
     * @param request Authentication initialization request.
     * @return Authentication initialization response.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody InitOperationResponse register(@RequestBody InitOperationRequest request) {
        logger.info("Operation INIT started");

        GetOperationDetailResponse operation = null;

        try {
            operation = getOperation();
            logger.info("Operation found, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationName());
            checkOperationExpiration(operation);
        } catch (OperationNotAvailableException ex) {
            logger.info("Operation not found");
            // Operation is not available - this state is valid in INIT authentication method, operation was not initialized yet
            // and it will be initialized as a new operation with default form data for a login operation.
        } catch (AuthStepException ex) {
            // Operation validation failed, fail operation.
            logger.warn("Operation state is not valid, reason: {}", ex.getMessage());
            return failedOperationResponse(null, ex.getMessageId());
        }

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        String sessionId = attributes.getSessionId();

        if (operation == null) {
            final DefaultSavedRequest savedRequest = (DefaultSavedRequest) attributes.getAttribute("SPRING_SECURITY_SAVED_REQUEST", RequestAttributes.SCOPE_SESSION);
            OAuthBasicContext oAuthBasicContext = extractOAuthBasicContext(savedRequest);

            String operationName;
            if (oAuthBasicContext == null) {
                logger.error("OAuth 2.0 operation context was not extracted correctly and hence the process cannot continue.");
                return failedOperationResponse(null, "operationConfig.missing");
            }

            //TODO: Call data adapter to obtain correct operation name
            operationName = "login";

            GetOperationConfigDetailResponse operationConfig;
            try {
                operationConfig = getOperationConfig(operationName);
            } catch (AuthStepException e) {
                logger.error("Operation configuration is missing, operation name: {}", operationName);
                return failedOperationResponse(null, "operationConfig.missing");
            }
            final OperationFormData formData = new OperationFormData();
            formData.addTitle( "login.title");
            formData.addGreeting("login.greeting");
            formData.addSummary("login.summary");
            String operationData;
            try {
                 operationData = new OperationDataBuilder()
                        .templateVersion(operationConfig.getTemplateVersion())
                        .templateId(operationConfig.getTemplateId())
                        .build();
            } catch (InvalidOperationDataException ex) {
                logger.error("Operation data is invalid, error: {}", ex.getMessage());
                return failedOperationResponse(null, "operationData.invalid");
            }
            List<KeyValueParameter> params = new ArrayList<>();
            logger.info("Initialized default login operation");
            return initiateOperationWithName(operationName, operationData, formData, sessionId, params, null, new AuthResponseProvider() {
                @Override
                public InitOperationResponse doneAuthentication(String userId) {
                    logger.info("Step result: CONFIRMED, authentication method: {}", getAuthMethodName().toString());
                    return completeOperationResponse();
                }

                @Override
                public InitOperationResponse failedAuthentication(String userId, String failedReason) {
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
                    return failedOperationResponse(null, failedReason);
                }

                @Override
                public InitOperationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
                    return continueOperationResponse(operationId, steps);
                }
            });
        } else {
            logger.info("Continuing operation, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationName());
            return continueOperationWithId(operation.getOperationId(), sessionId, new AuthResponseProvider() {
                @Override
                public InitOperationResponse doneAuthentication(String userId) {
                    logger.info("Step result: CONFIRMED, authentication method: {}", getAuthMethodName().toString());
                    return completeOperationResponse();
                }

                @Override
                public InitOperationResponse failedAuthentication(String userId, String failedReason) {
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
                    return failedOperationResponse(null, failedReason);
                }

                @Override
                public InitOperationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
                    return continueOperationResponse(operationId, steps);
                }
            });
        }

    }

    /**
     * Create a failed operation response.
     * @param userId User ID.
     * @param failedReason Failure reason.
     * @return Failed operation response.
     */
    private InitOperationResponse failedOperationResponse(String userId, String failedReason) {
        clearCurrentBrowserSession();
        InitOperationResponse registrationResponse = new InitOperationResponse();
        registrationResponse.setResult(AuthStepResult.AUTH_FAILED);
        registrationResponse.setMessage(failedReason);
        return registrationResponse;
    }

    /**
     * Create a confirmed operation response.
     * @return Confirmed operation response.
     */
    private InitOperationResponse completeOperationResponse() {
        authenticateCurrentBrowserSession();
        InitOperationResponse registrationResponse = new InitOperationResponse();
        registrationResponse.setResult(AuthStepResult.CONFIRMED);
        return registrationResponse;
    }

    /**
     * Create a continue operation response.
     * @param operationId Operation ID.
     * @param steps Operation authentication steps.
     * @return Continue operation response.
     */
    private InitOperationResponse continueOperationResponse(String operationId, List<AuthStep> steps) {
        String operationHash = operationSessionService.generateOperationHash(operationId);
        InitOperationResponse registrationResponse = new InitOperationResponse();
        registrationResponse.setResult(AuthStepResult.CONFIRMED);
        registrationResponse.getNext().addAll(steps);
        // transfer operation hash to client for operation created in this step (required for default operation)
        registrationResponse.setOperationHash(operationHash);
        return registrationResponse;
    }

    /**
     * Extract OAuth 2.0 context from the saved request. Namely, fetch client_id value and
     * array with requested scopes.
     *
     * @param savedRequest Saved request with OAuth 2.0 attributes.
     * @return OAuth 2.0 context information, or null in case context cannot be extracted.
     */
    private OAuthBasicContext extractOAuthBasicContext(DefaultSavedRequest savedRequest) {

        // Check saved request for null
        if (savedRequest == null) { // OAuth 2.0 context missing
            logger.debug("OAuth 2.0 context was not found.");
            return null;
        }

        // Get OAuth 2.0 Client ID
        final String[] client_ids = savedRequest.getParameterValues("client_id");
        if (client_ids.length != 1) { // no client ID is present, or worse - more are present
            logger.debug("OAuth 2.0 Client ID must be present and unique.");
            return null;
        }
        final String client_id = client_ids[0];

        // Get OAuth 2.0 Scopes
        final String[] scopes = savedRequest.getParameterValues("scope");

        // Return the result
        OAuthBasicContext result = new OAuthBasicContext();
        result.setClientId(client_id);
        result.setScopes(scopes);

        return result;
    }

    /**
     * Get current authentication method.
     * @return Current authentication method.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.INIT;
    }

}
