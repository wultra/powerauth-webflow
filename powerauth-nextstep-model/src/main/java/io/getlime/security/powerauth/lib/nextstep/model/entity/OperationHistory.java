package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

/**
 * Class representing operation history entities.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationHistory {

    private AuthMethod authMethod;
    private AuthResult authResult;
    private AuthStepResult requestAuthStepResult;

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public AuthResult getAuthResult() {
        return authResult;
    }

    public void setAuthResult(AuthResult authResult) {
        this.authResult = authResult;
    }

    public AuthStepResult getRequestAuthStepResult() {
        return requestAuthStepResult;
    }

    public void setRequestAuthStepResult(AuthStepResult requestAuthStepResult) {
        this.requestAuthStepResult = requestAuthStepResult;
    }
}
