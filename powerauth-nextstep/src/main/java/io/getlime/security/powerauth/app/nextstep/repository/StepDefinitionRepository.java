/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Crud repository for persistence of step definitions.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Component
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
     * Finds all distict operation names.
     *
     * @return List of operation names
     */
    @Query("SELECT DISTINCT(sd.operationName) FROM StepDefinitionEntity sd")
    List<String> findDistinctOperationNames();

}
