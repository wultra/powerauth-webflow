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

import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UsernameGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.CredentialGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.UsernameGenerationAlgorithm;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request object used for creating a credential policy.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateCredentialPolicyRequest {

    @NotBlank
    @Size(min = 2, max = 256)
    private String credentialPolicyName;
    @Size(min = 2, max = 256)
    private String description;
    @Positive
    private Integer usernameLengthMin;
    @Positive
    private Integer usernameLengthMax;
    @Size(min = 2, max = 256)
    private String usernameAllowedPattern;
    @Positive
    private Integer credentialLengthMin;
    @Positive
    private Integer credentialLengthMax;
    @Positive
    private Integer limitSoft;
    @Positive
    private Integer limitHard;
    @PositiveOrZero
    private int checkHistoryCount;
    private boolean rotationEnabled;
    @Positive
    private Integer rotationDays;
    @Positive
    private Integer temporaryCredentialExpirationTime;
    @NotNull
    private UsernameGenerationAlgorithm usernameGenAlgorithm;
    @NotNull
    private UsernameGenerationParam usernameGenParam = new UsernameGenerationParam();
    @NotNull
    private CredentialGenerationAlgorithm credentialGenAlgorithm;
    @NotNull
    private CredentialGenerationParam credentialGenParam = new CredentialGenerationParam();
    @NotNull
    private CredentialValidationParam credentialValParam = new CredentialValidationParam();

}

