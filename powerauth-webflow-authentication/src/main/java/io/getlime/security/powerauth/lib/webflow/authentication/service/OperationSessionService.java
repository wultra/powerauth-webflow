package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.OperationSessionRepository;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.OperationSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Service that handles mapping of operations to sessions and operation identification.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class OperationSessionService {

    private final OperationSessionRepository operationSessionRepository;

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
            return DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-512").digest(operationId.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }

}
