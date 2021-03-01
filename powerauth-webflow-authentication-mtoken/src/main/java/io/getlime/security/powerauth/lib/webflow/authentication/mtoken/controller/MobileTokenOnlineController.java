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

import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthenticationResult;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.PushMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Online mobile token authentication controller.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/token/web")
public class MobileTokenOnlineController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(MobileTokenOnlineController.class);

    private final WebFlowServicesConfiguration webFlowServicesConfiguration;
    private final PushMessageService pushMessageService;
    private final NextStepClient nextStepClient;
    private final HttpSession httpSession;

    /**
     * Controller constructor.
     * @param webFlowServicesConfiguration Web Flow configuration.
     * @param pushMessageService Push message service.
     * @param nextStepClient Next Step client.
     * @param httpSession HTTP session.
     */
    @Autowired
    public MobileTokenOnlineController(WebFlowServicesConfiguration webFlowServicesConfiguration, PushMessageService pushMessageService, NextStepClient nextStepClient, HttpSession httpSession) {
        this.webFlowServicesConfiguration = webFlowServicesConfiguration;
        this.pushMessageService = pushMessageService;
        this.nextStepClient = nextStepClient;
        this.httpSession = httpSession;
    }

    /**
     * Authenticate using online mobile token.
     * @param request Online mobile token authentication rquest.
     * @return Authentication result with user ID and organization ID.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @Override
    protected AuthenticationResult authenticate(MobileTokenAuthenticationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final List<OperationHistory> history = operation.getHistory();
        for (OperationHistory h : history) {
            if (AuthMethod.POWERAUTH_TOKEN.equals(h.getAuthMethod())
                    && !AuthResult.FAILED.equals(h.getAuthResult())) {
                return new AuthenticationResult(operation.getUserId(), operation.getOrganizationId());
            }
        }
        return null;
    }

    /**
     * Get current authentication method.
     * @return Current authentication method.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    /**
     * Initialize push message.
     * @return Initialization response.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public @ResponseBody MobileTokenInitResponse initPushMessage() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            final AuthMethod authMethod = getAuthMethodName(operation);
            logger.info("Init step started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());

            MobileTokenInitResponse initResponse = pushMessageService.sendStepInitPushMessage(operation, authMethod);
            initResponse.setOfflineModeAvailable(webFlowServicesConfiguration.isOfflineModeAvailable());
            if (authMethod == AuthMethod.LOGIN_SCA) {
                // Add username for LOGIN_SCA method
                String username = getUsernameFromHttpSession();
                initResponse.setUsername(username);
            }
            if (authMethod == AuthMethod.LOGIN_SCA || authMethod == AuthMethod.APPROVAL_SCA) {
                // Allow fallback to SMS in authentication method LOGIN_SCA
                initResponse.setSmsFallbackAvailable(true);
            }
            if (authMethod == AuthMethod.POWERAUTH_TOKEN) {
                // User selected POWERAUTH_TOKEN in a non-SCA step, set mobile token as active
                nextStepClient.updateMobileToken(operation.getOperationId(), true);
            }
            logger.debug("Step initialization succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return initResponse;
        } catch (NextStepClientException ex) {
            logger.error(ex.getMessage(), ex);
            MobileTokenInitResponse response = new MobileTokenInitResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            return response;
        }
    }

    /**
     * Perform online mobile token authentication. Method can be called repeatedly to verify current authentication status.
     *
     * @param request Online mobile token authentication request.
     * @return Authentication result.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse authenticateHandler(@RequestBody MobileTokenAuthenticationRequest request) throws AuthStepException {

        final GetOperationDetailResponse operation = getOperation(false);
        final AuthMethod authMethod = getAuthMethodName(operation);
        // Log level is set to FINE due to large amount of requests caused by polling.
        logger.debug("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
        if (operation.isExpired()) {
            logger.info("Operation has timed out, operation ID: {}", operation.getOperationId());
            // Handle operation expiration
            try {
                cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.TIMED_OUT_OPERATION, null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            clearCurrentBrowserSession();
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("operation.timeout");
            pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
            cleanHttpSession();
            logger.info("Step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return response;
        }

        if (AuthResult.DONE.equals(operation.getResult())) {
            authenticateCurrentBrowserSession();
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.CONFIRMED);
            response.getNext().addAll(operation.getSteps());
            response.setMessage("authentication.success");
            pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
            cleanHttpSession();
            logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return response;
        }

        final List<OperationHistory> history = operation.getHistory();
        for (OperationHistory h : history) {
            // in case step was already confirmed, the authentication method has already succeeded
            if (authMethod == h.getAuthMethod() && AuthStepResult.CONFIRMED.equals(h.getRequestAuthStepResult())) {
                final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                response.setResult(AuthStepResult.CONFIRMED);
                response.getNext().addAll(operation.getSteps());
                response.setMessage("authentication.success");
                pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
                cleanHttpSession();
                logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                return response;
            }
            // in case previous authentication lead to an authentication method failure, the authentication method has already failed
            if (authMethod == h.getAuthMethod() && AuthStepResult.AUTH_METHOD_FAILED.equals(h.getRequestAuthStepResult())) {
                clearCurrentBrowserSession();
                final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                response.setResult(AuthStepResult.AUTH_METHOD_FAILED);
                response.getNext().addAll(operation.getSteps());
                response.setMessage("authentication.fail");
                pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
                cleanHttpSession();
                logger.info("Step result: AUTH_METHOD_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                return response;
            }
            // in case the authentication has been canceled, the authentication method is canceled
            if (authMethod == h.getAuthMethod() && AuthResult.FAILED.equals(h.getAuthResult()) && AuthStepResult.CANCELED.equals(h.getRequestAuthStepResult())) {
                clearCurrentBrowserSession();
                final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                response.setResult(AuthStepResult.CANCELED);
                response.setMessage("operation.canceled");
                pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
                cleanHttpSession();
                logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                return response;
            }
        }
        // otherwise authentication is still pending and waits for user action

        // the check for disabled method needs to be done after operation history is verified - the operation can be already moved to the next step
        if (operation.getChosenAuthMethod() == AuthMethod.POWERAUTH_TOKEN && !isAuthMethodAvailable(operation)) {
            // when AuthMethod is disabled, operation should fail
            try {
                logger.info("Operation will be canceled because authentication method is no longer available, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
                cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.AUTH_METHOD_NOT_AVAILABLE, null);
            } catch (CommunicationFailedException ex) {
                // Exception is already logged
            }
            clearCurrentBrowserSession();
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("operation.methodNotAvailable");
            // push message may not be delivered when activation was blocked during authentication, error is logged and ignored
            pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
            cleanHttpSession();
            logger.info("Step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return response;
        }

        // WebSocket session can not be removed yet - authentication is in progress
        final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
        response.setResult(AuthStepResult.AUTH_FAILED);
        response.setMessage("authentication.fail");
        // Log level is set to FINE due to large amount of requests caused by polling.
        logger.debug("Step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
        return response;
    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse cancelAuthentication() throws AuthStepException {
        try {
            GetOperationDetailResponse operation = getOperation();
            AuthMethod authMethod = getAuthMethodName(operation);
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            pushMessageService.sendAuthStepFinishedPushMessage(operation, response.getMessage(), authMethod);
            cleanHttpSession();
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), authMethod.toString());
            return response;
        } catch (CommunicationFailedException ex) {
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            cleanHttpSession();
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Get username from HTTP session.
     */
    private String getUsernameFromHttpSession() {
        synchronized (httpSession.getServletContext()) {
            return (String) httpSession.getAttribute(HttpSessionAttributeNames.USERNAME);
        }
    }

    /**
     * Clean HTTP session.
     */
    private void cleanHttpSession() {
        synchronized (httpSession.getServletContext()) {
            httpSession.removeAttribute(HttpSessionAttributeNames.USERNAME);
        }
    }

}
