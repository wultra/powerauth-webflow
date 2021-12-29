/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
        final CredentialDefinitionDetail credentialDefinitionDetail = new CredentialDefinitionDetail();
        credentialDefinitionDetail.setCredentialDefinitionName(credentialDefinition.getName());
        credentialDefinitionDetail.setDescription(credentialDefinition.getDescription());
        credentialDefinitionDetail.setCredentialDefinitionStatus(credentialDefinition.getStatus());
        credentialDefinitionDetail.setApplicationName(credentialDefinition.getApplication().getName());
        if (credentialDefinition.getOrganization() != null) {
            credentialDefinitionDetail.setOrganizationId(credentialDefinition.getOrganization().getOrganizationId());
        }
        credentialDefinitionDetail.setCredentialPolicyName(credentialDefinition.getCredentialPolicy().getName());
        credentialDefinitionDetail.setCategory(credentialDefinition.getCategory());
        credentialDefinitionDetail.setEncryptionEnabled(credentialDefinition.isEncryptionEnabled());
        credentialDefinitionDetail.setEncryptionAlgorithm(credentialDefinition.getEncryptionAlgorithm());
        if (credentialDefinition.getHashingConfig() != null) {
            credentialDefinitionDetail.setHashingEnabled(true);
            credentialDefinitionDetail.setHashConfigName(credentialDefinition.getHashingConfig().getName());
        }
        credentialDefinitionDetail.setE2eEncryptionEnabled(credentialDefinition.isE2eEncryptionEnabled());
        credentialDefinitionDetail.setE2eEncryptionAlgorithm(credentialDefinition.getE2eEncryptionAlgorithm());
        credentialDefinitionDetail.setE2eEncryptionCipherTransformation(credentialDefinition.getE2eEncryptionCipherTransformation());
        credentialDefinitionDetail.setE2eEncryptionForTemporaryCredentialEnabled(credentialDefinition.isE2eEncryptionForTemporaryCredentialEnabled());
        credentialDefinitionDetail.setDataAdapterProxyEnabled(credentialDefinition.isDataAdapterProxyEnabled());
        credentialDefinitionDetail.setTimestampCreated(credentialDefinition.getTimestampCreated());
        credentialDefinitionDetail.setTimestampLastUpdated(credentialDefinition.getTimestampLastUpdated());
        return credentialDefinitionDetail;
    }

}