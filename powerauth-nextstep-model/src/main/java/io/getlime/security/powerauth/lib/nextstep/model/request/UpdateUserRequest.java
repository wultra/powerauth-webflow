/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ContactType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Request object used for updating a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class UpdateUserRequest {

    @NotBlank
    @Size(min = 1, max = 256)
    private String userId;

    private UserIdentityStatus userIdentityStatus;

    private Map<String, Object> extras;

    private List<String> roles;

    @Valid
    private List<UpdatedContact> contacts;

    @Valid
    private List<UpdatedCredential> credentials;

    /**
     * Contact to update.
     */
    @Data
    public static class UpdatedContact {

        @NotBlank
        @Size(min = 2, max = 256)
        private String contactName;
        @NotNull
        private ContactType contactType;
        @NotBlank
        @Size(min = 2, max = 256)
        private String contactValue;
        private boolean primary;

    }

    /**
     * Credential to update.
     */
    @Data
    public static class UpdatedCredential {

        @NotBlank
        @Size(min = 2, max = 256)
        private String credentialName;
        @NotNull
        private CredentialType credentialType;
        @Size(min = 1, max = 256)
        private String username;
        @Size(min = 1, max = 256)
        private String credentialValue;
        private Date timestampExpires;

    }

}
