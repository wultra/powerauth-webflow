/*
 * Copyright 2019 Wultra s.r.o.
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
