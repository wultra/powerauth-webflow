package io.getlime.security.powerauth.app.webauth.service;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;
import io.getlime.security.powerauth.app.webauth.model.entity.authentication.DisplayLoginFormResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.DisplayMessageResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.WebAuthMessageType;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ErrorModel;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * This service handles resolving the next message which will be shown to the user based on a response from the Next
 * Step server.
 *
 * @author Roman Strobl
 */
@Service
public class NextMessageResolutionService {

    public NextMessageResolutionService() {
    }

    /**
     * Resolves the next message based on a response from the Next Step server. AuthenticationStatus from Credential
     * Server can be used to provide a more meaningful error message.
     *
     * @param response   response from the Next Step server
     * @param authenticationSucceeded last authentication result, use null if authentication was not performed
     * @param sessionId  WebSocket sessionId
     * @return next message to show to the user
     */
    public WebSocketJsonMessage resolveNextMessage(Response<?> response, Boolean authenticationSucceeded, String sessionId) {
        // TODO - handle better with OOP
        if (response.getResponseObject() instanceof ErrorModel) {
            return handleError((ErrorModel) response.getResponseObject(), sessionId);
        }
        if (response.getResponseObject() instanceof CreateOperationResponse) {
            CreateOperationResponse responseObject = (CreateOperationResponse) response.getResponseObject();
            // create operation cannot have authStatus yet
            return resolveAuthResult(responseObject.getResult(), responseObject.getSteps(), responseObject.getOperationId(), null, sessionId);
        }
        if (response.getResponseObject() instanceof UpdateOperationResponse) {
            UpdateOperationResponse responseObject = (UpdateOperationResponse) response.getResponseObject();
            // authStatus contains last authentication status for error messages
            return resolveAuthResult(responseObject.getResult(), responseObject.getSteps(), responseObject.getOperationId(), authenticationSucceeded, sessionId);
        }
        return new DisplayMessageResponse(sessionId, WebAuthMessageType.ERROR, "Operation has failed.");
    }

    /**
     * Error handling for response with ERROR status
     *
     * @param errorModel ErrorModel describes the error message
     * @param sessionId  WebSocket sessionId
     * @return error message
     */
    private static WebSocketJsonMessage handleError(ErrorModel errorModel, String sessionId) {
        return new DisplayMessageResponse(sessionId, WebAuthMessageType.ERROR, errorModel.getMessage());
    }

    /**
     * Resolves responses with OK status
     *
     * @param authResult  main response type from Next Step server
     * @param steps       prioritized steps for the next action
     * @param operationId id of current operation
     * @param authenticationSucceeded  last authentication result, use null if authentication was not performed
     * @param sessionId   WebSocket sessionId
     * @return next message to show to the user
     */
    private static WebSocketJsonMessage resolveAuthResult(AuthResult authResult, List<AuthStep> steps, String operationId,
                                                          Boolean authenticationSucceeded, String sessionId) {
        switch (authResult) {
            case CONTINUE:
                for (AuthStep step : steps) {
                    switch (step.getAuthMethod()) {
                        case USER_ID_ASSIGN:
                            System.out.println("Unsupported auth method: " + step.getAuthMethod());
                            break;
                        case SMS_KEY:
                            System.out.println("Unsupported auth method: " + step.getAuthMethod());
                            break;
                        case POWERAUTH_TOKEN:
                            System.out.println("Unsupported auth method: " + step.getAuthMethod());
                            break;
                        case USERNAME_PASSWORD_AUTH:
                            System.out.println("Using supported auth method: " + step.getAuthMethod());
                            if (Boolean.FALSE.equals(authenticationSucceeded)) {
                                return new DisplayLoginFormResponse(sessionId, operationId, "Authentication failed, please try again.", false);
                            } else {
                                return new DisplayLoginFormResponse(sessionId, operationId, "Please sign in.", false);
                            }
                    }
                }
                break;
            case DONE:
                return new DisplayMessageResponse(sessionId, WebAuthMessageType.INFORMATION, "Operation has been authorized.");
            case FAILED:
                return new DisplayMessageResponse(sessionId, WebAuthMessageType.ERROR, "Authorization has failed.");
        }

        return new DisplayMessageResponse(sessionId, WebAuthMessageType.ERROR, "Operation has failed.");
    }


}
