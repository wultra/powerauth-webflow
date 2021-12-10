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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * Crud repository for persistence of user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface UserIdentityRepository extends CrudRepository<UserIdentityEntity, String> {

    /**
     * Find user identities by their identifiers.
     * @param userIds User IDs.
     * @return Stream of user identities.
     */
    Stream<UserIdentityEntity> findAllByUserIdIn(List<String> userIds);

    /**
     * Find user identities with given created date.
     * @param startDate Start of interval for created date.
     * @param endDate End of interval for created date.
     * @return Stream of user identities.
     */
    @Query(value = "from UserIdentityEntity u where u.timestampCreated BETWEEN :startDate AND :endDate")
    Stream<UserIdentityEntity> findUserIdentitiesByCreatedDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}