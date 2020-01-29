/*
 * Copyright 2017 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Crud repository for persistence of operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public interface OperationRepository extends CrudRepository<OperationEntity, String> {

    /**
     * Finds all pending operations for user.
     *
     * @param userId user ID
     * @return List of pending operations.
     */
    @Query("SELECT o FROM OperationEntity o WHERE o.userId=?1 AND o.result='CONTINUE' " +
            "AND o.timestampExpires > CURRENT_TIMESTAMP ORDER BY o.timestampExpires")
    List<OperationEntity> findPendingOperationsForUser(String userId);

    /**
     * Find operations by external transaction ID.
     * @param externalTransactionId External transaction ID.
     * @return List of operations matching the query.
     */
    List<OperationEntity> findAllByExternalTransactionId(String externalTransactionId);

}
