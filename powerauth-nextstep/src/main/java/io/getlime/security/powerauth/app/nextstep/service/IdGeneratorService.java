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

package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.OperationHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * This service is used for generating ids for entities used to persist operations and their history.
 *
 * @author Roman Strobl
 */
@Service
public class IdGeneratorService {

    private OperationHistoryRepository operationHistoryRepository;

    public IdGeneratorService(OperationHistoryRepository operationHistoryRepository) {
        this.operationHistoryRepository = operationHistoryRepository;
    }

    /**
     * Generates random operationId using UUID.randomUUID().
     *
     * @return generated operationId
     */
    public String generateOperationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a new id for for OperationHistory for given operation.
     *
     * @param operationId id specifying an operation
     * @return generated id for OperationHistory
     */
    public synchronized Long generateOperationHistoryId(String operationId) {
        Long maxId = operationHistoryRepository.findMaxResultId(operationId);
        if (maxId == null) {
            return 1L;
        } else {
            return maxId + 1;
        }
    }
}
