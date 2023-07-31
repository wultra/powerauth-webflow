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
package io.getlime.security.powerauth.app.nextstep;

import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UsernameGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.CredentialGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.UsernameGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialValidationFailedException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Next Step credential tests.
 *
 * @author Roman Strobl, roman.strobl@wulta.com
 */
class NextStepCredentialTest extends NextStepTest {

    @BeforeEach
    void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);
    }

    @Test
    void testGenerateCredential() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        // Create credential policy
        CredentialGenerationParam credentialGenParam = new CredentialGenerationParam();
        credentialGenParam.setLength(12);
        credentialGenParam.setIncludeSmallLetters(true);
        credentialGenParam.setIncludeCapitalLetters(true);
        credentialGenParam.setIncludeDigits(true);
        credentialGenParam.setIncludeSpecialChars(true);
        credentialGenParam.setSmallLettersCount(5);
        credentialGenParam.setCapitalLettersCount(5);
        credentialGenParam.setDigitsCount(1);
        credentialGenParam.setSpecialCharsCount(1);
        updateCredentialDefinition(name, credentialGenParam, null);
        CreateCredentialResponse r1 = nextStepClient.createCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION", CredentialType.PERMANENT, null, null).getResponseObject();
        assertNotNull(r1.getUsername());
        assertEquals(8, r1.getUsername().length());
        String credentialValue = r1.getCredentialValue();
        assertEquals(12, credentialValue.length());
        int smallLettersCount = 0;
        int capitalLettersCount = 0;
        int digitsCount = 0;
        int specialCharsCount = 0;
        for (int i = 0; i < 12; i++) {
            char c = credentialValue.charAt(i);
            if (Character.isLowerCase(c)) {
                smallLettersCount++;
                continue;
            }
            if (Character.isUpperCase(c)) {
                capitalLettersCount++;
                continue;
            }
            if (Character.isDigit(c)) {
                digitsCount++;
                continue;
            }
            specialCharsCount++;
        }
        assertEquals(5, smallLettersCount);
        assertEquals(5, capitalLettersCount);
        assertEquals(1, digitsCount);
        assertEquals(1, specialCharsCount);
    }

    @Test
    void testValidateCredential1() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeCharacterRule(true);
        credentialValParam.setIncludeDigits(true);
        credentialValParam.setDigitsMin(10);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_DIGIT), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential2() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeCharacterRule(true);
        credentialValParam.setIncludeCapitalLetters(true);
        credentialValParam.setCapitalLettersMin(1);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_UPPERCASE), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential3() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeCharacterRule(true);
        credentialValParam.setIncludeSmallLetters(true);
        credentialValParam.setSmallLettersMin(1);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_LOWERCASE), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential4() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeCharacterRule(true);
        credentialValParam.setIncludeSpecialChars(true);
        credentialValParam.setSpecialCharsMin(1);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_SPECIAL), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential5() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeAllowedCharacterRule(true);
        credentialValParam.setAllowedChars("abcdef");
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_ALLOWED_CHAR_FAILED), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential6() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeIllegalCharacterRule(true);
        credentialValParam.setIllegalChars("1");
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_ILLEGAL_CHAR), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential7() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeAllowedRegexRule(true);
        credentialValParam.setAllowedRegex("[a-z]+");
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_ALLOWED_MATCH_FAILED), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential8() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeIllegalRegexRule(true);
        credentialValParam.setIllegalRegex("[0-9]+");
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_ILLEGAL_MATCH), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential9() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeWhitespaceRule(true);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "123456789 ", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_ILLEGAL_WHITESPACE), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential10() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeUsernameRule(true);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "1234567890", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_ILLEGAL_USERNAME), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential11() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeUsernameRule(true);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "0987654321", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_ILLEGAL_USERNAME_REVERSED), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential12() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        credentialValParam.setIncludeCharacterRule(true);
        credentialValParam.setIncludeAlphabeticalLetters(true);
        credentialValParam.setAlphabeticalLettersMin(1);
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "12345678", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_INSUFFICIENT_ALPHABETICAL), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential13() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "1", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_TOO_SHORT), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential14() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", "12345678901234567890123456789012345678901234567890", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_TOO_LONG), r1.getValidationErrors());
    }

    @Test
    void testValidateCredential15() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialValidationParam credentialValParam = new CredentialValidationParam();
        updateCredentialDefinition(name, null, credentialValParam);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1234567890", " ", CredentialValidationMode.VALIDATE_CREDENTIAL).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_EMPTY), r1.getValidationErrors());
    }

    @Test
    void testValidateUsername1() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                " ", null, CredentialValidationMode.VALIDATE_USERNAME).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.USERNAME_EMPTY), r1.getValidationErrors());
    }

    @Test
    void testValidateUsername2() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "1", null, CredentialValidationMode.VALIDATE_USERNAME).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.USERNAME_TOO_SHORT), r1.getValidationErrors());
    }

    @Test
    void testValidateUsername3() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "12345678901234567890123456789012345678901234567890", null, CredentialValidationMode.VALIDATE_USERNAME).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.USERNAME_TOO_LONG), r1.getValidationErrors());
    }

    @Test
    void testValidateUsername5() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "12345 67890", null, CredentialValidationMode.VALIDATE_USERNAME).getResponseObject();
        assertEquals(Arrays.asList(CredentialValidationFailure.USERNAME_ILLEGAL_WHITESPACE, CredentialValidationFailure.USERNAME_ALLOWED_MATCH_FAILED), r1.getValidationErrors());
    }

    @Test
    void testValidateUsername6() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(name);
        nextStepClient.createUser(createUserRequest);
        ValidateCredentialResponse r1 = nextStepClient.validateCredential(name, "TEST_CREDENTIAL",
                "testuser", null, CredentialValidationMode.VALIDATE_USERNAME).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.USERNAME_ALREADY_EXISTS), r1.getValidationErrors());
    }

    @Test
    void testValidateUsername7() throws NextStepClientException {
        ValidateCredentialResponse r1 = nextStepClient.validateCredential("test_user_1", "TEST_CREDENTIAL_GENERATION_VALIDATION",
                "testuser$", null, CredentialValidationMode.VALIDATE_USERNAME).getResponseObject();
        assertEquals(Collections.singletonList(CredentialValidationFailure.USERNAME_ALLOWED_MATCH_FAILED), r1.getValidationErrors());
    }

    @Test
    void testResetCredential() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        createUserRequest.getCredentials().add(credential);
        CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        String username = r1.getCredentials().get(0).getUsername();
        String credentialValue = r1.getCredentials().get(0).getCredentialValue();
        ResetCredentialResponse r2 = nextStepClient.resetCredential(userId, "TEST_CREDENTIAL", CredentialType.PERMANENT).getResponseObject();
        assertEquals(username, r2.getUsername());
        assertNotEquals(credentialValue, r2.getCredentialValue());
    }

    @Test
    void testChangeUsername() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        createUserRequest.getCredentials().add(credential);
        CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        String credentialValue = r1.getCredentials().get(0).getCredentialValue();
        UpdateCredentialResponse r2 = nextStepClient.updateCredential(userId, "TEST_CREDENTIAL", CredentialType.PERMANENT, "new_username", null, null).getResponseObject();
        assertEquals("new_username", r2.getUsername());
        // Try lookup with new username and authentication with old credential value, it should not change
        LookupUserResponse r3 = nextStepClient.lookupUser("new_username", "TEST_CREDENTIAL").getResponseObject();
        String userIdLookup = r3.getUser().getUserId();
        CredentialAuthenticationResponse r4 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", userIdLookup, credentialValue).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r4.getAuthenticationResult());
    }

    @Test
    void testChangeUsernameValidation() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
        try {
            nextStepClient.updateCredential(userId, "TEST_CREDENTIAL", CredentialType.PERMANENT, "new username", null, null);
        } catch (NextStepClientException ex) {
            assertEquals(CredentialValidationFailedException.CODE, ex.getError().getCode());
            assertEquals(Arrays.asList(CredentialValidationFailure.USERNAME_ILLEGAL_WHITESPACE, CredentialValidationFailure.USERNAME_ALLOWED_MATCH_FAILED), ((CredentialValidationError)ex.getError()).getValidationFailures());
            return;
        }
        Assertions.fail();
    }

    @Test
    void testChangeUsernameAndCredential() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
        UpdateCredentialResponse r2 = nextStepClient.updateCredential(userId, "TEST_CREDENTIAL", CredentialType.PERMANENT, "new_username2", "rg^24jG2sk", null).getResponseObject();
        assertEquals("new_username2", r2.getUsername());
        // Try lookup with new username and authentication with old credential value, it should not change
        LookupUserResponse r3 = nextStepClient.lookupUser("new_username2", "TEST_CREDENTIAL").getResponseObject();
        String userIdLookup = r3.getUser().getUserId();
        CredentialAuthenticationResponse r4 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", userIdLookup, "rg^24jG2sk").getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r4.getAuthenticationResult());
    }

    @Test
    void testChangeUsernameAndCredentialValidation() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
        try {
            nextStepClient.updateCredential(userId, "TEST_CREDENTIAL", CredentialType.PERMANENT, "x", " ", null);
        } catch (NextStepClientException ex) {
            assertEquals(CredentialValidationFailedException.CODE, ex.getError().getCode());
            assertEquals(Arrays.asList(CredentialValidationFailure.USERNAME_TOO_SHORT, CredentialValidationFailure.CREDENTIAL_EMPTY), ((CredentialValidationError)ex.getError()).getValidationFailures());
            return;
        }
        Assertions.fail();
    }

    @Test
    void testChangeCredential() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        createUserRequest.getCredentials().add(credential);
        CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        String username = r1.getCredentials().get(0).getUsername();
        UpdateCredentialResponse r2 = nextStepClient.updateCredential(userId, "TEST_CREDENTIAL", CredentialType.PERMANENT, null, "rg^24jG2sk", null).getResponseObject();
        // Try lookup with new username and authentication with old credential value, it should not change
        LookupUserResponse r3 = nextStepClient.lookupUser(username, "TEST_CREDENTIAL").getResponseObject();
        String userIdLookup = r3.getUser().getUserId();
        CredentialAuthenticationResponse r4 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", userIdLookup, "rg^24jG2sk").getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r4.getAuthenticationResult());
    }

    @Test
    void testChangeCredentialValidation() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
        try {
            nextStepClient.updateCredential(userId, "TEST_CREDENTIAL", CredentialType.PERMANENT, null, " ", null);
        } catch (NextStepClientException ex) {
            assertEquals(CredentialValidationFailedException.CODE, ex.getError().getCode());
            assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_EMPTY), ((CredentialValidationError)ex.getError()).getValidationFailures());
            return;
        }
        Assertions.fail();
    }

    @Test
    void testChangeUsernameAlreadyExists() throws NextStepClientException {
        String userId1 = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest1 = new CreateUserRequest();
        createUserRequest1.setUserId(userId1);
        CreateUserRequest.NewCredential credential1 = new CreateUserRequest.NewCredential();
        credential1.setCredentialName("TEST_CREDENTIAL");
        credential1.setCredentialType(CredentialType.PERMANENT);
        createUserRequest1.getCredentials().add(credential1);
        nextStepClient.createUser(createUserRequest1);
        UpdateCredentialResponse r2 = nextStepClient.updateCredential(userId1, "TEST_CREDENTIAL", CredentialType.PERMANENT, "new_username_conflict", null, null).getResponseObject();
        assertEquals("new_username_conflict", r2.getUsername());
        String userId2 = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest2 = new CreateUserRequest();
        createUserRequest2.setUserId(userId2);
        CreateUserRequest.NewCredential credential2 = new CreateUserRequest.NewCredential();
        credential2.setCredentialName("TEST_CREDENTIAL");
        credential2.setCredentialType(CredentialType.PERMANENT);
        createUserRequest2.getCredentials().add(credential2);
        nextStepClient.createUser(createUserRequest2);
        try {
            nextStepClient.updateCredential(userId2, "TEST_CREDENTIAL", CredentialType.PERMANENT, "new_username_conflict", "rg^24jG2sk", null);
        } catch (NextStepClientException ex) {
            assertEquals(CredentialValidationFailedException.CODE, ex.getError().getCode());
            assertEquals(Collections.singletonList(CredentialValidationFailure.USERNAME_ALREADY_EXISTS), ((CredentialValidationError) ex.getError()).getValidationFailures());
            return;
        }
        Assertions.fail();
    }

    @Test
    void testDeleteCredential() throws NextStepClientException {
        final String userId = UUID.randomUUID().toString();

        final CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);

        final CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        createUserRequest.getCredentials().add(credential);

        final CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        final String credentialValue = r1.getCredentials().get(0).getCredentialValue();

        final DeleteCredentialResponse r2 = nextStepClient.deleteCredential(userId, "TEST_CREDENTIAL").getResponseObject();
        assertEquals(CredentialStatus.REMOVED, r2.getCredentialStatus());

        final GetUserCredentialListResponse r3 = nextStepClient.getUserCredentialList(userId, true).getResponseObject();
        assertEquals(1, r3.getCredentials().size());
        assertEquals(CredentialStatus.REMOVED, r3.getCredentials().get(0).getCredentialStatus());
        assertNull(r3.getCredentials().get(0).getUsername());

        final CredentialAuthenticationResponse r4 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", userId, credentialValue).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r4.getAuthenticationResult());
    }

    @Test
    void testGenerateUsernameRandomLetters() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CredentialGenerationParam credentialGenParam = new CredentialGenerationParam();
        credentialGenParam.setLength(10);
        credentialGenParam.setIncludeSmallLetters(true);
        credentialGenParam.setSmallLettersCount(10);
        updateCredentialDefinition(name, credentialGenParam, new CredentialValidationParam());
        UpdateCredentialPolicyRequest updateRequest = new UpdateCredentialPolicyRequest();
        updateRequest.setUsernameLengthMin(8);
        updateRequest.setUsernameLengthMax(30);
        updateRequest.setUsernameAllowedPattern("[a-z]{10}");
        updateRequest.setUsernameGenAlgorithm(UsernameGenerationAlgorithm.RANDOM_LETTERS);
        updateRequest.setCredentialPolicyName(name);
        UsernameGenerationParam usernameGenParam = new UsernameGenerationParam();
        usernameGenParam.setLength(10);
        updateRequest.setUsernameGenParam(usernameGenParam);
        nextStepClient.updateCredentialPolicy(updateRequest);
        String userId = UUID.randomUUID().toString();
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUserId(userId);
        nextStepClient.createUser(userRequest);
        CreateCredentialResponse r1 = nextStepClient.createCredential(userId, "TEST_CREDENTIAL_GENERATION_VALIDATION", CredentialType.PERMANENT,  null, null).getResponseObject();
        assertTrue(r1.getUsername().matches("[a-z]{10}"));
    }

    @Test
    void testGenerateRandomPin() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        CreateCredentialPolicyRequest credentialPolicyRequest = new CreateCredentialPolicyRequest();
        credentialPolicyRequest.setUsernameGenAlgorithm(UsernameGenerationAlgorithm.NO_USERNAME);
        credentialPolicyRequest.setCredentialPolicyName(name);
        credentialPolicyRequest.setLimitSoft(3);
        credentialPolicyRequest.setLimitHard(5);
        credentialPolicyRequest.setCredentialLengthMin(4);
        credentialPolicyRequest.setCredentialLengthMax(8);
        credentialPolicyRequest.setCredentialGenAlgorithm(CredentialGenerationAlgorithm.RANDOM_PIN);
        CredentialGenerationParam genParam = new CredentialGenerationParam();
        genParam.setLength(6);
        credentialPolicyRequest.setCredentialGenParam(genParam);
        CredentialValidationParam valParam = new CredentialValidationParam();
        valParam.setIncludeCharacterRule(true);
        valParam.setIncludeDigits(true);
        valParam.setDigitsMin(4);
        valParam.setIncludeAllowedRegexRule(true);
        valParam.setAllowedRegex("[0-9]{4,8}");
        credentialPolicyRequest.setCredentialValParam(valParam);
        nextStepClient.createCredentialPolicy(credentialPolicyRequest);
        UpdateCredentialDefinitionRequest credentialDefinitionRequest = new UpdateCredentialDefinitionRequest();
        credentialDefinitionRequest.setCredentialDefinitionName("TEST_CREDENTIAL_GENERATION_VALIDATION");
        credentialDefinitionRequest.setApplicationName("TEST_APP");
        credentialDefinitionRequest.setCredentialPolicyName(name);
        credentialDefinitionRequest.setCategory(CredentialCategory.PIN);
        nextStepClient.updateCredentialDefinition(credentialDefinitionRequest);
        String userId = UUID.randomUUID().toString();
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUserId(userId);
        nextStepClient.createUser(userRequest);
        CreateCredentialResponse r1 = nextStepClient.createCredential(userId, "TEST_CREDENTIAL_GENERATION_VALIDATION", CredentialType.PERMANENT,  null, null).getResponseObject();
        assertTrue(r1.getCredentialValue().matches("[0-9]{4,8}"));
    }

    @Test
    void testCredentialHistory1() throws NextStepClientException {
        String userId1 = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId1);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        CreateUserRequest.CredentialHistory credentialHistory = new CreateUserRequest.CredentialHistory();
        credentialHistory.setUsername("test_history_1");
        credentialHistory.setCredentialValue("je&qd8P1");
        credential.getCredentialHistory().add(credentialHistory);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
        try {
            nextStepClient.updateCredential(userId1, "TEST_CREDENTIAL", CredentialType.PERMANENT, null, "je&qd8P1", null);
        } catch (NextStepClientException ex) {
            assertEquals(CredentialValidationFailedException.CODE, ex.getError().getCode());
            assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_HISTORY_CHECK_FAILED), ((CredentialValidationError) ex.getError()).getValidationFailures());
            return;
        }
        Assertions.fail();
    }

    @Test
    void testCredentialHistory2() throws NextStepClientException {
        String userId1 = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId1);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        CreateUserRequest.CredentialHistory credentialHistory = new CreateUserRequest.CredentialHistory();
        credentialHistory.setUsername("test_history_1");
        credentialHistory.setCredentialValue("je&qd8P1");
        credential.getCredentialHistory().add(credentialHistory);
        credentialHistory = new CreateUserRequest.CredentialHistory();
        credentialHistory.setUsername("test_history_1");
        credentialHistory.setCredentialValue("Xl34Hn1s@");
        credential.getCredentialHistory().add(credentialHistory);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
        try {
            nextStepClient.updateCredential(userId1, "TEST_CREDENTIAL", CredentialType.PERMANENT, null, "je&qd8P1", null);
        } catch (NextStepClientException ex) {
            assertEquals(CredentialValidationFailedException.CODE, ex.getError().getCode());
            assertEquals(Collections.singletonList(CredentialValidationFailure.CREDENTIAL_HISTORY_CHECK_FAILED), ((CredentialValidationError) ex.getError()).getValidationFailures());
            return;
        }
        Assertions.fail();
    }

    @Test
    void testCredentialHistory3() throws NextStepClientException {
        String userId1 = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId1);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        CreateUserRequest.CredentialHistory credentialHistory = new CreateUserRequest.CredentialHistory();
        credentialHistory.setUsername("test_history_1");
        credentialHistory.setCredentialValue("je&qd8P1");
        credential.getCredentialHistory().add(credentialHistory);
        credentialHistory = new CreateUserRequest.CredentialHistory();
        credentialHistory.setUsername("test_history_1");
        credentialHistory.setCredentialValue("Xl34Hn1s@");
        credential.getCredentialHistory().add(credentialHistory);
        credentialHistory = new CreateUserRequest.CredentialHistory();
        credentialHistory.setUsername("test_history_1");
        credentialHistory.setCredentialValue("Oqw*2kd6M");
        credential.getCredentialHistory().add(credentialHistory);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
        nextStepClient.updateCredential(userId1, "TEST_CREDENTIAL", CredentialType.PERMANENT, null, "je&qd8P1", null);
    }

    private void updateCredentialDefinition(String name, CredentialGenerationParam credentialGenParam, CredentialValidationParam credentialValParam) throws NextStepClientException {
        // Create credential policy
        createCredentialPolicy(name, credentialGenParam, credentialValParam);
        UpdateCredentialDefinitionRequest credentialDefinitionRequest = new UpdateCredentialDefinitionRequest();
        credentialDefinitionRequest.setCredentialDefinitionName("TEST_CREDENTIAL_GENERATION_VALIDATION");
        credentialDefinitionRequest.setApplicationName("TEST_APP");
        credentialDefinitionRequest.setCredentialPolicyName(name);
        credentialDefinitionRequest.setCategory(CredentialCategory.PASSWORD);
        nextStepClient.updateCredentialDefinition(credentialDefinitionRequest);
    }

    private void createCredentialPolicy(String name, CredentialGenerationParam genParam, CredentialValidationParam valParam) throws NextStepClientException {
        CreateCredentialPolicyRequest credentialPolicyRequest = new CreateCredentialPolicyRequest();
        credentialPolicyRequest.setCredentialPolicyName(name);
        credentialPolicyRequest.setLimitSoft(3);
        credentialPolicyRequest.setLimitHard(5);
        credentialPolicyRequest.setUsernameLengthMin(8);
        credentialPolicyRequest.setUsernameLengthMax(30);
        credentialPolicyRequest.setUsernameAllowedPattern("[0-9]+");
        credentialPolicyRequest.setUsernameGenAlgorithm(UsernameGenerationAlgorithm.RANDOM_DIGITS);
        UsernameGenerationParam usernameGenParam = new UsernameGenerationParam();
        usernameGenParam.setLength(8);
        credentialPolicyRequest.setUsernameGenParam(usernameGenParam);
        credentialPolicyRequest.setCredentialLengthMin(6);
        credentialPolicyRequest.setCredentialLengthMax(30);
        credentialPolicyRequest.setCredentialGenAlgorithm(CredentialGenerationAlgorithm.RANDOM_PASSWORD);
        if (genParam != null) {
            credentialPolicyRequest.setCredentialGenParam(genParam);
        }
        if (valParam != null) {
            credentialPolicyRequest.setCredentialValParam(valParam);
        }
        nextStepClient.createCredentialPolicy(credentialPolicyRequest);
    }

}