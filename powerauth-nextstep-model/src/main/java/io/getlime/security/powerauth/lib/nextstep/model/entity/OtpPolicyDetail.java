/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
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
