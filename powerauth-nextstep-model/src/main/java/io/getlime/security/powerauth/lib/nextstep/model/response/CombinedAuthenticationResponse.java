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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
    private boolean credentialChangeRequired;
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
    private boolean showRemainingAttempts;
    @Size(min = 2, max = 256)
    private String errorMessage;
    @NotNull
    private boolean operationFailed;

}
