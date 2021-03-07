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
package io.getlime.security.powerauth.lib.nextstep.model.response;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * Response object used for updating an OTP policy.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class UpdateOtpPolicyResponse {

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
    @NotBlank
    @Size(min = 2, max = 256)
    private String genAlgorithm;
    @NotNull
    private OtpGenerationParam genParam;
    @NotNull
    private OtpPolicyStatus otpPolicyStatus;

}
