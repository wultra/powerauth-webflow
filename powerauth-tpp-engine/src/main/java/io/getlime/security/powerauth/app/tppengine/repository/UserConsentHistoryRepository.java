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

import io.getlime.security.powerauth.app.tppengine.repository.model.entity.UserConsentHistoryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository responsible for storing history of adding and removing consents of given user
 * to TPP app.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Repository
public interface UserConsentHistoryRepository extends CrudRepository<UserConsentHistoryEntity, Long> {

    @Query("SELECT che FROM UserConsentHistoryEntity che WHERE che.userId = :userId")
    List<UserConsentHistoryEntity> consentHistoryForUser(@Param("userId") String userId);

    @Query("SELECT che FROM UserConsentHistoryEntity che WHERE che.userId = :userId AND che.clientId = :clientId")
    List<UserConsentHistoryEntity> consentHistoryForUser(@Param("userId") String userId, @Param("clientId") String clientId);

}
