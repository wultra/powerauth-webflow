/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationFailure;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationMode;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.UsernameGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.EncryptionException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import org.passay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
    private final ServiceCatalogue serviceCatalogue;

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Credential validation service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     */
    public CredentialValidationService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue) {
        this.credentialRepository = repositoryCatalogue.getCredentialRepository();
        this.serviceCatalogue = serviceCatalogue;
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
     * @throws EncryptionException Thrown when decryption fails.
     */
    public List<CredentialValidationFailure> validateCredential(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                                                 String username, String credentialValue,
                                                                 CredentialValidationMode validationMode) throws InvalidRequestException, InvalidConfigurationException, EncryptionException {
        final List<CredentialValidationFailure> validationErrors = new ArrayList<>();
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
        final CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        final List<CredentialValidationFailure> validationFailures = new ArrayList<>();
        if (credentialPolicy.getUsernameGenAlgorithm() == UsernameGenerationAlgorithm.NO_USERNAME) {
            return validationFailures;
        }
        if (username == null || username.trim().isEmpty()) {
            validationFailures.add(CredentialValidationFailure.USERNAME_EMPTY);
            return validationFailures;
        }
        final Integer minLength = credentialPolicy.getUsernameLengthMin();
        final Integer maxLength = credentialPolicy.getUsernameLengthMax();
        final String allowedPattern = credentialPolicy.getUsernameAllowedPattern();
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
        final Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
        if (credentialOptional.isPresent()) {
            final CredentialEntity credential = credentialOptional.get();
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
     * @throws EncryptionException Thrown when decryption fails.
     */
    public List<CredentialValidationFailure> validateCredentialValue(UserIdentityEntity user, String username, String credentialValue, CredentialDefinitionEntity credentialDefinition, boolean checkHistory) throws InvalidConfigurationException, EncryptionException {
        final CredentialHistoryService credentialHistoryService = serviceCatalogue.getCredentialHistoryService();
        final List<CredentialValidationFailure> validationFailures = new ArrayList<>();
        if (credentialValue == null || credentialValue.trim().isEmpty()) {
            validationFailures.add(CredentialValidationFailure.CREDENTIAL_EMPTY);
            return validationFailures;
        }
        final CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        final Integer minLength = credentialPolicy.getCredentialLengthMin();
        final Integer maxLength = credentialPolicy.getCredentialLengthMax();
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
            final CredentialValidationParam param = parameterConverter.fromString(credentialPolicy.getCredentialValParam(), CredentialValidationParam.class);
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
        final List<CredentialValidationFailure> validationFailures = new ArrayList<>();
        final List<Rule> rules = new ArrayList<>();
        try {
            if (param.isIncludeWhitespaceRule()) {
                rules.add(new WhitespaceRule());
            }
            if (param.isIncludeUsernameRule()) {
                rules.add(new UsernameRule(true, true));
            }
            if (param.isIncludeAllowedCharacterRule()) {
                final String allowedChars = param.getAllowedChars();
                if (allowedChars == null) {
                    throw new InvalidConfigurationException("The allowedChars value is missing");
                }
                rules.add(new AllowedCharacterRule(allowedChars.toCharArray()));
            }
            if (param.isIncludeAllowedRegexRule()) {
                final String allowedRegex = param.getAllowedRegex();
                if (allowedRegex == null) {
                    throw new InvalidConfigurationException("The allowedRegex value is missing");
                }
                rules.add(new AllowedRegexRule(allowedRegex));
            }
            if (param.isIncludeIllegalCharacterRule()) {
                final String illegalChars = param.getIllegalChars();
                if (illegalChars == null) {
                    throw new InvalidConfigurationException("The illegalChars value is missing");
                }
                rules.add(new IllegalCharacterRule(illegalChars.toCharArray()));
            }
            if (param.isIncludeIllegalRegexRule()) {
                final String illegalRegex = param.getIllegalRegex();
                if (illegalRegex == null) {
                    throw new InvalidConfigurationException("The illegalRegex value is missing");
                }
                rules.add(new IllegalRegexRule(illegalRegex));
            }
            if (param.isIncludeCharacterRule()) {
                final boolean includeSmallLetters = param.isIncludeSmallLetters();
                final Integer smallLettersMin = param.getSmallLettersMin();
                final boolean includeCapitalLetters = param.isIncludeCapitalLetters();
                final Integer capitalLettersMin = param.getCapitalLettersMin();
                final boolean includeAlphabeticalLetters = param.isIncludeAlphabeticalLetters();
                final Integer alphabeticalLettersMin = param.getAlphabeticalLettersMin();
                final boolean includeDigits = param.isIncludeDigits();
                final Integer digitsMin = param.getDigitsMin();
                final boolean includeSpecialChars = param.isIncludeSpecialChars();
                final Integer specialCharsMin = param.getSpecialCharsMin();
                if (includeSmallLetters) {
                    final CharacterRule rule;
                    if (smallLettersMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.LowerCase);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.LowerCase, smallLettersMin);
                    }
                    rules.add(rule);
                }
                if (includeCapitalLetters) {
                    final CharacterRule rule;
                    if (capitalLettersMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.UpperCase);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.UpperCase, capitalLettersMin);
                    }
                    rules.add(rule);
                }
                if (includeAlphabeticalLetters) {
                    final CharacterRule rule;
                    if (alphabeticalLettersMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.Alphabetical);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.Alphabetical, alphabeticalLettersMin);
                    }
                    rules.add(rule);
                }
                if (includeDigits) {
                    final CharacterRule rule;
                    if (digitsMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.Digit);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.Digit, digitsMin);
                    }
                    rules.add(rule);
                }
                if (includeSpecialChars) {
                    final CharacterRule rule;
                    if (specialCharsMin == null) {
                        rule = new CharacterRule(EnglishCharacterData.Special);
                    } else {
                        rule = new CharacterRule(EnglishCharacterData.Special, specialCharsMin);
                    }
                    rules.add(rule);
                }
            }
            final PasswordData passwordData;
            if (username != null) {
                passwordData = new PasswordData(username, credentialValue);
            } else {
                passwordData = new PasswordData(credentialValue);
            }

            final PasswordValidator passwordValidator = new PasswordValidator(rules);
            final RuleResult result = passwordValidator.validate(passwordData);
            for (RuleResultDetail detail : result.getDetails()) {
                final CredentialValidationFailure failure = convertToValidationFailure(detail.getErrorCode());
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
            case "ILLEGAL_WHITESPACE" -> {
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_WHITESPACE;
            }
            case "ILLEGAL_USERNAME" -> {
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_USERNAME;
            }
            case "ILLEGAL_USERNAME_REVERSED" -> {
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_USERNAME_REVERSED;
            }
            case "ALLOWED_CHAR" -> {
                return CredentialValidationFailure.CREDENTIAL_ALLOWED_CHAR_FAILED;
            }
            case "ALLOWED_MATCH" -> {
                return CredentialValidationFailure.CREDENTIAL_ALLOWED_MATCH_FAILED;
            }
            case "ILLEGAL_CHAR" -> {
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_CHAR;
            }
            case "ILLEGAL_MATCH" -> {
                return CredentialValidationFailure.CREDENTIAL_ILLEGAL_MATCH;
            }
            case "INSUFFICIENT_UPPERCASE" -> {
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_UPPERCASE;
            }
            case "INSUFFICIENT_LOWERCASE" -> {
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_LOWERCASE;
            }
            case "INSUFFICIENT_ALPHABETICAL" -> {
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_ALPHABETICAL;
            }
            case "INSUFFICIENT_DIGIT" -> {
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_DIGIT;
            }
            case "INSUFFICIENT_SPECIAL" -> {
                return CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_SPECIAL;
            }
        }
        throw new InvalidConfigurationException("Unknown error code: " + errorCode);
    }

}