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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Crud repository for persistence of step definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Repository
public interface StepDefinitionRepository extends CrudRepository<StepDefinitionEntity, Long> {

    /**
     * Finds all step definitions for operation specified by its name.
     *
     * @param operationName name of the operation
     * @return List of step definitions
     */
    @Query("SELECT sd FROM StepDefinitionEntity sd WHERE sd.operationName=?1 ORDER BY sd.stepDefinitionId")
    List<StepDefinitionEntity> findStepDefinitionsForOperation(String operationName);

    /**
     * Finds all distinct operation names.
     *
     * @return List of operation names
     */
    @Query("SELECT DISTINCT(sd.operationName) FROM StepDefinitionEntity sd")
    List<String> findDistinctOperationNames();

    /**
     * Count number of step definitions with given request authentication method.
     * @param authMethod Authentication method.
     * @return Number of step definitions with given request authentication method.
     */
    long countByRequestAuthMethod(AuthMethod authMethod);

    /**
     * Count number of step definitions with given response authentication method.
     * @param authMethod Authentication method.
     * @return Number of step definitions with given response authentication method.
     */
    long countByResponseAuthMethod(AuthMethod authMethod);

}
