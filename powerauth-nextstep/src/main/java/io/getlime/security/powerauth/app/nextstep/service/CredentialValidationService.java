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
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationFailure;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationMode;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.UsernameGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import org.passay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This service handles validation of credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialValidationService {

    private final Logger logger = LoggerFactory.getLogger(CredentialValidationService.class);

    private final CredentialRepository credentialRepository;
    private final CredentialHistoryService credentialHistoryService;

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Credential validation service constructor.
     * @param credentialRepository Credential repository.
     * @param credentialHistoryService Credential history service.
     */
    public CredentialValidationService(CredentialRepository credentialRepository, CredentialHistoryService credentialHistoryService) {
        this.credentialRepository = credentialRepository;
        this.credentialHistoryService = credentialHistoryService;
    }

    /**
     * Validate credential.
     * @param user User identity entity.
     * @param credentialDefinition Credential definition entity.
     * @param username Username.
     * @param credentialValue Credential value.
     * @param validationMode Validation mode.
     * @return List of validation errors.
     * @throws InvalidRequestException Thrown in case request is invalid.
     * @throws InvalidConfigurationException Thrown when validation configuration is invalid.
     */
    public List<CredentialValidationFailure> validateCredential(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                                                 String username, String credentialValue,
                                                                 CredentialValidationMode validationMode) throws InvalidRequestException, InvalidConfigurationException {
        List<CredentialValidationFailure> validationErrors = new ArrayList<>();
        switch (validationMode) {
            case NO_VALIDATION:
                break;

            case VALIDATE_USERNAME:
                validationErrors.addAll(validateUsername(user, username, credentialDefinition));
                break;

            case VALIDATE_CREDENTIAL:
                validationErrors.addAll(validateCredentialValue(user, username, credentialValue, credentialDefinition, true));
                break;

            case VALIDATE_USERNAME_AND_CREDENTIAL:
                validationErrors.addAll(validateUsername(user, username, credentialDefinition));
                validationErrors.addAll(validateCredentialValue(user, username, credentialValue, credentialDefinition, true));
                break;

            default:
                throw new InvalidRequestException("Invalid validation mode: " + validationMode);

        }
        return validationErrors;
    }

    /**
     * Validate a username.
     * @param username Username.
     * @param credentialDefinition Credential definition.
     * @return List of validation errors.
     */
    public List<CredentialValidationFailure> validateUsername(UserIdentityEntity user, String username, CredentialDefinitionEntity credentialDefinition) {
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        List<CredentialValidationFailure> validationFailures = new ArrayList<>();
        if (credentialPolicy.getUsernameGenAlgorithm() == UsernameGenerationAlgorithm.NO_USERNAME) {
            return validationFailures;
        }
        if (username == null || username.trim().isEmpty()) {
            validationFailures.add(CredentialValidationFailure.USERNAME_EMPTY);
            return validationFailures;
        }
        Integer minLength = credentialPolicy.getUsernameLengthMin();
        Integer maxLength = credentialPolicy.getUsernameLengthMax();
        String allowedPattern = credentialPolicy.getUsernameAllowedPattern();
        if (minLength != null && username.length() < minLength) {
            validationFailures.add(CredentialValidationFailure.USERNAME_TOO_SHORT);
        }
        if (maxLength != null && username.length() > maxLength) {
            validationFailures.add(CredentialValidationFailure.USERNAME_TOO_LONG);
        }
        for (char c : username.toCharArray()) {
            if (Character.isWhitespace(c)) {
                validationFailures.add(CredentialValidationFailure.USERNAME_ILLEGAL_WHITESPACE);
                break;
            }
        }
        if (allowedPattern != null && !username.matches(allowedPattern)) {
            validationFailures.add(CredentialValidationFailure.USERNAME_ALLOWED_MATCH_FAILED);
        }
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
        if (credentialOptional.isPresent()) {
            CredentialEntity credential = credentialOptional.get();
            if (!credential.getUser().equals(user)) {
                validationFailures.add(CredentialValidationFailure.USERNAME_ALREADY_EXISTS);
            }
        }
        return validationFailures;
    }

    /**
     * Validate a credential value.
     * @param credentialValue Credential value.
     * @param credentialDefinition Credential definition.
     * @return List of validation failures.
     * @throws InvalidConfigurationException Thrown when validation configuration is invalid.
     */
    public List<CredentialValidationFailure> validateCredentialValue(UserIdentityEntity user, String username, String credentialValue, CredentialDefinitionEntity credentialDefinition, boolean checkHistory) throws InvalidConfigurationException {
        List<CredentialValidationFailure> validationFailures = new ArrayList<>();
        if (credentialValue == null || credentialValue.trim().isEmpty()) {
            validationFailures.add(CredentialValidationFailure.CREDENTIAL_EMPTY);
            return validationFailures;
        }
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        Integer minLength = credentialPolicy.getCredentialLengthMin();
        Integer maxLength = credentialPolicy.getCredentialLengthMax();
        if (minLength != null && credentialValue.length() < minLength) {
            validationFailures.add(CredentialValidationFailure.CREDENTIAL_TOO_SHORT);
        }
        if (maxLength != null && credentialValue.length() > maxLength) {
            validationFailures.add(CredentialValidationFailure.CREDENTIAL_TOO_LONG);
        }
        if (checkHistory && !credentialHistoryService.checkCredentialHistory(user, credentialValue, credentialDefinition)) {
            validationFailures.add(CredentialValidationFailure.CREDENTIAL_HISTORY_CHECK_FAILED);
        }
        try {
            CredentialValidationParam param = parameterConverter.fromString(credentialPolicy.getCredentialValParam(), CredentialValidationParam.class);
            validationFailures.addAll(validateCredentialValueAdvanced(username, credentialValue, param));
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        return validationFailures;
    }

    /**
     * Execute advanced credential validations based on defined validation rules.
     * @param username Username.
     * @param credentialValue Credential value.
     * @param param Credential validation parameters.
     * @return List of validation failures.
     * @throws InvalidConfigurationException Thrown when validation configuration is invalid.
     */
    private List<CredentialValidationFailure> validateCredentialValueAdvanced(String username, String credentialValue, CredentialValidationParam param) throws InvalidConfigurationException {
        List<CredentialValidationFailure> validationFailures = new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        try {
            if (param.isIncludeWhitespaceRule()) {
                rules.add(new WhitespaceRule());
            }
            if (param.isIncludeUsernameRule()) {
                rules.add(new UsernameRule(true, true));
            }
            if (param.isIncludeAllowedCharacterRule()) {
                String allowedChars = param.getAllowedChars();
                if (allowedChars == null) {
                    throw new InvalidConfigurationException("The allowedChars value is missing");
                }
                rules.add(new AllowedCharacterRule(allowedChars.toCharArray()));
            }
            if (param.isIncludeAllowedRegexRule()) {
                String allowedRegex = param.getAllowedRegex();
                if (allowedRegex == null) {
                    throw new InvalidConfigurationException("The allowedRegex value is missing");
                }
                rules.add(new AllowedRegexRule(allowedRegex));
            }
            if (param.isIncludeIllegalCharacterRule()) {
                String illegalChars = param.getIllegalChars();
                if (illegalChars == null) {
                    throw new InvalidConfigurationException("The illegalChars value is missing");
                }
                rules.add(new IllegalCharacterRule(illegalChars.toCharArray()));
            }
            if (param.isIncludeIllegalRegexRule()) {
                String illegalRegex = param.getIllegalRegex();
                if (illegalRegex == null) {
                    throw new InvalidConfigurationException("The illegalRegex value is missing");
                }
                rules.add(new IllegalRegexRule(illegalRegex));
            }
            if (param.isIncludeCharacterRule()) {
                boolean includeSmallLetters = param.isIncludeSmallLetters();
                Integer smallLettersMin = param.getSmallLettersMin();
                boolean includeCapitalLetters = param.isIncludeCapitalLetters();
                Integer capitalLettersMin = param.getCapitalLettersMin();
                boolean includeAlphabeticalLetters = param.isIncludeAlphabeticalLetters();
                Integer alphabeticalLettersMin = param.getAlphabeticalLettersMin();
                boolean includeDigits = param.isIncludeDigits();
                Integer digitsMin = param.getDigitsMin();
                boolean includeSpecialChars = param.isIncludeSpecialChars();
                Integer specialCharsMin = param.getSpecialCharsMin();
                if (includeSmallLetters) {
                    CharacterRule rule;
                    if (smallLettersMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.LowerCase);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.LowerCase, smallLettersMin);
                    }
                    rules.add(rule);
                }
                if (includeCapitalLetters) {
                    CharacterRule rule;
                    if (capitalLettersMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.UpperCase);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.UpperCase, capitalLettersMin);
                    }
                    rules.add(rule);
                }
                if (includeAlphabeticalLetters) {
                    CharacterRule rule;
                    if (alphabeticalLettersMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.Alphabetical);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.Alphabetical, alphabeticalLettersMin);
                    }
                    rules.add(rule);
                }
                if (includeDigits) {
                    CharacterRule rule;
                    if (digitsMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.Digit);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.Digit, digitsMin);
                    }
                    rules.add(rule);
                }
                if (includeSpecialChars) {
                    CharacterRule rule;
                    if (specialCharsMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.Special);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.Special, specialCharsMin);
                    }
                    rules.add(rule);
                }
            }
            PasswordData passwordData;
            if (username != null) {
                passwordData = new PasswordData(username, credentialValue);
            } else {
                passwordData = new PasswordData(credentialValue);
            }

            PasswordValidator passwordValidator = new PasswordValidator(rules);
            RuleResult result = passwordValidator.validate(passwordData);
            for (RuleResultDetail detail : result.getDetails()) {
                CredentialValidationFailure failure = convertToValidationFailure(detail.getErrorCode());
                if (!validationFailures.contains(failure)) {
                    validationFailures.add(failure);
                }
            }
        } catch (Exception ex) {
            throw new InvalidConfigurationException(ex);
        }
        return validationFailures;
    }

    /**
     * Convert validation error code to validation failure.
     * @param errorCode Validation error code.
     * @return Validation failure.
     */
    private CredentialValidationFailure convertToValidationFailure(String errorCode) throws InvalidConfigurationException {
        switch (errorCode) {
            case "ILLEGAL_WHITESPACE":
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_WHITESPACE;
            case "ILLEGAL_USERNAME":
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_USERNAME;
            case "ILLEGAL_USERNAME_REVERSED":
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_USERNAME_REVERSED;
            case "ALLOWED_CHAR":
                return CredentialValidationFailure.CREDENTIAL_ALLOWED_CHAR_FAILED;
            case "ALLOWED_MATCH":
                return CredentialValidationFailure.CREDENTIAL_ALLOWED_MATCH_FAILED;
            case "ILLEGAL_CHAR":
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_CHAR;
            case "ILLEGAL_MATCH":
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_MATCH;
            case "INSUFFICIENT_UPPERCASE":
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_UPPERCASE;
            case "INSUFFICIENT_LOWERCASE":
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_LOWERCASE;
            case "INSUFFICIENT_ALPHABETICAL":
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_ALPHABETICAL;
            case "INSUFFICIENT_DIGIT":
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_DIGIT;
            case "INSUFFICIENT_SPECIAL":
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_SPECIAL;

        }
        throw new InvalidConfigurationException("Unknown error code: " + errorCode);
    }

}