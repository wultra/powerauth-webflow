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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Crud repository for persistence of user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface UserIdentityRepository extends CrudRepository<UserIdentityEntity, String> {

    /**
     * Find user identities with given created date.
     * @param startDate Start of interval for created date.
     * @param endDate End of interval for created date.
     * @return List of user identities.
     */
    @Query(value = "from UserIdentityEntity u where u.timestampCreated BETWEEN :startDate AND :endDate")
    List<UserIdentityEntity> findUserIdentitiesByCreatedDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}