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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Crud repository for persistence of operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
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

    /**
     * Count number of operations with given organization.
     * @param organization Organization entity.
     * @return Number of operations with given organization.
     */
    long countByOrganization(OrganizationEntity organization);

    /**
     * Count number of operations with given operation name.
     * @param operationName operation name.
     * @return Number of operations with given operation name.
     */
    long countByOperationName(String operationName);

}
