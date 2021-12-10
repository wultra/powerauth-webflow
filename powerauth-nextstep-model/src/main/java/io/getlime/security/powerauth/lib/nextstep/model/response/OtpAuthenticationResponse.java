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
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Response object used for authenticating using an OTP.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class OtpAuthenticationResponse {

    // User ID is null in OTP authentication without user ID
    @Size(min = 1, max = 256)
    private String userId;
    // User identity status is null in OTP authentication without user ID
    private UserIdentityStatus userIdentityStatus;
    // Credential status is sent in case OTP is linked with a credential
    private CredentialStatus credentialStatus;
    private Date timestampBlocked;
    @NotNull
    private OtpStatus otpStatus;
    @NotNull
    private AuthenticationResult authenticationResult;
    @PositiveOrZero
    private Integer remainingAttempts;
    @NotNull
    private boolean showRemainingAttempts;
    @Size(min = 2, max = 256)
    private String errorMessage;
    @NotNull
    private boolean operationFailed;

}
