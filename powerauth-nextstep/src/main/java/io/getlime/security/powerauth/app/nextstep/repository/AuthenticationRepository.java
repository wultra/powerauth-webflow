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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthenticationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.stream.Stream;

/**
 * Crud repository for persistence of authentication events.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface AuthenticationRepository extends CrudRepository<AuthenticationEntity, String> {

    /**
     * Find all authentication entities by user identity.
     * @param userId User ID.
     * @return Stream of authentication entities.
     */
    Stream<AuthenticationEntity> findAllByUserIdOrderByTimestampCreatedDesc(String userId);

    /**
     * Find authentication entities by user identity and created date.
     * @param userId User ID.
     * @param startDate Created date range start.
     * @param endDate Created date range end.
     * @return Stream of authentication entities.
     */
    @Query(value = "from AuthenticationEntity a where a.userId = :userId AND a.timestampCreated BETWEEN :startDate AND :endDate ORDER BY a.timestampCreated DESC")
    Stream<AuthenticationEntity> findAuthenticationsByUserIdAndCreatedDate(@Param("userId") String userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}