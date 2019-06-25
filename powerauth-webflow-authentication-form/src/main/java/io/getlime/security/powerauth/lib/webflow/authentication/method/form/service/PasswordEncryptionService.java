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
package io.getlime.security.powerauth.lib.webflow.authentication.method.form.service;

import com.google.common.io.BaseEncoding;
import io.getlime.security.powerauth.crypto.lib.config.PowerAuthConfiguration;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.provider.CryptoProviderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Service for encryption of user passwords.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class PasswordEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryptionService.class);

    private final WebFlowServicesConfiguration configuration;

    private final AESEncryptionUtils aes = new AESEncryptionUtils();
    private final KeyGenerator keyGenerator = new KeyGenerator();

    /**
     * Password encryption service constructor.
     * @param configuration Web Flow configuration.
     */
    public PasswordEncryptionService(WebFlowServicesConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Encrypt password using configured algorithm and parameters.
     * @param password Plain text password.
     * @return Password after encryption in case it is configured or null in case password encryption fails.
     */
    public String encryptPassword(String password) {
        CryptoProviderUtil keyConvertor = PowerAuthConfiguration.INSTANCE.getKeyConvertor();
        AuthenticationType authenticationType = configuration.getAuthenticationType();
        switch (authenticationType) {

            case BASIC:
                // Password is sent in plain text
                return password;

            case PASSWORD_ENCRYPTION_AES:
                // Password is encrypted using AES with random IV and with configured mode and padding
                try {
                    String cipherTransformation = configuration.getCipherTransformation();
                    if (cipherTransformation == null || cipherTransformation.isEmpty()) {
                        logger.error("Password encryption failed because application property powerauth.webflow.password.encryption.transformation is not configured");
                        return null;
                    }
                    if (!cipherTransformation.startsWith("AES/")) {
                        logger.error("Password encryption failed because cipher transformation is configured to use different algorithm than AES");

                    }
                    String secretKeyBase64 = configuration.getPasswordEncryptionKey();
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

            default:
                logger.error("Unsupported authentication type: {}", authenticationType);
                return null;
        }
    }

}
