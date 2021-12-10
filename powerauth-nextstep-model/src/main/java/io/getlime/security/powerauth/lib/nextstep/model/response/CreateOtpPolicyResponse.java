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
package io.getlime.security.powerauth.lib.nextstep.model.response;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * Response object used for creating an OTP policy.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateOtpPolicyResponse {

    @NotBlank
    @Size(min = 2, max = 256)
    private String otpPolicyName;
    @Size(min = 2, max = 256)
    private String description;
    @NotNull
    @Positive
    private Integer length;
    @Positive
    private Integer attemptLimit;
    @Positive
    private Long expirationTime;
    @NotNull
    private OtpGenerationAlgorithm genAlgorithm;
    @NotNull
    private OtpGenerationParam genParam;
    @NotNull
    private OtpPolicyStatus otpPolicyStatus;

}
