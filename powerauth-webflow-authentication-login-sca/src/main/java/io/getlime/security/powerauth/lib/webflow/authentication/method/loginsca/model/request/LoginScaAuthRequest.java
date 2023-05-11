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
package io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.Collections;
import java.util.List;

/**
 * Model for an authentication request for SCA login.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class LoginScaAuthRequest extends AuthStepRequest {

    // Empty String is accepted in regexp to allow returning different message in @NotEmpty validation
    @Pattern(regexp = "^$|^[a-zA-Z0-9_\\-@./\\\\:;<>!#$%&'\"*+=?^`(){}\\[\\]|~]{4,256}$", message = "login.username.invalidFormat")
    // Empty username can be sent in case client certificate is used
    private String username;

    // Empty String is accepted in regexp to allow returning different message in @NotEmpty validation
    @Pattern(regexp = "^$|^[a-zA-Z0-9_\\-@./\\\\:;<>!#$%&'\"*+=?^`(){}\\[\\]|~\\s]{2,256}$", message = "login.organization.invalidFormat")
    @NotEmpty(message = "login.organization.empty")
    private String organizationId;
    private boolean clientCertificateUsed;

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

    /**
     * Get whether client certificate is used for authentication.
     * @return Whether client certificate is used for authentication.
     */
    public boolean isClientCertificateUsed() {
        return clientCertificateUsed;
    }

    /**
     * Set whether client certificate is used for authentication.
     * @param clientCertificateUsed Whether client certificate is used for authentication.
     */
    public void setClientCertificateUsed(boolean clientCertificateUsed) {
        this.clientCertificateUsed = clientCertificateUsed;
    }

    @Override
    public List<AuthInstrument> getAuthInstruments() {
        if (isClientCertificateUsed()) {
            return Collections.singletonList(AuthInstrument.CLIENT_CERTIFICATE);
        }
        return Collections.emptyList();
    }
}
