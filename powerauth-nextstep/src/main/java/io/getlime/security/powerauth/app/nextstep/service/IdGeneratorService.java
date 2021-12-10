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

package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.OperationHistoryRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * This service is used for generating ids for entities used to persist operations and their history.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class IdGeneratorService {

    private final OperationHistoryRepository operationHistoryRepository;

    /**
     * ID generator constructor.
     * @param repositoryCatalogue Repository catalogue.
     */
    @Autowired
    public IdGeneratorService(RepositoryCatalogue repositoryCatalogue) {
        this.operationHistoryRepository = repositoryCatalogue.getOperationHistoryRepository();
    }

    /**
     * Generate random operationId using UUID.randomUUID().
     *
     * @return Generated operation ID.
     */
    public String generateOperationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate credential ID.
     * @return Credential ID.
     */
    public String generateCredentialId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate OTP ID.
     * @return OTP ID.
     */
    public String generateOtpId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate authentication ID.
     * @return Authentication ID.
     */
    public String generateAuthenticationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a new id for for OperationHistory for given operation.
     *
     * @param operationId Operation ID.
     * @return Generated OperationHistory ID.
     */
    public synchronized Long generateOperationHistoryId(String operationId) {
        final Long maxId = operationHistoryRepository.findMaxResultId(operationId);
        if (maxId == null) {
            return 1L;
        } else {
            return maxId + 1;
        }
    }
}
