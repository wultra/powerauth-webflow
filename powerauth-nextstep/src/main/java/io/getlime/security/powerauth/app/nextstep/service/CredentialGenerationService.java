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
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UsernameGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This service handles generation of credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialGenerationService {

    private final Logger logger = LoggerFactory.getLogger(CredentialGenerationService.class);

    private final CredentialRepository credentialRepository;
    private final NextStepServerConfiguration nextStepServerConfiguration;

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Credential generation service constructor.
     * @param credentialRepository Credential repository.
     * @param nextStepServerConfiguration Next Step server configuration.
     */
    public CredentialGenerationService(CredentialRepository credentialRepository, NextStepServerConfiguration nextStepServerConfiguration) {
        this.credentialRepository = credentialRepository;
        this.nextStepServerConfiguration = nextStepServerConfiguration;
    }

    /**
     * Generate a username as defined in credential policy.
     * @param credentialDefinition Credential definition.
     * @return Generated username.
     * @throws InvalidConfigurationException Thrown in case username could not be generated.
     */
    public String generateUsername(CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        switch (credentialPolicy.getUsernameGenAlgorithm()) {
            case "DEFAULT":
            case "RANDOM_DIGITS":
                try {
                    return generateRandomUsernameWithDigits(credentialDefinition);
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            case "RANDOM_LETTERS":
                try {
                    return generateRandomUsernameWithLetters(credentialDefinition);
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            default:
                throw new InvalidConfigurationException("Unsupported username generation algorithm: " + credentialPolicy.getUsernameGenAlgorithm());
        }
    }

    /**
     * Generate a credential value as defined in credential policy.
     * @param credentialDefinition Credential definition.
     * @return Generated credential value.
     * @throws InvalidConfigurationException Thrown in case credential value could not be generated.
     */
    public String generateCredentialValue(CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        switch (credentialPolicy.getCredentialGenAlgorithm()) {
            case "DEFAULT":
            case "RANDOM_PASSWORD":
                try {
                    return generateRandomPassword(credentialPolicy);
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            case "RANDOM_PIN":
                try {
                    return generateRandomPin(credentialPolicy);
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            default:
                throw new InvalidConfigurationException("Unsupported credential value generation algorithm: " + credentialPolicy.getCredentialGenAlgorithm());
        }
    }

    /**
     * Generate random username with digits.
     * @param credentialDefinition Credential definition.
     * @return Generated username.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private String generateRandomUsernameWithDigits(CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        UsernameGenerationParam param;
        try {
            param = parameterConverter.fromString(credentialPolicy.getUsernameGenParam(), UsernameGenerationParam.class);
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        int length = param.getLength();
        SecureRandom secureRandom = new SecureRandom();
        int generateUsernameMaxAttempts = nextStepServerConfiguration.getGenerateUsernameMaxAttempts();
        for (int i = 0; i < generateUsernameMaxAttempts; i++) {
            BigInteger bound = BigInteger.valueOf(Math.round(Math.pow(10, length)));
            BigInteger randomNumber = new BigInteger(bound.bitLength(), secureRandom).mod(bound);
            String username = randomNumber.toString();
            if (username.length() < length) {
                // This can happen with leading zeros
                continue;
            }
            Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
            if (credentialOptional.isPresent()) {
                // Username is already taken
                continue;
            }
            return username;
        }
        throw new InvalidConfigurationException("Username could not be generated, all attempts failed");
    }

    /**
     * Generate random username with letters.
     * @param credentialDefinition Credential definition.
     * @return Generated username.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private String generateRandomUsernameWithLetters(CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        UsernameGenerationParam param;
        try {
            param = parameterConverter.fromString(credentialPolicy.getUsernameGenParam(), UsernameGenerationParam.class);
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        int length = param.getLength();
        SecureRandom secureRandom = new SecureRandom();
        int generateUsernameMaxAttempts = nextStepServerConfiguration.getGenerateUsernameMaxAttempts();
        for (int i = 0; i < generateUsernameMaxAttempts; i++) {
            StringBuilder usernameBuilder = new StringBuilder();
            for (int j = 0; j < length; j++) {
                char c = (char) (secureRandom.nextInt(26) + 'a');
                usernameBuilder.append(c);
            }
            String username = usernameBuilder.toString();
            Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
            if (credentialOptional.isPresent()) {
                // Username is already taken
                continue;
            }
            return username;
        }
        throw new InvalidConfigurationException("Username could not be generated, all attempts failed");
    }

    /**
     * Generate random password.
     * @param credentialPolicy Credential policy.
     * @return Generated password.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private String generateRandomPassword(CredentialPolicyEntity credentialPolicy) throws InvalidConfigurationException {
        CredentialGenerationParam param;
        try {
            param = parameterConverter.fromString(credentialPolicy.getCredentialGenParam(), CredentialGenerationParam.class);
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        int length = param.getLength();
        int countFromRules = 0;
        boolean includeSmallLetters = param.isIncludeSmallLetters();
        Integer smallLettersCount = param.getSmallLettersCount();
        if (smallLettersCount != null) {
            countFromRules += smallLettersCount;
        }
        boolean includeCapitalLetters = param.isIncludeCapitalLetters();
        Integer capitalLettersCount = param.getCapitalLettersCount();
        if (capitalLettersCount != null) {
            countFromRules += capitalLettersCount;
        }
        boolean includeDigits = param.isIncludeDigits();
        Integer digitsCount = param.getDigitsCount();
        if (digitsCount != null) {
            countFromRules += digitsCount;
        }
        boolean includeSpecialChars = param.isIncludeSpecialChars();
        Integer specialCharsCount = param.getSpecialCharsCount();
        if (specialCharsCount != null) {
            countFromRules += specialCharsCount;
        }
        if (!includeSmallLetters && !includeCapitalLetters && !includeDigits && !includeSpecialChars) {
            throw new InvalidConfigurationException("Invalid configuration of algorithm RANDOM_PASSWORD: at least one character rule is required");
        }
        if (countFromRules > 0 && countFromRules != length) {
            throw new InvalidConfigurationException("Invalid configuration of algorithm RANDOM_PASSWORD: credential length does not match rules");
        }
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        List<CharacterRule> characterRules = new ArrayList<>();
        if (includeSmallLetters) {
            CharacterRule rule;
            if (smallLettersCount == null) {
                rule = new CharacterRule(EnglishCharacterData.LowerCase);
            } else {
                rule = new CharacterRule(EnglishCharacterData.LowerCase, smallLettersCount);
            }
            characterRules.add(rule);
        }
        if (includeCapitalLetters) {
            CharacterRule rule;
            if (capitalLettersCount == null) {
                rule = new CharacterRule(EnglishCharacterData.UpperCase);
            } else {
                rule = new CharacterRule(EnglishCharacterData.UpperCase, capitalLettersCount);
            }
            characterRules.add(rule);
        }
        if (includeDigits) {
            CharacterRule rule;
            if (digitsCount == null) {
                rule = new CharacterRule(EnglishCharacterData.Digit);
            } else {
                rule = new CharacterRule(EnglishCharacterData.Digit, digitsCount);
            }
            characterRules.add(rule);
        }
        if (includeSpecialChars) {
            CharacterRule rule;
            if (specialCharsCount == null) {
                rule = new CharacterRule(new SpecialCharacters());
            } else {
                rule = new CharacterRule(new SpecialCharacters(), specialCharsCount);
            }
            characterRules.add(rule);
        }
        return passwordGenerator.generatePassword(length, characterRules);
    }

    /**
     * Generate random PIN.
     * @param credentialPolicy Credential policy.
     * @return Random PIN.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private String generateRandomPin(CredentialPolicyEntity credentialPolicy) throws InvalidConfigurationException {
        CredentialGenerationParam param;
        try {
            param = parameterConverter.fromString(credentialPolicy.getCredentialGenParam(), CredentialGenerationParam.class);
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        int length = param.getLength();
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        return passwordGenerator.generatePassword(length, new CharacterRule(EnglishCharacterData.Digit));
    }

    /**
     * Custom character data definition for special characters.
     */
    private static class SpecialCharacters implements CharacterData {

        private static final String ERROR_CODE = "INSUFFICIENT_SPECIAL_CHARACTERS";
        private static final String CHARACTERS = "^<>{};:.,~!?@#$%=&*[]()";

        @Override
        public String getErrorCode() {
            return ERROR_CODE;
        }

        @Override
        public String getCharacters() {
            return CHARACTERS;
        }
    }

}