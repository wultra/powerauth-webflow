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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialCategory;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request object used for creating a credential definition.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class CreateCredentialDefinitionRequest {

    @NotBlank
    @Size(min = 2, max = 256)
    private String credentialDefinitionName;
    @NotBlank
    @Size(min = 2, max = 256)
    private String applicationName;
    @NotBlank
    @Size(min = 2, max = 256)
    private String credentialPolicyName;
    @Size(min = 2, max = 256)
    private String description;
    @NotNull
    private CredentialCategory category;
    private boolean encryptionEnabled;
    @Size(min = 2, max = 256)
    private String encryptionAlgorithm;
    private boolean hashingEnabled;
    @Size(min = 2, max = 256)
    private String hashConfigName;
    private boolean e2eEncryptionEnabled;
    private boolean dataAdapterProxyEnabled;

}

