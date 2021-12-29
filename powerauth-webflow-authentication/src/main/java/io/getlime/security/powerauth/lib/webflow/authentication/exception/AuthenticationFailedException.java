/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;

/**
 * Exception thrown in case user authentication fails.
 */
public class AuthenticationFailedException extends AuthStepException {

    private UserAccountStatus accountStatus;

    /**
     * Constructor with message and message ID.
     *
     * @param message Error message.
     * @param messageId Error message localization key.
     */
    public AuthenticationFailedException(String message, String messageId) {
        super(message, messageId);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message Error message.
     * @param cause   Error cause (original exception, if any).
     */
    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message, cause and message ID.
     *
     * @param message Error message.
     * @param cause   Error cause (original exception, if any).
     * @param messageId Error message localization key.
     */
    public AuthenticationFailedException(String message, Throwable cause, String messageId) {
        super(message, cause, messageId);
    }

    /**
     * Get current user account status.
     * @return User account status.
     */
    public UserAccountStatus getAccountStatus() {
        return accountStatus;
    }

    /**
     * Set current user account status.
     * @param accountStatus User account status.
     */
    public void setAccountStatus(UserAccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
}
