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
package io.getlime.security.powerauth.app.nextstep.repository;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    List<CredentialEntity> findAllByCredentialDefinitionAndStatus(CredentialDefinitionEntity credentialDefinition, CredentialStatus status);

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