/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.method.init.controller;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateImplicitLoginOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
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
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.OperationNotAvailableException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.entity.OAuthBasicContext;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.request.InitOperationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.response.InitOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * @throws CommunicationFailedException In case the network communication fails when creating an implicit login operation.
     */
    @PostMapping("/authenticate")
    public @ResponseBody InitOperationResponse register(@RequestBody InitOperationRequest request) throws CommunicationFailedException {
        logger.info("Operation INIT started");

        GetOperationDetailResponse operation = null;

        try {
            operation = getOperation();
            logger.info("Operation found, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationName());
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

            if (oAuthBasicContext == null) {
                logger.error("OAuth 2.1 operation context was not extracted correctly and hence the process cannot continue.");
                return failedOperationResponse(null, "operationConfig.missing");
            }

            // Fetch and prepare operation by calling the Data Adapter.
            final CreateImplicitLoginOperationResponse ilo = createImplicitLoginOperation(oAuthBasicContext.getClientId(), oAuthBasicContext.getScopes());
            String operationName = (ilo.getName() != null) ? ilo.getName() : "login" ;
            final FormData daFormData = ilo.getFormData();
            final OperationFormData formData;
            if (daFormData != null) {
                formData = new OperationFormData();
                formData.addTitle(daFormData.getTitle().getId());
                formData.addGreeting(daFormData.getGreeting().getId());
                formData.addSummary(daFormData.getSummary().getId());
            } else {
                formData = new OperationFormData();
                formData.addTitle("login.title");
                formData.addGreeting("login.greeting");
                formData.addSummary("login.summary");
            }
            final ApplicationContext applicationContext = ilo.getApplicationContext();

            GetOperationConfigDetailResponse operationConfig;
            try {
                operationConfig = getOperationConfig(operationName);
            } catch (AuthStepException e) {
                logger.error("Operation configuration is missing, operation name: {}", operationName);
                return failedOperationResponse(null, "operationConfig.missing");
            }
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
            return initiateOperationWithName(operationName, operationData, formData, sessionId, params, applicationContext, new AuthResponseProvider() {
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
     * Extract OAuth 2.1 context from the saved request. Namely, fetch client_id value and
     * array with requested scopes.
     *
     * @param savedRequest Saved request with OAuth 2.1 attributes.
     * @return OAuth 2.1 context information, or null in case context cannot be extracted.
     */
    private OAuthBasicContext extractOAuthBasicContext(DefaultSavedRequest savedRequest) {

        // Check saved request for null
        if (savedRequest == null) { // OAuth 2.1 context missing
            logger.warn("OAuth 2.1 context was not found.");
            return null;
        }

        // Get OAuth 2.1 Client ID
        final String[] clientIds = savedRequest.getParameterValues("client_id");
        if (clientIds == null || clientIds.length != 1) { // no client ID is present, or worse - more are present
            logger.warn("OAuth 2.1 Client ID must be present and unique.");
            return null;
        }
        final String clientId = clientIds[0];

        // Get OAuth 2.1 Scopes
        final String[] scopes = savedRequest.getParameterValues("scope");

        // Return the result
        OAuthBasicContext result = new OAuthBasicContext();
        result.setClientId(clientId);
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
