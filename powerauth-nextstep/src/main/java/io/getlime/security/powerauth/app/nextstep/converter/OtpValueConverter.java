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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpDefinitionEntity;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.exception.CryptoProviderException;
import io.getlime.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import io.getlime.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import io.getlime.security.powerauth.crypto.lib.util.KeyConvertor;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpValue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.EncryptionException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Converter for OTP value which handles key encryption and decryption in case it is configured.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class OtpValueConverter {

    private static final Logger logger = LoggerFactory.getLogger(OtpValueConverter.class);

    private final NextStepServerConfiguration configuration;

    private final KeyGenerator keyGenerator = new KeyGenerator();
    private final AESEncryptionUtils aes = new AESEncryptionUtils();
    private final KeyConvertor keyConvertor = new KeyConvertor();

    /**
     * Converter constructor.
     *
     * @param configuration Next Step server configuration.
     */
    @Autowired
    public OtpValueConverter(NextStepServerConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Convert credential from value stored in database which can be encrypted.
     *
     * @param otpValue OTP value.
     * @param otpId Otp ID.
     * @param otpDefinition OTP definition.
     * @return Converted OTP value.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public String fromDBValue(OtpValue otpValue, String otpId, OtpDefinitionEntity otpDefinition) throws InvalidConfigurationException, EncryptionException {
        switch (otpValue.getEncryptionAlgorithm()) {
            case NO_ENCRYPTION -> {
                return otpValue.getValue();
            }
            case AES_HMAC -> {
                final String masterDbEncryptionKeyBase64 = configuration.getMasterDbEncryptionKey();

                // In case master DB encryption key does not exist, do not encrypt the server private key
                if (masterDbEncryptionKeyBase64 == null || masterDbEncryptionKeyBase64.isEmpty()) {
                    throw new InvalidConfigurationException("Master DB encryption key is missing");
                }

                // Convert master DB encryption key
                final SecretKey masterDbEncryptionKey = keyConvertor.convertBytesToSharedSecretKey(Base64.getDecoder().decode(masterDbEncryptionKeyBase64));

                // Derive secret key from master DB encryption key, userId and activationId
                final SecretKey secretKey = deriveSecretKey(masterDbEncryptionKey, otpId, otpDefinition.getName());

                // Base64-decode credential value
                final byte[] credentialValueBytes = Base64.getDecoder().decode(otpValue.getValue());

                // IV is present in first 16 bytes
                final byte[] iv = Arrays.copyOfRange(credentialValueBytes, 0, 16);

                // Encrypted credential value is present after IV
                final byte[] encryptedCredentialValue = Arrays.copyOfRange(credentialValueBytes, 16, credentialValueBytes.length);

                // Decrypt credential value
                try {
                    final byte[] decryptedCredentialValue = aes.decrypt(encryptedCredentialValue, iv, secretKey);
                    return new String(decryptedCredentialValue, StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    throw new EncryptionException(ex);
                }
            }
            default -> throw new InvalidConfigurationException("Unsupported encryption algorithm: " + otpValue.getEncryptionAlgorithm());
        }

    }

    /**
     * Convert OTP value to database value with optional record encryption.
     *
     * @param otpValue OTP value.
     * @param otpId OTP ID.
     * @param otpDefinition OTP definition.
     * @return Converted credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption fails.
     */
    public OtpValue toDBValue(String otpValue, String otpId, OtpDefinitionEntity otpDefinition) throws InvalidConfigurationException, EncryptionException {
        if (!otpDefinition.isEncryptionEnabled()) {
            return new OtpValue(EncryptionAlgorithm.NO_ENCRYPTION, otpValue);
        }

        switch (otpDefinition.getEncryptionAlgorithm()) {
            case NO_ENCRYPTION -> {
                return new OtpValue(EncryptionAlgorithm.NO_ENCRYPTION, otpValue);
            }
            case AES_HMAC -> {
                final String masterDbEncryptionKeyBase64 = configuration.getMasterDbEncryptionKey();

                // In case master DB encryption key does not exist, do not encrypt the server private key
                if (masterDbEncryptionKeyBase64 == null || masterDbEncryptionKeyBase64.isEmpty()) {
                    throw new InvalidConfigurationException("Master DB encryption key is missing");
                }

                // Convert master DB encryption key
                final SecretKey masterDbEncryptionKey = keyConvertor.convertBytesToSharedSecretKey(Base64.getDecoder().decode(masterDbEncryptionKeyBase64));

                // Derive secret key from master DB encryption key, userId and activationId
                final SecretKey secretKey = deriveSecretKey(masterDbEncryptionKey, otpId, otpDefinition.getName());
                try {
                    // Generate random IV
                    final byte[] iv = keyGenerator.generateRandomBytes(16);

                    // Encrypt serverPrivateKey using secretKey with generated IV
                    final byte[] encrypted = aes.encrypt(otpValue.getBytes(StandardCharsets.UTF_8), iv, secretKey);

                    // Generate output bytes as encrypted + IV
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    baos.write(iv);
                    baos.write(encrypted);
                    final byte[] record = baos.toByteArray();

                    // Base64-encode output
                    final String encoded = Base64.getEncoder().encodeToString(record);

                    // Return encrypted record including encryption algorithm
                    return new OtpValue(EncryptionAlgorithm.AES_HMAC, encoded);
                } catch (Exception ex) {
                    throw new EncryptionException(ex);
                }
            }
            default -> throw new InvalidConfigurationException("Unsupported encryption algorithm: " + otpDefinition.getEncryptionAlgorithm());
        }

    }

    /**
     * Derive secret key from master DB encryption key and OTP ID.
     *
     * @param masterDbEncryptionKey Master DB encryption key.
     * @param otpId OTP ID.
     * @param otpName OTP name.
     * @return Derived secret key.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private SecretKey deriveSecretKey(SecretKey masterDbEncryptionKey, String otpId, String otpName) throws InvalidConfigurationException {
        try {
            // Use OTP ID as index for KDF_INTERNAL
            final byte[] index = (otpId + "&" + otpName).getBytes(StandardCharsets.UTF_8);

            // Derive secretKey from master DB encryption key using KDF_INTERNAL with constructed index
            return keyGenerator.deriveSecretKeyHmac(masterDbEncryptionKey, index);
        } catch (GenericCryptoException | CryptoProviderException ex) {
            throw new InvalidConfigurationException(ex);
        }
    }

}