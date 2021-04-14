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
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
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

    private static final int SALT_SIZE = 16;

    private final CredentialRepository credentialRepository;
    private final CredentialValueConverter credentialValueConverter;

    private final ParameterConverter parameterConverter = new ParameterConverter();
    private final KeyGenerator keyGenerator = new KeyGenerator();

    /**
     * Credential protection service constructor.
     * @param credentialRepository Credential repository.
     * @param credentialValueConverter Credential value converter.
     */
    @Autowired
    public CredentialProtectionService(CredentialRepository credentialRepository, CredentialValueConverter credentialValueConverter) {
        this.credentialRepository = credentialRepository;
        this.credentialValueConverter = credentialValueConverter;
    }

    /**
     * Protect the credential value before persistence.
     * @param credentialValue Credential value.
     * @param credential Credential entity.
     * @return Protected credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption fails.
     */
    public CredentialValue protectCredential(String credentialValue, CredentialEntity credential) throws InvalidConfigurationException, EncryptionException {
        CredentialDefinitionEntity credentialDefinition = credential.getCredentialDefinition();
        String userId = credential.getUser().getUserId();
        HashConfigEntity hashingConfig = credentialDefinition.getHashingConfig();
        if (hashingConfig == null) {
            return credentialValueConverter.toDBValue(credentialValue, userId, credentialDefinition);
        }
        HashAlgorithm algorithm = hashingConfig.getAlgorithm();
        Map<String, String> param;
        try {
            param = parameterConverter.fromString(hashingConfig.getParameters());
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        switch (algorithm) {
            case ARGON_2D:
            case ARGON_2I:
            case ARGON_2ID:
                Argon2Hash argon2Hash = hashCredentialUsingArgon2(credentialValue, algorithm, param);
                String hashedValue = argon2Hash.toString();
                return credentialValueConverter.toDBValue(hashedValue, userId, credentialDefinition);

            default:
                throw new InvalidConfigurationException("Unsupported hashing algorithm: " + algorithm);
        }
    }

    /**
     * Verify a credential value.
     * @param credentialValue Credential value sent by user.
     * @param credential Credential entity.
     * @return Whether credential value matches expected credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public boolean verifyCredential(String credentialValue, CredentialEntity credential) throws InvalidConfigurationException, EncryptionException {
        CredentialDefinitionEntity credentialDefinition = credential.getCredentialDefinition();
        HashConfigEntity hashingConfig = credentialDefinition.getHashingConfig();
        String decryptedCredentialValue = extractCredentialValue(credential);
        if (hashingConfig == null) {
            boolean succeeded = credentialValue.equals(decryptedCredentialValue);
            if (succeeded) {
                updateStoredCredentialValueIfRequired(credentialValue, credential);
            }
            return succeeded;
        }
        HashAlgorithm algorithm = hashingConfig.getAlgorithm();
        switch (algorithm) {
            case ARGON_2I:
            case ARGON_2D:
            case ARGON_2ID:
                boolean succeeded = verifyCredentialUsingArgon2(credentialValue, algorithm, decryptedCredentialValue);
                if (succeeded) {
                    updateStoredCredentialValueIfRequired(credentialValue, credential);
                }
                return succeeded;

            default:
                throw new InvalidConfigurationException("Unsupported hashing algorithm: " + algorithm);
        }
    }

    /**
     * Extract a credential value.
     * @param credential Credential entity.
     * @return Extracted credential value.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public String extractCredentialValue(CredentialEntity credential) throws InvalidConfigurationException, EncryptionException {
        CredentialDefinitionEntity credentialDefinition = credential.getCredentialDefinition();
        String userId = credential.getUser().getUserId();
        CredentialValue credentialValueStored = new CredentialValue(credential.getEncryptionAlgorithm(), credential.getValue());
        return credentialValueConverter.fromDBValue(credentialValueStored, userId, credentialDefinition);
    }

    /**
     * Verify a credential value protected using Argon2 algorithm.
     * @param credentialValue Unprotected credential value to verify.
     * @param algorithm Hashing algorithm.
     * @param expectedCredentialValue Expected hashed credential value.
     * @return Whether credential value matches expected credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private boolean verifyCredentialUsingArgon2(String credentialValue, HashAlgorithm algorithm, String expectedCredentialValue) throws InvalidConfigurationException {
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
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(algorithm.getId())
                .withVersion(version)
                .withIterations(argon2.getIterations())
                .withMemoryAsKB(argon2.getMemory())
                .withParallelism(argon2.getParallelism())
                .withSalt(argon2.getSalt());
        Argon2Parameters parameters = builder.build();
        int outputLength = argon2.getDigest().length;
        // Compute password hash using provided parameters
        Argon2Hash expectedHash = createArgon2Hash(credentialValue.getBytes(StandardCharsets.UTF_8), algorithm, parameters, outputLength);
        // Compare hash values
        return argon2.hashEquals(expectedHash);
    }

    /**
     * Hash a credential value using Argon2 algorithm.
     * @param credentialValue Credential value.
     * @param param Algorithm parameters.
     * @return Argon2 hash in Modular Crypt Format.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private Argon2Hash hashCredentialUsingArgon2(String credentialValue, HashAlgorithm algorithm, Map<String, String> param) throws InvalidConfigurationException {
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
            byte[] salt = keyGenerator.generateRandomBytes(SALT_SIZE);
            argon2Parameters = new Argon2Parameters.Builder(algorithm.getId())
                    .withVersion(version)
                    .withIterations(iterations)
                    .withMemoryPowOfTwo(memory)
                    .withParallelism(parallelism)
                    .withSalt(salt)
                    .build();
            // Generate Argon2 hash from the credential value
            return createArgon2Hash(credentialValue.getBytes(StandardCharsets.UTF_8), algorithm, argon2Parameters, outputLength);
        } catch (Exception ex) {
            throw new InvalidConfigurationException(ex);
        }
    }

    /**
     * Create an Argon2 hash.
     * @param credentialBytes Credential bytes.
     * @param algorithm Hashing algorithm.
     * @param parameters Algorithm parameters.
     * @param outputLength Expected output length.
     * @return Argon2 hash in Modular Crypt Format.
     */
    private static Argon2Hash createArgon2Hash(byte[] credentialBytes, HashAlgorithm algorithm, Argon2Parameters parameters, int outputLength) {
        // Generate password digest
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(parameters);
        byte[] digest = new byte[outputLength];
        gen.generateBytes(credentialBytes, digest);

        // Convert algorithm parameters and digest to Argon2 Modular Crypt Format
        Argon2Hash result = new Argon2Hash(algorithm.getName());
        result.setVersion(parameters.getVersion());
        result.setIterations(parameters.getIterations());
        result.setParallelism(parameters.getLanes());
        result.setMemory(parameters.getMemory());
        result.setSalt(parameters.getSalt());
        result.setDigest(digest);
        return result;
    }

    /**
     * Update credential value in case encryption or hashing algorithm was changed.
     * @param credentialValue Credential value.
     * @param credential Credential entity.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption fails.
     */
    private void updateStoredCredentialValueIfRequired(String credentialValue, CredentialEntity credential) throws InvalidConfigurationException, EncryptionException {
        boolean updateRequired = false;
        CredentialDefinitionEntity credentialDefinition = credential.getCredentialDefinition();
        if (credential.getEncryptionAlgorithm() == null && credentialDefinition.isEncryptionEnabled()) {
            // Encryption is expected but credential is not encrypted
            updateRequired = true;
        }
        if (credential.getEncryptionAlgorithm() != null && !credentialDefinition.isEncryptionEnabled()) {
            // Encryption is not expected but credential is encrypted
            updateRequired = true;
        }
        if (credentialDefinition.isEncryptionEnabled() && credential.getEncryptionAlgorithm() != credentialDefinition.getEncryptionAlgorithm()) {
            // Encryption algorithm differs
            updateRequired = true;
        }
        if (credential.getHashingConfig() == null && credentialDefinition.getHashingConfig() != null) {
            // Hashing is expected but credential is not hashed
            updateRequired = true;
        }
        if (credential.getHashingConfig() != null && credentialDefinition.getHashingConfig() == null) {
            // Hashing is not expected but credential is hashed
            updateRequired = true;
        }
        if (credential.getHashingConfig() != null && credentialDefinition.getHashingConfig() != null) {
            if (credential.getHashingConfig() != credentialDefinition.getHashingConfig()) {
                // Hashing configuration was changed to another record, hashing algorithm upgrade is expected
                updateRequired = true;
            } else if (!credential.getHashingConfig().getAlgorithm().equals(credentialDefinition.getHashingConfig().getAlgorithm())) {
                // Hashing algorithm name has been changed
                updateRequired = true;
            } else {
                // Check actual argon2 parameters from the hash in the database and compare them with credential definition
                updateRequired = updateRequired || !argon2ParamMatch(extractCredentialValue(credential), credentialDefinition.getHashingConfig().getAlgorithm(), credential.getHashingConfig().getParameters());
            }
        }

        if (updateRequired) {
            // Protect the credential value and save it into DB
            CredentialValue updatedValue = protectCredential(credentialValue, credential);
            credential.setHashingConfig(credentialDefinition.getHashingConfig());
            if (updatedValue.getEncryptionAlgorithm() != EncryptionAlgorithm.NO_ENCRYPTION) {
                credential.setEncryptionAlgorithm(credentialDefinition.getEncryptionAlgorithm());
            } else {
                credential.setEncryptionAlgorithm(null);
            }
            credential.setValue(updatedValue.getValue());
            credentialRepository.save(credential);
        }
    }

    /**
     * Check whether Argon2 parameters match the hashed credential value and parameters from credential definition.
     * @param argon2Hash Argon2 hash in Modular Crypt Format.
     * @param algorithm Hashing algorithm.
     * @param expectedParam Expected parameters from credential definition.
     * @return Whether actual Argon2 parameters match expected values.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private boolean argon2ParamMatch(String argon2Hash, HashAlgorithm algorithm, String expectedParam) throws InvalidConfigurationException {
        try {
            Argon2Hash hash = Argon2Hash.parse(argon2Hash);
            Map<String, String> param = parameterConverter.fromString(expectedParam);
            if (!hash.getAlgorithm().equals(algorithm.getName())) {
                return false;
            }
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
            int version = Integer.parseInt(versionParam);
            int iterations = Integer.parseInt(iterationsParam);
            // Memory is stored in kB in the hash
            int memory = (int) Math.pow(2, Integer.parseInt(memoryParam));
            int parallelism = Integer.parseInt(parallelismParam);
            int outputLength = Integer.parseInt(outputLengthParam);
            // The version property is missing in Argon version 0x10 = 16 decimal.
            // Thus, version null -> version 19 is effectively an upgrade from version 0x10 (16 decimal) to 0x13 (19 decimal).
            if ((version > 16 && hash.getVersion() == null) ||
                    (hash.getVersion() != null && hash.getVersion() != version)) {
                return false;
            }
            if (hash.getIterations() == null || hash.getIterations() != iterations) {
                return false;
            }
            if (hash.getMemory() == null || hash.getMemory() != memory) {
                return false;
            }
            if (hash.getParallelism() == null || hash.getParallelism() != parallelism) {
                return false;
            }
            // Digest length is checked directly on the digest
            if (hash.getDigest() == null || hash.getDigest().length != outputLength) {
                return false;
            }
            return true;
        } catch (IOException ex) {
            logger.warn("Argon2 parameter comparison failed, reason: " + ex.getMessage());
            return false;
        }
    }

}