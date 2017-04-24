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
package io.getlime.security.powerauth.lib.credentials.model.response;

/**
 * Response of the authentication using username and password.
 *
 * @author Roman Strobl
 */
public class AuthenticationResponse {

    /**
     * Unique userId which identifies the user.
     */
    private String userId;

    /**
     * Empty constructor.
     */
    public AuthenticationResponse() {
    }

    /**
     * Constructor with userId parameter for convenience.
     * @param userId Identification of the user.
     */
    public AuthenticationResponse(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the userId.
     * @param userId userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the userId.
     * @return userID
     */
    public String getUserId() {
        return userId;
    }

}
