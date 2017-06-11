package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationHistory {

    private AuthMethod authMethod;
    private AuthResult authResult;

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
}
