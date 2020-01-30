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
package io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Collections;
import java.util.List;

/**
 * Model for an authentication request for SCA login.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class LoginScaAuthRequest extends AuthStepRequest {

    @Pattern(regexp = "^[a-zA-Z0-9_\\-@./\\\\:;<>!#$%&'\"*+=?^`(){}\\[\\]|~]{4,256}$", message = "login.username.invalidFormat")
    @NotEmpty(message = "login.username.empty")
    private String username;

    @Pattern(regexp = "^[a-zA-Z0-9_\\-@./\\\\:;<>!#$%&'\"*+=?^`(){}\\[\\]|~\\s]{2,256}$", message = "login.organization.invalidFormat")
    @NotEmpty(message = "login.organization.empty")
    private String organizationId;

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
     * Get organization ID.
     * @return Organization ID.
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set organization ID.
     * @param organizationId Organization ID.
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public List<AuthInstrument> getAuthInstruments() {
        return Collections.emptyList();
    }
}
