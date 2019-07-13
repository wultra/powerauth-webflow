/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.encryption;

import com.google.common.io.BaseEncoding;
import io.getlime.security.powerauth.crypto.lib.config.PowerAuthConfiguration;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import io.getlime.security.powerauth.provider.CryptoProviderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * AES-based protection for passwords during transfer.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AesEncryptionPasswordProtection implements PasswordProtection {

    private static final Logger logger = LoggerFactory.getLogger(AesEncryptionPasswordProtection.class);

    private final AESEncryptionUtils aes = new AESEncryptionUtils();
    private KeyGenerator keyGenerator = new KeyGenerator();

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
        CryptoProviderUtil keyConvertor = PowerAuthConfiguration.INSTANCE.getKeyConvertor();
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
            byte[] secretKeyBytes = BaseEncoding.base64().decode(secretKeyBase64);
            SecretKey secretKey = keyConvertor.convertBytesToSharedSecretKey(secretKeyBytes);

            // Generate random IV, the AES requires 16 bytes no matter the key size
            byte[] ivBytes = keyGenerator.generateRandomBytes(16);

            // Encrypt password bytes using random IV, secret key and transformation
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedPasswordBytes = aes.encrypt(passwordBytes, ivBytes, secretKey, cipherTransformation);
            String encryptedPasswordBase64 = BaseEncoding.base64().encode(encryptedPasswordBytes);

            // Base64 encode the IV
            String ivBase64 = BaseEncoding.base64().encode(ivBytes);

            // Return encrypted String in format [ivBase64]:[encryptedDataBase64], without square brackets
            return ivBase64 + ":" + encryptedPasswordBase64;
        } catch (Throwable t) {
            // Cryptography errors are caught and logged, error messages are not sent back to the user to avoid leaking information about encryption
            logger.error("Password encryption failed", t);
            return null;
        }

    }
}
