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
package io.getlime.security.powerauth.lib.webflow.authentication.repository;

import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.OperationSessionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Crud repository for persistence of mapping of operations to HTTP sessions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public interface OperationSessionRepository extends CrudRepository<OperationSessionEntity, String> {

    @Query("SELECT o FROM OperationSessionEntity o WHERE o.httpSessionId = ?1 AND o.result='CONTINUE'")
    List<OperationSessionEntity> findActiveOperationsByHttpSessionId(String httpSessionId);

    OperationSessionEntity findByOperationId(String operationId);

    OperationSessionEntity findByOperationHash(String operationHash);

    OperationSessionEntity findByWebSocketSessionId(String operationHash);

}
