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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDetail;

/**
 * Converter for credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialConverter {

    /**
     * Convert credential entity to detail.
     * @param credential Credential entity.
     * @return Credential detail.
     */
    public CredentialDetail fromEntity(CredentialEntity credential) {
        final CredentialDetail credentialDetail = new CredentialDetail();
        credentialDetail.setCredentialName(credential.getCredentialDefinition().getName());
        credentialDetail.setCredentialType(credential.getType());
        credentialDetail.setCredentialStatus(credential.getStatus());
        credentialDetail.setUsername(credential.getUsername());
        credentialDetail.setTimestampCreated(credential.getTimestampCreated());
        credentialDetail.setTimestampLastUpdated(credential.getTimestampLastUpdated());
        credentialDetail.setTimestampExpires(credential.getTimestampExpires());
        credentialDetail.setTimestampBlocked(credential.getTimestampBlocked());
        credentialDetail.setTimestampLastCredentialChange(credential.getTimestampLastCredentialChange());
        credentialDetail.setTimestampLastUsernameChange(credential.getTimestampLastUsernameChange());
        return credentialDetail;
    }

}