/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2025 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ExternalCredentialDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;

/**
 * Service for verifying credentials and reading external credential status using LDAP.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
public interface LdapVerifierService {

    /**
     * Verifies credential in configured LDAP.
     *
     * @param credential credentials to verify - credentials can have set up special base search to override server configuration.
     * @param credentialValue credential value to verify.
     * @return result of authentication, i.e. FAILED or SUCCEEDED
     */
    AuthenticationResult verifyCredential(CredentialEntity credential, String credentialValue) throws InvalidRequestException;

    /**
     * Get attempt counter from LDAP
     *
     * @param credential credentials to get attributes for - credentials can have set up special base search to override server configuration.
     * @return number of attempts.
     */
    ExternalCredentialDetail readCredentialExternalStatus(CredentialEntity credential) throws InvalidRequestException;
}
