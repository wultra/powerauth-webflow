package io.getlime.security.powerauth.lib.webflow.authentication.service;


import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service that handles identification of operations without exposing operation ID.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class OperationIdentificationService {

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
