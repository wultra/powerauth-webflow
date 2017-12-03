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

package io.getlime.security.powerauth.lib.webflow.authentication.base;

import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for any authentication step responses.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AuthStepResponse {

    private AuthStepResult result;
    private List<AuthStep> next;
    private String message;
    private Integer remainingAttempts;

    public AuthStepResponse() {
        this.next = new ArrayList<>();
    }

    /**
     * Get the auth step result for the response - either success, or failure.
     *
     * @return Auth result of the current step.
     */
    public AuthStepResult getResult() {
        return result;
    }

    /**
     * Set auth step result for the response.
     *
     * @param result Auth result of the current step.
     */
    public void setResult(AuthStepResult result) {
        this.result = result;
    }

    /**
     * Sets the message displayed to the user.
     *
     * @param message message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the message to be displayed to the user.
     *
     * @return message to show
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the list with the next authentication methods.
     *
     * @return List with the next authentication methods.
     */
    public List<AuthStep> getNext() {
        return next;
    }

    /**
     * Get number of remaining authentication attempts.
     * @return Number of remaining attempts.
     */
    public Integer getRemainingAttempts() {
        return remainingAttempts;
    }

    /**
     * Set number of remaining authentication attempts.
     * @param remainingAttempts Number of remaining attempts.
     */
    public void setRemainingAttempts(Integer remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }
}
