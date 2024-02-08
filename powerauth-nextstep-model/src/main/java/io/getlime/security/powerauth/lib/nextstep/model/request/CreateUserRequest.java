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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ContactType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.*;

/**
 * Request object used for creating a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateUserRequest {

    @NotBlank
    @Size(min = 1, max = 256)
    private String userId;

    @JsonSetter(nulls = Nulls.SKIP)
    private final Map<String, Object> extras = new LinkedHashMap<>();

    @JsonSetter(nulls = Nulls.SKIP)
    private final List<String> roles = new ArrayList<>();

    @Valid
    @JsonSetter(nulls = Nulls.SKIP)
    private final List<NewContact> contacts = new ArrayList<>();

    @Valid
    @JsonSetter(nulls = Nulls.SKIP)
    private final List<NewCredential> credentials = new ArrayList<>();

    /**
     * Contact to create.
     */
    @Data
    public static class NewContact {

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
     * Credential to create.
     */
    @Data
    public static class NewCredential {

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
        // Null value allowed, defaults to CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL
        private CredentialValidationMode validationMode;
        @Valid
        @JsonSetter(nulls = Nulls.SKIP)
        private final List<CredentialHistory> credentialHistory = new ArrayList<>();

    }

    /**
     * Credential history.
     */
    @Data
    public static class CredentialHistory {

        @Size(min = 1, max = 256)
        private String username;
        @NotBlank
        @Size(min = 1, max = 256)
        private String credentialValue;

    }

}
