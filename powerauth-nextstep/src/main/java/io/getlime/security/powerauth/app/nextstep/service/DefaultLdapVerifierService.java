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

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepLdapConfigurationProperties;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ExternalCredentialDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Specialization of {@link LdapVerifierService} which uses Next Step configuration for LDAP.
 *
 * @author Zdenek Cerny, zdenek.cerny@wultra.com
 */
@Slf4j
@AllArgsConstructor
public class DefaultLdapVerifierService implements LdapVerifierService {

    private static final String CONFIG_ERROR = "LDAP_NOT_CONFIGURED";
    private static final String ACCOUNT_LOCK_ATTRIBUTE = "nsAccountLock";
    private static final String PASSWORD_RETRY_COUNT_ATTRIBUTE = "passwordRetryCount";
    private static final String PASSWORD_EXPIRATION_TIME = "passwordExpirationTime";

    private final NextStepLdapConfigurationProperties configuration;

    private final LdapTemplate ldapTemplate;

    private String resolveUserSearchBase(CredentialEntity credential) {

        final String userSearchBase;
        // base search for user can be configured per customer or on server level
        if (credential.getExternalReference() != null) {
            userSearchBase = credential.getExternalReference();
        } else {
            userSearchBase = configuration.getUserSearchBase();
        }
        return userSearchBase;
    }

    @Override
    public AuthenticationResult verifyCredential(CredentialEntity credential, String credentialValue) throws InvalidRequestException {
        try {
            final String userSearchBase = resolveUserSearchBase(credential);
            if (userSearchBase == null) {
                logger.error("action: verifyCredential, state: failed, reason: {}", CONFIG_ERROR);
                throw new InvalidRequestException(CONFIG_ERROR);
            }

            // Build a search query to find the user entry
            final LdapQuery query = query()
                    .base(userSearchBase)
                    .searchScope(SearchScope.SUBTREE)
                    .filter(configuration.getUserSearchFilter(), credential.getUser().getUserId());
            // Spring LDAP will do: search -> resolve DN -> attempt bind with that DN+password
            ldapTemplate.authenticate(query, credentialValue);
            return AuthenticationResult.SUCCEEDED;
        } catch (EmptyResultDataAccessException | NamingException e) {
            logger.warn("action: verifyCredential, state: failed, reason: {}", e.getMessage(), e);
            return AuthenticationResult.FAILED;
        }
    }

    private static Boolean getBoolAttrSafe(Attributes attrs, String name)  {
        try {
            final Attribute a = attrs.get(name);
            if (a == null) {
                return null;
            }
            return Boolean.parseBoolean(a.get().toString());
        } catch (javax.naming.NamingException e) {
            return null;
        }
    }

    private static Integer getIntAttrSafe(Attributes attrs, String name)  {
        try {
            final Attribute a = attrs.get(name);
            if (a == null) {
                return null;
            }
            return Integer.valueOf(a.get().toString());
        } catch (javax.naming.NamingException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ExternalCredentialDetail readCredentialExternalStatus(CredentialEntity credential) throws InvalidRequestException {
        final String userSearchBase = resolveUserSearchBase(credential);

        if (userSearchBase == null) {
            logger.error("action: readCredentialExternalStatus, state: failed, reason: {}", CONFIG_ERROR);
            throw new InvalidRequestException(CONFIG_ERROR);
        }
        final var query = LdapQueryBuilder.query()
                .base(userSearchBase)
                .attributes(PASSWORD_RETRY_COUNT_ATTRIBUTE, ACCOUNT_LOCK_ATTRIBUTE, PASSWORD_EXPIRATION_TIME)
                .filter(configuration.getUserSearchFilter(), credential.getUser().getUserId());

        return ldapTemplate.search(query, attributeMapper()).stream()
                .findFirst()
                .orElse(new ExternalCredentialDetail());
    }

    private static AttributesMapper<ExternalCredentialDetail> attributeMapper() {
        return attrs -> {
            final ExternalCredentialDetail externalCredentialDetail = new ExternalCredentialDetail();

            final Boolean blocked = getBoolAttrSafe(attrs, ACCOUNT_LOCK_ATTRIBUTE);
            if (blocked != null) {
                externalCredentialDetail.setCredentialStatus(blocked ? CredentialStatus.BLOCKED_TEMPORARY : CredentialStatus.ACTIVE);
            }

            final Integer attemptCounter = getIntAttrSafe(attrs, PASSWORD_RETRY_COUNT_ATTRIBUTE);
            externalCredentialDetail.setFailedAttempts(attemptCounter);

            return externalCredentialDetail;
        };
    }
}
