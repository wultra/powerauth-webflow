package io.getlime.security.powerauth.app.webauth.authentication.base;

import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AuthStepResponse {

    private AuthStepResult result;
    private List<AuthStep> next;
    private String message;

    public AuthStepResponse() {
        this.next = new ArrayList<>();
    }

    /**
     * Get the auth step result for the response - either success, or failure.
     * @return Auth result of the current step.
     */
    public AuthStepResult getResult() {
        return result;
    }

    /**
     * Set auth step result for the response.
     * @param result Auth result of the current step.
     */
    public void setResult(AuthStepResult result) {
        this.result = result;
    }

    /**
     * Sets the message displayed to the user.
     * @param message message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the message to be displayed to the user.
     * @return message to show
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the list with the next authentication methods.
     * @return List with the next authentication methods.
     */
    public List<AuthStep> getNext() {
        return next;
    }

}
