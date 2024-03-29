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
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Request object used for looking up a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class LookupUsersRequest {

    // Filter by created date
    private Date createdStartDate;
    private Date createdEndDate;

    // Filter by user identity status
    private UserIdentityStatus userIdentityStatus;

    // Filter by roles
    @JsonSetter(nulls = Nulls.SKIP)
    private final List<String> roles = new ArrayList<>();

    // Filter by username and credentialName to allow username -> user ID mapping
    @Size(min = 1, max = 256)
    private String username;
    @Size(min = 2, max = 256)
    private String credentialName;

    // Filter by credential status to allow lookup of blocked credentials
    private CredentialStatus credentialStatus;

}
