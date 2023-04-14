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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Class represents details of an OTP policy.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@EqualsAndHashCode(of = "otpPolicyName")
public class OtpPolicyDetail {

    @NotBlank
    @Size(min = 2, max = 256)
    private String otpPolicyName;
    @Size(min = 2, max = 256)
    private String description;
    @NotNull
    @Positive
    private Integer length;
    @PositiveOrZero
    private Integer attemptLimit;
    @Positive
    private Long expirationTime;
    @NotNull
    private OtpGenerationAlgorithm genAlgorithm;
    @NotNull
    private OtpGenerationParam genParam;
    @NotNull
    private OtpPolicyStatus otpPolicyStatus;
    @NotNull
    private Date timestampCreated;
    private Date timestampLastUpdated;

}
