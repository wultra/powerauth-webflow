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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialCategory;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialDefinitionStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EndToEndEncryptionAlgorithm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Class represents details of a credential definition.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@EqualsAndHashCode(of = {"credentialDefinitionName", "applicationName", "credentialPolicyName"})
public class CredentialDefinitionDetail {

    @NotBlank
    @Size(min = 2, max = 256)
    private String credentialDefinitionName;
    @NotBlank
    @Size(min = 2, max = 256)
    private String applicationName;
    @Size(min = 2, max = 256)
    private String organizationId;
    @NotBlank
    @Size(min = 2, max = 256)
    private String credentialPolicyName;
    @Size(min = 2, max = 256)
    private String description;
    @NotNull
    private CredentialCategory category;
    @NotNull
    private boolean encryptionEnabled;
    private EncryptionAlgorithm encryptionAlgorithm;
    @NotNull
    private boolean hashingEnabled;
    @Size(min = 2, max = 256)
    private String hashConfigName;
    @NotNull
    private boolean e2eEncryptionEnabled;
    private EndToEndEncryptionAlgorithm e2eEncryptionAlgorithm;
    private String e2eEncryptionCipherTransformation;
    private boolean e2eEncryptionForTemporaryCredentialEnabled;
    @NotNull
    private CredentialDefinitionStatus credentialDefinitionStatus;
    @NotNull
    private boolean dataAdapterProxyEnabled;
    @NotNull
    private Date timestampCreated;
    private Date timestampLastUpdated;

}
