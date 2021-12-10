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

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Request object used for creating an OTP.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateOtpRequest {

    // The userId is optional to allow creating OTP codes for users without identity
    @Size(min = 1, max = 256)
    private String userId;
    @NotBlank
    @Size(min = 2, max = 256)
    private String otpName;
    // Optional credential name for updating credential counters
    @Size(min = 2, max = 256)
    private String credentialName;
    // The otpData parameter has priority over data extracted from operation.
    // The otpData can be an empty string, null value indicates data taken from operation.
    @Size(max = 256)
    private String otpData;
    @Size(min = 1, max = 256)
    private String operationId;

}
