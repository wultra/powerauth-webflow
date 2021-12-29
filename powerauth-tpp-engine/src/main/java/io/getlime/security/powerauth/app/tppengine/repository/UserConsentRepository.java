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

package io.getlime.security.powerauth.app.tppengine.repository;

import io.getlime.security.powerauth.app.tppengine.repository.model.entity.UserConsentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface used for storing approved and removing rejected consents
 * of a user and TPP app.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
public interface UserConsentRepository extends CrudRepository<UserConsentEntity, Long> {

    @Query("SELECT uc FROM UserConsentEntity uc WHERE uc.userId = :userId AND uc.clientId = :clientId AND uc.consentId = :consentId")
    Optional<UserConsentEntity> findConsentStatus(@Param("userId") String userId, @Param("consentId") String consentId, @Param("clientId") String clientId);

    @Query("SELECT uc FROM UserConsentEntity uc WHERE uc.userId = :userId")
    List<UserConsentEntity> findAllConsentsGivenByUser(@Param("userId") String userId);

    @Query("SELECT uc FROM UserConsentEntity uc WHERE uc.userId = :userId AND uc.clientId = :clientId")
    List<UserConsentEntity> findConsentsGivenByUserToApp(@Param("userId") String userId, @Param("clientId") String clientId);

}
