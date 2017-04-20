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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

/**
 * Class representing a REST endpoint error.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class ErrorModel {

    /**
     * Response codes for different authentication failures.
     */
    public enum Code {
        ERROR_GENERIC
    }

    private Code code;
    private String message;

    /**
     * Get the error code.
     * @return Error code.
     */
    public Code getCode() {
        return code;
    }

    /**
     * Set the error code.
     * @param code Error code.
     */
    public void setCode(Code code) {
        this.code = code;
    }

    /**
     * Get the error message.
     * @return Error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the error message.
     * @param message Error message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
