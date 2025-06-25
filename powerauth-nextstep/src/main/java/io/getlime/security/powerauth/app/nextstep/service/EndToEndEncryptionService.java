/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import com.wultra.security.powerauth.crypto.lib.generator.KeyGenerator;
import com.wultra.security.powerauth.crypto.lib.model.exception.CryptoProviderException;
import com.wultra.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import com.wultra.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import com.wultra.security.powerauth.crypto.lib.util.KeyConvertor;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EndToEndEncryptionAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.EncryptionException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;

/**
 * Service which handles end-to-end encryption.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class EndToEndEncryptionService {

    private final Logger logger = LoggerFactory.getLogger(EndToEndEncryptionService.class);

    private final NextStepServerConfiguration configuration;

    private final KeyGenerator keyGenerator = new KeyGenerator();
    private final KeyConvertor keyConvertor = new KeyConvertor();
    private final AESEncryptionUtils aes = new AESEncryptionUtils();

    /**
     * End-to-end encryption service constructor.
     * @param configuration Next Step configuration.
     */
    @Autowired
    public EndToEndEncryptionService(NextStepServerConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Encrypt a plain text credential value.
     * @param credentialValue Plain text credential value.
     * @param credentialDefinition Credential definition.
     * @return Encrypted credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption fails.
     */
    public String encryptCredential(String credentialValue, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException, EncryptionException {
        if (!credentialDefinition.isE2eEncryptionEnabled()) {
            return credentialValue;
        }
        final EndToEndEncryptionAlgorithm algorithm = credentialDefinition.getE2eEncryptionAlgorithm();
        if (algorithm == null) {
            throw new InvalidConfigurationException("End-to-end encryption algorithm is missing");
        }
        if (algorithm != EndToEndEncryptionAlgorithm.AES) {
            throw new InvalidConfigurationException("End-to-end encryption algorithm is not supported: " + algorithm);
        }
        final String cipherTransformation = credentialDefinition.getE2eEncryptionCipherTransformation();
        if (cipherTransformation == null || cipherTransformation.isEmpty()) {
            throw new InvalidConfigurationException("End-to-end encryption cipher transformation is missing");
        }
        final String e2eEncryptionKey = configuration.getE2eEncryptionKey();
        if (e2eEncryptionKey == null || e2eEncryptionKey.isEmpty()) {
            throw new InvalidConfigurationException("End-to-end encryption key is missing");
        }
        try {
            // Convert secret key from Base64 String to SecretKey
            final byte[] secretKeyBytes = Base64.getDecoder().decode(e2eEncryptionKey);
            final SecretKey secretKey = keyConvertor.convertBytesToSharedSecretKey(secretKeyBytes);

            final byte[] ivBytes = keyGenerator.generateRandomBytes(16);
            // Encrypt password bytes using random IV, secret key and transformation
            final byte[] credentialValueBytes = credentialValue.getBytes(StandardCharsets.UTF_8);
            final byte[] encryptedCredentialBytes = aes.encrypt(credentialValueBytes, ivBytes, secretKey, cipherTransformation);
            final String encryptedCredentialBase64 = Base64.getEncoder().encodeToString(encryptedCredentialBytes);
            final String ivBase64 = Base64.getEncoder().encodeToString(ivBytes);
            return ivBase64 + ":" + encryptedCredentialBase64;
        } catch (CryptoProviderException | InvalidKeyException | GenericCryptoException ex) {
            throw new EncryptionException(ex);
        }
    }

    /**
     * Decrypt an encrypted credential value.
     * @param encryptedValue Encrypted credential value.
     * @param credentialDefinition Credential definition.
     * @return Decrypted credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public String decryptCredential(String encryptedValue, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException, InvalidRequestException, EncryptionException {
        if (!credentialDefinition.isE2eEncryptionEnabled()) {
            return encryptedValue;
        }
        if (!encryptedValue.contains(":")) {
            throw new InvalidRequestException("Invalid format of encrypted credential value");
        }
        final EndToEndEncryptionAlgorithm algorithm = credentialDefinition.getE2eEncryptionAlgorithm();
        if (algorithm == null) {
            throw new InvalidConfigurationException("End-to-end encryption algorithm is missing");
        }
        if (algorithm != EndToEndEncryptionAlgorithm.AES) {
            throw new InvalidConfigurationException("End-to-end encryption algorithm is not supported: " + algorithm);
        }
        final String cipherTransformation = credentialDefinition.getE2eEncryptionCipherTransformation();
        if (cipherTransformation == null || cipherTransformation.isEmpty()) {
            throw new InvalidConfigurationException("End-to-end encryption cipher transformation is missing");
        }
        final String e2eEncryptionKey = configuration.getE2eEncryptionKey();
        if (e2eEncryptionKey == null || e2eEncryptionKey.isEmpty()) {
            throw new InvalidConfigurationException("End-to-end encryption key is missing");
        }
        try {
            // Convert secret key from Base64 String to SecretKey
            final byte[] secretKeyBytes = Base64.getDecoder().decode(e2eEncryptionKey);
            final SecretKey secretKey = keyConvertor.convertBytesToSharedSecretKey(secretKeyBytes);

            // Decrypt encrypted credential value
            final String[] parts = encryptedValue.split(":");
            if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
                throw new InvalidRequestException("Invalid format of encrypted credential value");
            }
            final byte[] iv = Base64.getDecoder().decode(parts[0]);
            final byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);
            final byte[] decryptedBytes = aes.decrypt(encryptedBytes, iv, secretKey, cipherTransformation);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (CryptoProviderException | InvalidKeyException | GenericCryptoException ex) {
            throw new EncryptionException(ex);
        }
    }

}