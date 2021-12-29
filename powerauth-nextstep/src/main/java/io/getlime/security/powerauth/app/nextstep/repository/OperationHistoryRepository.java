/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
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
     * Find the newest resultId for given operation.
     *
     * @param operationId id of an operation
     * @return newest resultId
     */
    @Query("SELECT max(h.primaryKey.resultId) FROM OperationHistoryEntity h WHERE h.primaryKey.operationId=?1")
    Long findMaxResultId(String operationId);

    /**
     * Count number of records with given request authentication method.
     * @param authMethod Authentication method.
     * @return Number of records with given request authentication method.
     */
    long countByRequestAuthMethod(AuthMethod authMethod);

    /**
     * Count number of records with given chosen authentication method.
     * @param authMethod Authentication method.
     * @return Number of records with given chosen authentication method.
     */
    long countByChosenAuthMethod(AuthMethod authMethod);
}
