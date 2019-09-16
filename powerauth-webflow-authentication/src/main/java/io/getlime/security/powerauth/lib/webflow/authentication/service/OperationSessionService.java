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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.OperationSessionRepository;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.OperationSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Service that handles mapping of operations to sessions and operation identification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OperationSessionService {

    private final OperationSessionRepository operationSessionRepository;

    /**
     * Service constructor.
     * @param operationSessionRepository Operation session repository.
     */
    @Autowired
    public OperationSessionService(OperationSessionRepository operationSessionRepository) {
        this.operationSessionRepository = operationSessionRepository;
    }

    /**
     * Get operation to HTTP session mapping for given operation.
     * @param operationId Operation ID
     * @return Operation to HTTP session mapping.
     */
    public OperationSessionEntity getOperationToSessionMapping(String operationId) {
        return operationSessionRepository.findByOperationId(operationId);
    }

    /**
     * Persist operation to HTTP session mapping.
     * @param operationId Operation ID.
     * @param httpSessionId HTTP session ID.
     * @param result Operation result.
     */
    public void persistOperationToSessionMapping(String operationId, String httpSessionId, AuthResult result) {
        OperationSessionEntity operationSessionEntity = new OperationSessionEntity(operationId, httpSessionId, result);
        String operationHash = generateOperationHash(operationId);
        operationSessionEntity.setOperationHash(operationHash);
        operationSessionRepository.save(operationSessionEntity);
    }

    /**
     * Update operation result in operation to HTTP session mapping.
     * @param operationId Operation ID.
     * @param result Operation result.
     */
    public void updateOperationResult(String operationId, AuthResult result) {
        OperationSessionEntity operationSessionEntity = operationSessionRepository.findByOperationId(operationId);
        if (operationSessionEntity != null) {
            operationSessionEntity.setResult(result);
            operationSessionRepository.save(operationSessionEntity);
        }
    }

    /**
     * Cancel operations in HTTP session and get their list.
     * @param httpSessionId HTTP session.
     * @return Canceled operations.
     */
    public List<OperationSessionEntity> cancelOperationsInHttpSession(String httpSessionId) {
        // cancel previous operations in Next Step and update operation to HTTP session mapping
        List<OperationSessionEntity> previousOperations = operationSessionRepository.findActiveOperationsByHttpSessionId(httpSessionId);
        for (OperationSessionEntity previousOperation : previousOperations) {
            previousOperation.setResult(AuthResult.FAILED);
            operationSessionRepository.save(previousOperation);
        }
        return previousOperations;
    }

    /**
     * Generate hash for given operationId.
     * @param operationId Operation ID.
     * @return SHA-512 hash of operation ID.
     */
    public String generateOperationHash(String operationId) {
        if (operationId == null) {
            return null;
        }
        try {
            return DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-512").digest(operationId.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Lookup Web Socket Session ID based on operation hash.
     * @param operationHash Operation hash.
     * @return Web Socket Session ID or null if session was not found.
     */
    public String lookupWebSocketSessionIdByOperationHash(String operationHash) {
        OperationSessionEntity operationSessionEntity = operationSessionRepository.findByOperationHash(operationHash);
        if (operationSessionEntity != null) {
            return operationSessionEntity.getWebSocketSessionId();
        }
        return null;
    }

    /**
     * Lookup Operation ID based on Web Socket session ID.
     * @param webSocketSessionId Web Socket session ID.
     * @return Operation ID or null if session was not found.
     */
    public String lookupOperationIdByWebSocketSessionId(String webSocketSessionId) {
        OperationSessionEntity operationSessionEntity = operationSessionRepository.findByWebSocketSessionId(webSocketSessionId);
        if (operationSessionEntity != null) {
            return operationSessionEntity.getOperationId();
        }
        return null;
    }

    /**
     * Lookup an operation by operation hash and store Web Socket session ID.
     * @param operationHash Operation hash.
     * @param webSocketSessionId Web Socket session ID.
     * @param clientIp Remote client IP address.
     */
    public void storeWebSocketSessionId(String operationHash, String webSocketSessionId, String clientIp) {
        OperationSessionEntity operationSessionEntity = operationSessionRepository.findByOperationHash(operationHash);
        if (operationSessionEntity != null) {
            operationSessionEntity.setWebSocketSessionId(webSocketSessionId);
            operationSessionEntity.setClientIp(clientIp);
            operationSessionRepository.save(operationSessionEntity);
        }
    }

}
