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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Crud repository for persistence of operation history.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface OperationHistoryRepository extends CrudRepository<OperationHistoryEntity, OperationHistoryEntity.OperationHistoryKey> {

    /**
     * Finds the newest resultId for given operation.
     *
     * @param operationId id of an operation
     * @return newest resultId
     */
    @Query("SELECT max(h.primaryKey.resultId) FROM OperationHistoryEntity h WHERE h.primaryKey.operationId=?1")
    Long findMaxResultId(String operationId);

}
