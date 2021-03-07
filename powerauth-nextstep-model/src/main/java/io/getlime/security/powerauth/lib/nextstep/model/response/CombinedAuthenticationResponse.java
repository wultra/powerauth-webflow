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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Response object used for authenticating using a credential and an OTP.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CombinedAuthenticationResponse {

    @NotBlank
    @Size(min = 1, max = 256)
    private String userId;
    @NotNull
    private UserIdentityStatus userIdentityStatus;
    private Date timestampBlocked;
    @NotNull
    private CredentialStatus credentialStatus;
    @NotNull
    private OtpStatus otpStatus;
    @NotNull
    private AuthenticationResult authenticationResult;
    @NotNull
    private AuthenticationResult credentialAuthenticationResult;
    @NotNull
    private AuthenticationResult otpAuthenticationResult;
    @PositiveOrZero
    private Integer remainingAttempts;
    @NotNull
    private boolean operationFailed;

}
