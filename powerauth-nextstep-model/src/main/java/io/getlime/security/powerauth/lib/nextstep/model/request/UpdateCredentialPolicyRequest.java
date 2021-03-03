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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Request object used for updating a credential policy.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class UpdateCredentialPolicyRequest {

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
    private String usernameAllowedChars;
    @Positive
    private Integer credentialLengthMin;
    @Positive
    private Integer credentialLengthMax;
    @Size(min = 2, max = 256)
    private String credentialAllowedChars;
    @Positive
    private Integer limitSoft;
    @Positive
    private Integer limitHard;
    private int checkHistoryCount;
    private boolean rotationEnabled;
    @Positive
    private Integer rotationDays;
    @Size(min = 2, max = 256)
    private String usernameGenAlgorithm;
    private Map<String, String> usernameGenParam = new LinkedHashMap<>();
    @Size(min = 2, max = 256)
    private String credentialGenAlgorithm;
    private Map<String, String> credentialGenParam = new LinkedHashMap<>();
    private CredentialPolicyStatus credentialPolicyStatus;

}
