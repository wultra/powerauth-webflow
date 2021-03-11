/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDefinitionDetail;

/**
 * Converter for credential definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialDefinitionConverter {

    /**
     * Convert credential definition from entity to detail.
     * @param credentialDefinition Credential definition entity.
     * @return Credential definition detail.
     */
    public CredentialDefinitionDetail fromEntity(CredentialDefinitionEntity credentialDefinition) {
        CredentialDefinitionDetail credentialDefinitionDetail = new CredentialDefinitionDetail();
        credentialDefinitionDetail.setCredentialDefinitionName(credentialDefinition.getName());
        credentialDefinitionDetail.setDescription(credentialDefinition.getDescription());
        credentialDefinitionDetail.setCredentialDefinitionStatus(credentialDefinition.getStatus());
        credentialDefinitionDetail.setApplicationName(credentialDefinition.getApplication().getName());
        credentialDefinitionDetail.setCredentialPolicyName(credentialDefinition.getCredentialPolicy().getName());
        credentialDefinitionDetail.setCategory(credentialDefinition.getCategory());
        credentialDefinitionDetail.setEncryptionEnabled(credentialDefinition.isEncryptionEnabled());
        credentialDefinitionDetail.setEncryptionAlgorithm(credentialDefinition.getEncryptionAlgorithm());
        if (credentialDefinition.getHashingConfig() != null) {
            credentialDefinitionDetail.setHashingEnabled(true);
            credentialDefinitionDetail.setHashConfigName(credentialDefinition.getHashingConfig().getName());
        }
        credentialDefinitionDetail.setE2eEncryptionEnabled(credentialDefinition.isE2eEncryptionEnabled());
        credentialDefinitionDetail.setDataAdapterProxyEnabled(credentialDefinition.isDataAdapterProxyEnabled());
        credentialDefinitionDetail.setTimestampCreated(credentialDefinition.getTimestampCreated());
        credentialDefinitionDetail.setTimestampLastUpdated(credentialDefinition.getTimestampLastUpdated());
        return credentialDefinitionDetail;
    }

}