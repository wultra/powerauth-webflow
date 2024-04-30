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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialAuthenticationMode;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object used for authenticating using a credential and an OTP.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CombinedAuthenticationRequest {

    @Size(min = 2, max = 256)
    private String credentialName;
    @NotBlank
    @Size(min = 1, max = 256)
    private String userId;
    @NotBlank
    @Size(min = 1, max = 256)
    private String credentialValue;
    // Null value is allowed, defaults to MATCH_EXACT
    private CredentialAuthenticationMode authenticationMode;
    private List<Integer> credentialPositionsToVerify = new ArrayList<>();
    // Either otpId or operationId should be present
    @Size(min = 36, max = 36)
    private String otpId;
    @Size(min = 1, max = 256)
    private String operationId;
    @NotBlank
    @Size(min = 1, max = 256)
    private String otpValue;
    // Operation ID is extracted from OTP record in case that otpId is sent
    private boolean updateOperation;
    // Authentication method is required only in case multiple methods are defined in Next Steps
    private AuthMethod authMethod;

}
