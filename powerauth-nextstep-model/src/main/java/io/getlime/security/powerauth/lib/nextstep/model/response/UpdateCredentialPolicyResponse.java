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

import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UsernameGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Response object used for updating a credential policy.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class UpdateCredentialPolicyResponse {

    @NotNull
    private String credentialPolicyName;
    private String description;
    private Integer usernameLengthMin;
    private Integer usernameLengthMax;
    private String usernameAllowedPattern;
    private Integer credentialLengthMin;
    private Integer credentialLengthMax;
    private Integer limitSoft;
    private Integer limitHard;
    private int checkHistoryCount;
    private boolean rotationEnabled;
    private Integer rotationDays;
    @NotNull
    private String usernameGenAlgorithm;
    @NotNull
    private UsernameGenerationParam usernameGenParam;
    @NotNull
    private String credentialGenAlgorithm;
    @NotNull
    private CredentialGenerationParam credentialGenParam;
    @NotNull
    private CredentialValidationParam credentialValParam;
    @NotNull
    private CredentialPolicyStatus credentialPolicyStatus;

}
