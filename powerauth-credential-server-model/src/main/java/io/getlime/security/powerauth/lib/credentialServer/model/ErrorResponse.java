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
package io.getlime.security.powerauth.lib.credentialServer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Roman Strobl
 */
public class ErrorResponse {

    public enum ResponseCode {
        AUTH_FAIL,
        USERNAME_FORMAT_INVALID,
        PASSWORD_FORMAT_INVALID,
        AUTH_METHOD_UNSUPPORTED,
        INTERNAL_SERVER_ERROR
    }

    @JsonProperty
    private ResponseCode code;
    @JsonProperty
    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(ResponseCode code, String message) {
        this.code = code;
        this.message = message;
    }

}
