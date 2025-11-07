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
package com.wultra.security.powerauth.app.nextstep.service;

import com.wultra.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import com.wultra.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import com.wultra.security.powerauth.lib.nextstep.model.entity.ExternalCredentialDetail;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import com.wultra.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.stereotype.Service;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Service for verifying credentials and reading external credential status using LDAP.
 *
 * @author Zdenek Cerny, zdenek.cerny@wultra.com
 */
@Service
public class LdapVerifierService {

    private final Logger logger = LoggerFactory.getLogger(LdapVerifierService.class);
    private static final String CONFIG_ERROR = "LDAP_NOT_CONFIGURED";
    private static final String ACCOUNT_LOCK_ATTRIBUTE = "nsAccountLock";
    private static final String PASSWORD_RETRY_COUNT_ATTRIBUTE = "passwordRetryCount";
    private static final String PASSWORD_EXPIRATION_TIME = "passwordExpirationTime";

    private final NextStepServerConfiguration nextStepServerConfiguration;

    private final LdapTemplate ldapTemplate;

    @Autowired
    public LdapVerifierService(NextStepServerConfiguration nextStepServerConfiguration, LdapTemplate ldapTemplate) {
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.ldapTemplate = ldapTemplate;
    }

    private String resolveUserSearchBase(CredentialEntity credential) {

        final String userSearchBase;
        // base search for user can be configured per customer or on server level
        if (credential.getExternalReference() != null) {
            userSearchBase = credential.getExternalReference();
        } else {
            userSearchBase = nextStepServerConfiguration.getUserSearchBase();
        }
        return userSearchBase;
    }

    /**
     * Verifies credential in configured LDAP.
     *
     * @param credential credentials to verify - credentials can have set up special base search to override server configuration.
     * @param credentialValue credential value to verify.
     * @return result of authentication, i.e. FAILED or SUCCEEDED
     */
    public AuthenticationResult verifyCredential(CredentialEntity credential, String credentialValue) throws InvalidRequestException {
        try {
            final String userSearchBase = resolveUserSearchBase(credential);
            if (userSearchBase == null) {
                logger.error("action: verifyCredential, state: failed, reason: {}", CONFIG_ERROR);
                throw new InvalidRequestException(CONFIG_ERROR);
            }

            // Build a search query to find the user entry
            LdapQuery query = query()
                    .base(userSearchBase)
                    .searchScope(SearchScope.SUBTREE)
                    .filter(nextStepServerConfiguration.getUserSearchFilter(), credential.getUser().getUserId());
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
            Attribute a = attrs.get(name);
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
            Attribute a = attrs.get(name);
            if (a == null) {
                return null;
            }
            return Integer.valueOf(a.get().toString());
        } catch (javax.naming.NamingException | NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get attempt counter from LDAP
     *
     * @param credential credentials to get attributes for - credentials can have set up special base search to override server configuration.
     * @return number of attempts.
     */
    public ExternalCredentialDetail readCredentialExternalStatus(CredentialEntity credential) throws InvalidRequestException {
        final String userSearchBase = resolveUserSearchBase(credential);

        if (userSearchBase == null) {
            logger.error("action: readCredentialExternalStatus, state: failed, reason: {}", CONFIG_ERROR);
            throw new InvalidRequestException(CONFIG_ERROR);
        }
        var query = LdapQueryBuilder.query()
                .base(userSearchBase)
                .attributes(PASSWORD_RETRY_COUNT_ATTRIBUTE, ACCOUNT_LOCK_ATTRIBUTE, PASSWORD_EXPIRATION_TIME)
                .filter(nextStepServerConfiguration.getUserSearchFilter(), credential.getUser().getUserId());

        return ldapTemplate.search(query, (AttributesMapper<ExternalCredentialDetail>) attrs -> {
            ExternalCredentialDetail externalCredentialDetail = new ExternalCredentialDetail();

            final Boolean blocked = getBoolAttrSafe(attrs, ACCOUNT_LOCK_ATTRIBUTE);
            if (blocked != null) {
                externalCredentialDetail.setCredentialStatus(blocked ? CredentialStatus.BLOCKED_TEMPORARY : CredentialStatus.ACTIVE);
            }

            final Integer attemptCounter = getIntAttrSafe(attrs, PASSWORD_RETRY_COUNT_ATTRIBUTE);
            externalCredentialDetail.setFailedAttempts(attemptCounter);

            return externalCredentialDetail;
        }).stream().findFirst().orElse(new ExternalCredentialDetail());
    }
}
