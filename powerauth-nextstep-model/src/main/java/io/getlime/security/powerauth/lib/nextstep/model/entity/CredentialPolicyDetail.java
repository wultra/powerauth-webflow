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

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Class represents details of a credential policy.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@EqualsAndHashCode(of = "credentialPolicyName")
public class CredentialPolicyDetail {

    @NotNull
    private String credentialPolicyName;
    private String description;
    private Long usernameLengthMin;
    private Long usernameLengthMax;
    private String usernameAllowedChars;
    private Long passwordLengthMin;
    private Long passwordLengthMax;
    private String passwordAllowedChars;
    private Long limitSoft;
    private Long limitHard;
    private long checkHistoryCount;
    private boolean rotationEnabled;
    private String usernameGenAlgorithm;
    private String passwordGenAlgorithm;
    @NotNull
    private Date timestampCreated;
    @NotNull
    private Date timestampLastUpdated;

}
