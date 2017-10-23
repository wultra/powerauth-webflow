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
package io.getlime.security.powerauth.lib.dataadapter.exception;

/**
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class UserNotFoundException extends Exception {

    /**
     * Default constructor.
     */
    public UserNotFoundException() {
    }

    /**
     * Constructor with message.
     *
     * @param message Message.
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message Message.
     * @param cause   Cause, original exception.
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause.
     *
     * @param cause Cause, original exception.
     */
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}
