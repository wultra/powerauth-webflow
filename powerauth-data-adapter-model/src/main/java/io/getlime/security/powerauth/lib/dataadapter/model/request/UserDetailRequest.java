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

package io.getlime.security.powerauth.lib.dataadapter.model.request;

/**
 * Request object for obtaining user details by user ID.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UserDetailRequest {

    private String id;

    /**
     * Default constructor.
     */
    public UserDetailRequest() {
    }

    /**
     * Constructor with user ID as a parameter.
     * @param id User ID.
     */
    public UserDetailRequest(String id) {
        this.id = id;
    }

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set user ID.
     * @param id User ID.
     */
    public void setId(String id) {
        this.id = id;
    }
}
