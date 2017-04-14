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
package io.getlime.security.powerauth.lib.credentials.model.request;

import io.getlime.security.powerauth.lib.credentials.model.enumeration.AuthenticationType;

/**
 * @author Roman Strobl
 */
public class AuthenticationRequest {

    private String username;
    private String password;
    private AuthenticationType type;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(String username, String password, AuthenticationType type) {
        this.username = username;
        this.password = password;
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setType(AuthenticationType type) {
        this.type = type;
    }

    public AuthenticationType getType() {
        return type;
    }

}
