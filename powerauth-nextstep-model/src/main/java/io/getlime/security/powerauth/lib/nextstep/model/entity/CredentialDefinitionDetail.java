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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialCategory;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialDefinitionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Class represents details of a credential definition.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@EqualsAndHashCode(of = {"credentialDefinitionName", "applicationName", "credentialPolicyName"})
public class CredentialDefinitionDetail {

    @NotNull
    private String credentialDefinitionName;
    @NotNull
    private String applicationName;
    @NotNull
    private String credentialPolicyName;
    @NotNull
    private CredentialCategory category;
    private boolean encryptionEnabled;
    private String encryptionAlgorithm;
    private boolean hashingEnabled;
    private String hashConfigName;
    private boolean e2eEncryptionEnabled;
    @NotNull
    private CredentialDefinitionStatus credentialDefinitionStatus;
    @NotNull
    private Date timestampCreated;
    @NotNull
    private Date timestampLastUpdated;

}
