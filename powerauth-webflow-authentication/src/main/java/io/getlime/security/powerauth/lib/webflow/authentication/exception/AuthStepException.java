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

package io.getlime.security.powerauth.lib.webflow.authentication.exception;

/**
 * Exception during an authentication step.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class AuthStepException extends Exception {

    private Integer remainingAttempts;
    private String messageId;

    /**
     * Constructor with message and message ID.
     *
     * @param message Error message.
     * @param messageId Error message localization key.
     */
    public AuthStepException(String message, String messageId) {
        super(message);
        this.messageId = messageId;
    }

    /**
     * Constructor with message and cause.
     *
     * @param message Error message.
     * @param cause   Error cause (original exception, if any).
     */
    public AuthStepException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message, cause and message ID.
     *
     * @param message Error message.
     * @param cause   Error cause (original exception, if any).
     * @param messageId Error message localization key.
     */
    public AuthStepException(String message, Throwable cause, String messageId) {
        super(message, cause);
        this.messageId = messageId;
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

    /**
     * Get error message localization key.
     * @return Error message localization key.
     */
    public String getMessageId() {
        return messageId;
    }
}
