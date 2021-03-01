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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthenticationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

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
     * @return List of authentication entities.
     */
    List<AuthenticationEntity> findAllByUserIdOrderByTimestampCreatedDesc(String userId);

    @Query(value = "from AuthenticationEntity a where a.userId = :userId AND a.timestampCreated BETWEEN :startDate AND :endDate ORDER BY a.timestampCreated DESC")
    List<AuthenticationEntity> findAuthenticationsByUserIdAndCreatedDate(@Param("userId") String userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}