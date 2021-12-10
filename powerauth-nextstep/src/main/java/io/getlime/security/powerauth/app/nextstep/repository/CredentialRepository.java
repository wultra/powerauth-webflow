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
package io.getlime.security.powerauth.app.nextstep.repository;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Crud repository for persistence of credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface CredentialRepository extends CrudRepository<CredentialEntity, String> {

    /**
     * Find credentials by credential definition and credential status.
     * @param credentialDefinition Credential definition.
     * @param status Credential status.
     * @return Credentials matching query criteria.
     */
    Stream<CredentialEntity> findAllByCredentialDefinitionAndStatus(CredentialDefinitionEntity credentialDefinition, CredentialStatus status);

    /**
     * Find credential by credential name and username.
     * @param credentialDefinition Credential definition.
     * @param username Username.
     * @return Credential matching query criteria.
     */
    Optional<CredentialEntity> findByCredentialDefinitionAndUsername(CredentialDefinitionEntity credentialDefinition, String username);

    /**
     * Reset soft failed attempt counters for credentials in BLOCKED_TEMPORARY status and change credential status to ACTIVE.
     * @return Count of updated credentials.
     */
    @Modifying
    @Query("UPDATE CredentialEntity c SET " +
            "c.status = io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus.ACTIVE, " +
            "c.failedAttemptCounterSoft = 0, " +
            "c.timestampBlocked = null " +
            "WHERE c.status = io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus.BLOCKED_TEMPORARY")
    int resetSoftFailedCountersForBlockedTemporaryStatus();

    /**
     * Reset soft failed attempt counters for credentials in ACTIVE status.
     * @return Count of updated credentials.
     */
    @Modifying
    @Query("UPDATE CredentialEntity c SET " +
            "c.failedAttemptCounterSoft = 0 " +
            "WHERE c.status = io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus.ACTIVE " +
            "AND c.failedAttemptCounterSoft <> 0")
    int resetSoftFailedCountersForActiveStatus();

}