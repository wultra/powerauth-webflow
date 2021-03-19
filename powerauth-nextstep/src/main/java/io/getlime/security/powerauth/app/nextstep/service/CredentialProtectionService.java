/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.converter.CredentialValueConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.Argon2Hash;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.EncryptionException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service which secures credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialProtectionService {

    private final Logger logger = LoggerFactory.getLogger(CredentialProtectionService.class);

    private final CredentialValueConverter credentialValueConverter;

    private final ParameterConverter parameterConverter = new ParameterConverter();
    private final KeyGenerator keyGenerator = new KeyGenerator();

    /**
     * Credential protection service constructor.
     * @param credentialValueConverter Credential value converter.
     */
    @Autowired
    public CredentialProtectionService(CredentialValueConverter credentialValueConverter) {
        this.credentialValueConverter = credentialValueConverter;
    }

    /**
     * Protect the credential value before persistence.
     * @param credentialValue Credential value.
     * @param userId User ID.
     * @param credentialDefinition Credential definition.
     * @return Protected credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption fails.
     */
    public CredentialValue protectCredential(String credentialValue, String userId, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException, EncryptionException {
        HashConfigEntity hashingConfig = credentialDefinition.getHashingConfig();
        if (hashingConfig == null) {
            return new CredentialValue(EncryptionAlgorithm.NO_ENCRYPTION, credentialValue);
        }
        HashAlgorithm algorithm = hashingConfig.getAlgorithm();
        Map<String, String> param;
        try {
            param = parameterConverter.fromString(hashingConfig.getParameters());
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        switch (algorithm) {
            case ARGON_2i:
                String hashedValue = hashCredentialUsingArgon2(credentialValue, "argon2i", param);
                return credentialValueConverter.toDBValue(hashedValue, userId, credentialDefinition);

            default:
                throw new InvalidConfigurationException("Unsupported hashing algorithm: " + algorithm);
        }
    }

    /**
     * Verify a credential value.
     * @param credentialValue Credential value sent by user.
     * @param expectedCredentialValue Expected credential value, in protected form in case credential protection is configured, unprotected otherwise.
     * @param credentialDefinition Credential definition.
     * @return Whether credential value matches expected credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public boolean verifyCredential(String credentialValue, CredentialValue expectedCredentialValue, String userId, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException, EncryptionException {
        HashConfigEntity hashingConfig = credentialDefinition.getHashingConfig();
        String decryptedCredentialValue = extractCredentialValue(expectedCredentialValue, userId, credentialDefinition);
        if (hashingConfig == null) {
            return credentialValue.equals(decryptedCredentialValue);
        }
        HashAlgorithm algorithm = hashingConfig.getAlgorithm();
        switch (algorithm) {
            case ARGON_2i:
                return verifyCredentialUsingArgon2(credentialValue, decryptedCredentialValue);

            default:
                throw new InvalidConfigurationException("Unsupported hashing algorithm: " + algorithm);
        }
    }

    /**
     * Extract a credential value.
     * @param credentialValue Optionally encrypted credential value.
     * @param userId User ID.
     * @param credentialDefinition Credential definition.
     * @return Extracted credential value.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public String extractCredentialValue(CredentialValue credentialValue, String userId, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException, EncryptionException {
        return credentialValueConverter.fromDBValue(credentialValue, userId, credentialDefinition);
    }

    /**
     * Verify a credential value protected using Argon2 algorithm.
     * @param credentialValue Unprotected credential value to verify.
     * @param expectedCredentialValue Expected hashed credential value.
     * @return Whether credential value matches expected credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private boolean verifyCredentialUsingArgon2(String credentialValue, String expectedCredentialValue) throws InvalidConfigurationException {
        Argon2Hash argon2;
        try {
            argon2 = Argon2Hash.parse(expectedCredentialValue);
        } catch (IOException ex) {
            throw new InvalidConfigurationException(ex);
        }
        // Earlier version Argon strings do not include version, default to version 16 (10 hex)
        int version = 16;
        if (argon2.getVersion() != null) {
            version = argon2.getVersion();
        }
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_i)
                .withVersion(version)
                .withIterations(argon2.getIterations())
                .withMemoryAsKB(argon2.getMemory())
                .withParallelism(argon2.getParallelism())
                .withSalt(argon2.getSalt());
        Argon2Parameters parameters = builder.build();
        int outputLength = argon2.getDigest().length;
        // Compute password hash using provided parameters
        Argon2Hash expectedHash = createArgon2Hash(credentialValue.getBytes(StandardCharsets.UTF_8), "argon2i", parameters, outputLength);
        // Compare hash values
        return argon2.hashEquals(expectedHash);
    }

    /**
     * Hash a credential value using Argon2 algorithm.
     * @param credentialValue Credential value.
     * @param algorithm Algorithm name.
     * @param param Algorithm parameters.
     * @return Argon2 hash in Modular Crypt Format.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private String hashCredentialUsingArgon2(String credentialValue, String algorithm, Map<String, String> param) throws InvalidConfigurationException {
        String versionParam = param.get("version");
        String iterationsParam = param.get("iterations");
        String memoryParam = param.get("memory");
        String parallelismParam = param.get("parallelism");
        String outputLengthParam = param.get("outputLength");
        if (versionParam == null) {
            throw new InvalidConfigurationException("Missing hashing parameter: version");
        }
        if (iterationsParam == null) {
            throw new InvalidConfigurationException("Missing hashing parameter: iterations");
        }
        if (memoryParam == null) {
            throw new InvalidConfigurationException("Missing hashing parameter: memory");
        }
        if (parallelismParam == null) {
            throw new InvalidConfigurationException("Missing hashing parameter: parallelism");
        }
        if (outputLengthParam == null) {
            throw new InvalidConfigurationException("Missing hashing parameter: outputLengthParam");
        }
        Argon2Parameters argon2Parameters;
        try {
            int version = Integer.parseInt(versionParam);
            int iterations = Integer.parseInt(iterationsParam);
            int memory = Integer.parseInt(memoryParam);
            int parallelism = Integer.parseInt(parallelismParam);
            int outputLength = Integer.parseInt(outputLengthParam);
            // Generate random salt
            byte[] salt = keyGenerator.generateRandomBytes(16);
            argon2Parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_i)
                    .withVersion(version)
                    .withIterations(iterations)
                    .withMemoryPowOfTwo(memory)
                    .withParallelism(parallelism)
                    .withSalt(salt)
                    .build();
            // Generate credential digest
            Argon2Hash hash = createArgon2Hash(credentialValue.getBytes(StandardCharsets.UTF_8), "argon2i", argon2Parameters, outputLength);
            return hash.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InvalidConfigurationException(ex);
        }
    }

    /**
     * Create an Argon2 hash
     * @param credentialBytes Credential bytes.
     * @param algorithm Algorithm name.
     * @param parameters Algorithm parameters.
     * @param outputLength Expected output length.
     * @return Argon2 hash in Modular Crypt Format.
     */
    private static Argon2Hash createArgon2Hash(byte[] credentialBytes, String algorithm, Argon2Parameters parameters, int outputLength) {
        // Generate password digest
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(parameters);
        byte[] digest = new byte[outputLength];
        gen.generateBytes(credentialBytes, digest);

        // Convert algorithm parameters and digest to Argon2 Modular Crypt Format
        Argon2Hash result = new Argon2Hash(algorithm);
        result.setVersion(parameters.getVersion());
        result.setIterations(parameters.getIterations());
        result.setParallelism(parameters.getLanes());
        result.setMemory(parameters.getMemory());
        result.setSalt(parameters.getSalt());
        result.setDigest(digest);
        return result;
    }

}