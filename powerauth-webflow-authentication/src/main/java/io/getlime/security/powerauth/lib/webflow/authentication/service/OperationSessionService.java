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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.OperationSessionRepository;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.OperationSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

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
     * @return Whether HTTP session was successfully registered for the operation.
     */
    public boolean registerHttpSession(String operationId, String httpSessionId, AuthResult result) {
        OperationSessionEntity existingSession = getOperationToSessionMapping(operationId);
        if (existingSession != null) {
            // Registration failed because operation is being accessed from another HTTP session
            return false;
        }
        OperationSessionEntity operationSessionEntity = new OperationSessionEntity(operationId, httpSessionId, result);
        String operationHash = generateOperationHash(operationId);
        operationSessionEntity.setOperationHash(operationHash);
        operationSessionRepository.save(operationSessionEntity);
        return true;
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
            return String.valueOf(Hex.encode(MessageDigest.getInstance("SHA-512").digest(operationId.getBytes(StandardCharsets.UTF_8))));
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
     * @param clientIpAddress Remote client IP address.
     * @return Whether Web Socket session ID was successfully registered for the operation.
     */
    public boolean registerWebSocketSession(String operationHash, String webSocketSessionId, String clientIpAddress) {
        OperationSessionEntity operationSessionEntity = operationSessionRepository.findByOperationHash(operationHash);
        if (operationSessionEntity == null) {
            // Registration failed because operation was not found
            return false;
        }
        String existingWebSocketSessionId = operationSessionEntity.getWebSocketSessionId();
        if (existingWebSocketSessionId != null) {
            // Registration failed because operation is being accessed from another browser tab / window or Web Socket
            // session has been already registered.
            return false;
        }
        operationSessionEntity.setWebSocketSessionId(webSocketSessionId);
        operationSessionEntity.setClientIp(clientIpAddress);
        operationSessionRepository.save(operationSessionEntity);
        // Registration succeeded
        return true;
    }

}
