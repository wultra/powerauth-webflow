/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.encryption;

import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import io.getlime.security.powerauth.crypto.lib.util.KeyConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-based protection for passwords during transfer.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AesEncryptionPasswordProtection implements PasswordProtection {

    private static final Logger logger = LoggerFactory.getLogger(AesEncryptionPasswordProtection.class);

    private final AESEncryptionUtils aes = new AESEncryptionUtils();
    private KeyGenerator keyGenerator = new KeyGenerator();
    private KeyConvertor keyConvertor = new KeyConvertor();

    private String cipherTransformation;
    private String secretKeyBase64;

    /**
     * Class constructor.
     * @param cipherTransformation Cipher transformation to use for AES.
     * @param secretKeyBase64 Base64 encoded secret key for AES.
     */
    public AesEncryptionPasswordProtection(String cipherTransformation, String secretKeyBase64) {
        this.cipherTransformation = cipherTransformation;
        this.secretKeyBase64 = secretKeyBase64;
    }

    @Override
    public String protect(String password) {

        // Password is encrypted using AES with random IV and with configured mode and padding
        try {
            if (cipherTransformation == null || cipherTransformation.isEmpty()) {
                logger.error("Password encryption failed because application property powerauth.webflow.password.encryption.transformation is not configured");
                return null;
            }
            if (!cipherTransformation.startsWith("AES/")) {
                logger.error("Password encryption failed because cipher transformation is configured to use different algorithm than AES");

            }
            if (secretKeyBase64 == null || secretKeyBase64.isEmpty()) {
                logger.error("Password encryption failed because application property powerauth.webflow.password.encryption.key is not configured");
                return null;
            }

            // Convert secret key from Base64 String to SecretKey
            byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyBase64);
            SecretKey secretKey = keyConvertor.convertBytesToSharedSecretKey(secretKeyBytes);

            // Generate random IV, the AES requires 16 bytes no matter the key size
            byte[] ivBytes = keyGenerator.generateRandomBytes(16);

            // Encrypt password bytes using random IV, secret key and transformation
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedPasswordBytes = aes.encrypt(passwordBytes, ivBytes, secretKey, cipherTransformation);
            String encryptedPasswordBase64 = Base64.getEncoder().encodeToString(encryptedPasswordBytes);

            // Base64 encode the IV
            String ivBase64 = Base64.getEncoder().encodeToString(ivBytes);

            // Return encrypted String in format [ivBase64]:[encryptedDataBase64], without square brackets
            return ivBase64 + ":" + encryptedPasswordBase64;
        } catch (Throwable t) {
            // Cryptography errors are caught and logged, error messages are not sent back to the user to avoid leaking information about encryption
            logger.error("Password encryption failed", t);
            return null;
        }

    }
}
