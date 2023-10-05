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

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request object used for authenticating using an OTP.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class OtpAuthenticationRequest {

    // Either otpId or operationId should be present
    @Size(min = 36, max = 36)
    private String otpId;
    @Size(min = 1, max = 256)
    private String operationId;
    @NotBlank
    @Size(min = 1, max = 256)
    private String otpValue;
    // Whether the OTP value is only being checked, successful OTP check does not change OTP status
    private boolean checkOnly;
    // Operation ID is extracted from OTP record in case that otpId is sent
    private boolean updateOperation;
    // Authentication method is required only in case multiple methods are defined in Next Steps
    private AuthMethod authMethod;

}
