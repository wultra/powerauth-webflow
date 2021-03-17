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
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.Argon2Hash;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service which secures credentials.
 */
@Service
public class CredentialProtectionService {

    private final Logger logger = LoggerFactory.getLogger(CredentialProtectionService.class);

    private final ParameterConverter parameterConverter = new ParameterConverter();
    private final KeyGenerator keyGenerator = new KeyGenerator();

    /**
     * Protect the credential value before persistence.
     * @param credentialValue Credential value.
     * @param credentialDefinition Credential definition.
     * @return Protected credential value.
     * @throws InvalidConfigurationException Thrown in case Next Step configuration is invalid.
     */
    public String protectCredential(String credentialValue, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        HashConfigEntity hashingConfig = credentialDefinition.getHashingConfig();
        if (hashingConfig == null) {
            return credentialValue;
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
                return hashCredentialUsingArgon2(credentialValue, "argon2i", param);

            default:
                throw new InvalidConfigurationException("Unsupported hashing algorithm: " + algorithm);
        }
    }

    /**
     * Verify a credential value.
     * @param credentialValue Credential value, in protected form in case credential protection is configured, unprotected otherwise.
     * @param expectedCredentialValue Expected credential value in unprotected form.
     * @param credentialDefinition Credential definition.
     * @return Whether credential value matches expected credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public boolean verifyCredential(String credentialValue, String expectedCredentialValue, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        HashConfigEntity hashingConfig = credentialDefinition.getHashingConfig();
        if (hashingConfig == null) {
            return credentialValue.equals(expectedCredentialValue);
        }
        HashAlgorithm algorithm = hashingConfig.getAlgorithm();
        switch (algorithm) {
            case ARGON_2i:
                return verifyCredentialUsingArgon2(credentialValue, expectedCredentialValue);

            default:
                throw new InvalidConfigurationException("Unsupported hashing algorithm: " + algorithm);
        }

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
     * @return Argon2 hash in Modular Crypto Format.
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
     * @return Argon2 hash in Modular Crypto Format.
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