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
package io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Model for a username / password authentication request from client.
 *
 * @author Roman Strobl
 */
public class UsernamePasswordAuthenticationRequest extends AuthStepRequest {

    private String username;
    private String password;

    /**
     * Get username.
     *
     * @return Username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username.
     *
     * @param username Username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get password.
     *
     * @return Password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password.
     *
     * @param password Password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
