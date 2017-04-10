package io.getlime.security.powerauth.app.webauth.service;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;
import io.getlime.security.powerauth.app.webauth.model.entity.authentication.DisplayLoginFormResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.DisplayMessageResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.WebAuthMessageType;
import io.getlime.security.powerauth.lib.credentialServer.model.AuthenticationStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Roman Strobl
 */
@Service
public class NextMessageResolutionService {

    public NextMessageResolutionService() {
    }

    public WebSocketJsonMessage resolveNextMessage(CreateOperationResponse response, String sessionId) {
        return resolveAuthResult(response.getResult(), response.getSteps(), response.getOperationId(), null, sessionId);
    }

    public WebSocketJsonMessage resolveNextMessage(UpdateOperationResponse response, AuthenticationStatus authStatus, String sessionId) {
        return resolveAuthResult(response.getResult(), response.getSteps(), response.getOperationId(), authStatus, sessionId);
    }

    private WebSocketJsonMessage resolveAuthResult(AuthResult authResult, List<AuthStep> steps, String operationId, AuthenticationStatus authStatus, String sessionId) {
        switch (authResult) {
            case CONTINUE:
                for (AuthStep step: steps) {
                    switch(step.getAuthMethod()) {
                        case USER_ID_ASSIGN:
                            System.out.println("Unsupported auth method: "+step.getAuthMethod());
                            break;
                        case SMS_KEY:
                            System.out.println("Unsupported auth method: "+step.getAuthMethod());
                            break;
                        case POWERAUTH_TOKEN:
                            System.out.println("Unsupported auth method: "+step.getAuthMethod());
                            break;
                        case USERNAME_PASSWORD_AUTH:
                            System.out.println("Using supported auth method: "+step.getAuthMethod());
                            if (authStatus==AuthenticationStatus.ERROR) {
                                return new DisplayLoginFormResponse(sessionId, operationId, "Authentication failed, please try again.", false);
                            } else {
                                return new DisplayLoginFormResponse(sessionId, operationId, "Please sign in.", false);
                            }
                    }
                }
                break;
            case DONE:
                return new DisplayMessageResponse(sessionId,
                        WebAuthMessageType.INFORMATION, "Operation has been authorized.");
            case FAILED:
                return new DisplayMessageResponse(sessionId,
                        WebAuthMessageType.INFORMATION, "Authorization has failed.");
        }

        return new DisplayMessageResponse(sessionId,
                WebAuthMessageType.INFORMATION, "Operation has failed.");
    }


}
