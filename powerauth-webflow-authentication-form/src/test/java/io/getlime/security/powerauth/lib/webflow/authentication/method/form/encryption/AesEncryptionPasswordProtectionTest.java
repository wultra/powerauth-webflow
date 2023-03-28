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

package io.getlime.security.powerauth.lib.webflow.authentication.method.form.encryption;

import io.getlime.security.powerauth.lib.webflow.authentication.encryption.AesEncryptionPasswordProtection;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Test class for the AES password encryptor.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
class AesEncryptionPasswordProtectionTest {

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
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
        byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyBase64);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");

        // Extract IV and encrypted password and convert them to bytes
        String[] parts = encryptedPassword.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid request");
        }
        String ivBase64 = parts[0];
        byte[] ivBytes = Base64.getDecoder().decode(ivBase64);
        String encryptedPasswordBase64 = parts[1];
        byte[] encryptedPasswordBytes = Base64.getDecoder().decode(encryptedPasswordBase64);

        // Decrypt password using specified cipher transformation, extracted IV and encrypted password bytes
        Cipher cipher = Cipher.getInstance(cipherTransformation, "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
        byte[] decryptedPasswordBytes = cipher.doFinal(encryptedPasswordBytes);
        return new String(decryptedPasswordBytes, StandardCharsets.UTF_8);
    }
}