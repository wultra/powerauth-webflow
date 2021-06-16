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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UsernameGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.CredentialGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.UsernameGenerationAlgorithm;
import lombok.Data;

import javax.validation.constraints.*;

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

