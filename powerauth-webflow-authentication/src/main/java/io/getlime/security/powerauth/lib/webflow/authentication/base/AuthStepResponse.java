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

package io.getlime.security.powerauth.lib.webflow.authentication.base;

import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for any authentication step responses.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class AuthStepResponse {

    private AuthStepResult result;
    private final List<AuthStep> next = new ArrayList<>();
    private String message;
    private Integer remainingAttempts;

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
