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

package io.getlime.security.powerauth.lib.webflow.authentication.method.form.encryption;

import com.google.common.io.BaseEncoding;
import io.getlime.security.powerauth.crypto.lib.config.PowerAuthConfiguration;
import io.getlime.security.powerauth.provider.CryptoProviderUtilBouncyCastle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Test class for the AES password encryptor.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
class AesEncryptionPasswordProtectionTest {

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
        PowerAuthConfiguration.INSTANCE.setKeyConvertor(new CryptoProviderUtilBouncyCastle());
    }

    @org.junit.jupiter.api.Test
    void protect() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {

        final String secretKeyBase64 = "Qee4CK44d8GduTxoHU7JPM2lCs+KF63akIpKyaLk9+c=";
        final String cipherTransformation = "AES/CBC/PKCS7Padding";
        final String originalPassword = "passw0rd";

        System.out.println("Encrypting Password: '" + originalPassword
                + "' with key '" + secretKeyBase64
                + "' using '" + cipherTransformation + "' mode."
        );

        String decryptedPassword;

        // Decrypt previously known password
        String encryptedPassword = "uOaHgDWvvrHPzFSvRY6bUg==:y8SuSlUipZKkcw2QjCpuCw==";
        decryptedPassword = decryptPassword(secretKeyBase64, cipherTransformation, encryptedPassword);
        System.out.println("Encrypted Password - static: " + encryptedPassword);
        System.out.println("Decrypted Password - static: " + decryptedPassword);
        Assertions.assertEquals(decryptedPassword, originalPassword);

        // Perform repeated encryption and decryption of passwords
        AesEncryptionPasswordProtection inst = new AesEncryptionPasswordProtection(cipherTransformation, secretKeyBase64);
        for (int i = 0; i < 10; i++){
            encryptedPassword = inst.protect(originalPassword);
            System.out.println("Encrypted Password " + i + ": " + encryptedPassword);
            decryptedPassword = decryptPassword(secretKeyBase64, cipherTransformation, encryptedPassword);
            System.out.println("Decrypted Password " + i + ": " + decryptedPassword);
            Assertions.assertEquals(decryptedPassword, originalPassword);
        }

    }

    private String decryptPassword(String secretKeyBase64, String cipherTransformation, String encryptedPassword) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        // Read secret key from configuration
        byte[] secretKeyBytes = BaseEncoding.base64().decode(secretKeyBase64);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");

        // Extract IV and encrypted password and convert them to bytes
        String[] parts = encryptedPassword.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid request");
        }
        String ivBase64 = parts[0];
        byte[] ivBytes = BaseEncoding.base64().decode(ivBase64);
        String encryptedPasswordBase64 = parts[1];
        byte[] encryptedPasswordBytes = BaseEncoding.base64().decode(encryptedPasswordBase64);

        // Decrypt password using specified cipher transformation, extracted IV and encrypted password bytes
        Cipher cipher = Cipher.getInstance(cipherTransformation, "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
        byte[] decryptedPasswordBytes = cipher.doFinal(encryptedPasswordBytes);
        return new String(decryptedPasswordBytes, StandardCharsets.UTF_8);
    }
}