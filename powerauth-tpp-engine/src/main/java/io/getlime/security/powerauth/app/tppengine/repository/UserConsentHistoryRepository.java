/*
 * Copyright 2019 Wultra s.r.o.
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
