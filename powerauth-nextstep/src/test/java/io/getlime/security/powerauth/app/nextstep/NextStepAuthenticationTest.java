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

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.exception.CryptoProviderException;
import io.getlime.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import io.getlime.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import io.getlime.security.powerauth.crypto.lib.util.KeyConvertor;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.CounterResetMode;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.LookupUsersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.ResetCountersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Next Step authentication tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepAuthenticationTest extends NextStepTest {

    private final KeyGenerator keyGenerator = new KeyGenerator();
    private final KeyConvertor keyConvertor = new KeyConvertor();
    private final AESEncryptionUtils aes = new AESEncryptionUtils();

    @Autowired
    private NextStepServerConfiguration nextStepServerConfiguration;

    @BeforeEach
    public void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);

        // Reset counters before each test
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.SUCCEEDED);
    }

    @Test
    public void testOtpSuccessNoOperationNoCredential() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        assertEquals(8, r1.getOtpValue().length());
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), r1.getOtpValue()).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getAuthenticationResult());
        assertEquals(OtpStatus.USED, r2.getOtpStatus());
        assertEquals("test_user_1", r2.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUserIdentityStatus());
        assertEquals(0, (int) r2.getRemainingAttempts());
    }

    @Test
    public void testOtpFailNoOperationNoCredential() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertEquals("test_user_1", r2.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUserIdentityStatus());
        assertEquals(2, (int) r2.getRemainingAttempts());
    }

    @Test
    public void testOtpBlockNoOperationNoCredential() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000");
        nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000");
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(OtpStatus.BLOCKED, r2.getOtpStatus());
        assertEquals("test_user_1", r2.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUserIdentityStatus());
        assertEquals(0, (int) r2.getRemainingAttempts());
    }

    @Test
    public void testOtpBlockNoOperationBlockCredential() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        OtpAuthenticationResponse r1a = nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000").getResponseObject();
        assertEquals(2, (int) r1a.getRemainingAttempts());
        OtpAuthenticationResponse r1b = nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000").getResponseObject();
        assertEquals(1, (int) r1b.getRemainingAttempts());
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(0, (int) r2.getRemainingAttempts());
        assertEquals(OtpStatus.BLOCKED, r2.getOtpStatus());
        // Soft limit reached
        GetUserCredentialListResponse ru1 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.BLOCKED_TEMPORARY, ru1.getCredentials().get(0).getCredentialStatus());
        // Reset soft counters to be able to verify hard block later
        ResetCountersRequest resetCountersRequest = new ResetCountersRequest();
        resetCountersRequest.setResetMode(CounterResetMode.RESET_BLOCKED_TEMPORARY);
        nextStepClient.resetAllCounters(resetCountersRequest);
        CreateOtpResponse r3 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r3.getOtpStatus());
        GetUserCredentialListResponse ru2 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.ACTIVE, ru2.getCredentials().get(0).getCredentialStatus());
        // Continue with 2 more OTP attempts
        OtpAuthenticationResponse r3a = nextStepClient.authenticateWithOtp(r3.getOtpId(), "00000000000").getResponseObject();
        assertEquals(1, (int) r3a.getRemainingAttempts());
        OtpAuthenticationResponse r4 = nextStepClient.authenticateWithOtp(r1.getOtpId(), "00000000000").getResponseObject();
        assertEquals(0, (int) r4.getRemainingAttempts());
        GetUserCredentialListResponse ru3 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        // Hard limit reached
        assertEquals(CredentialStatus.BLOCKED_PERMANENT, ru3.getCredentials().get(0).getCredentialStatus());
        // Unblock credential
        nextStepClient.unblockCredential("test_user_1", "TEST_CREDENTIAL");
        GetUserCredentialListResponse ru4 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.ACTIVE, ru4.getCredentials().get(0).getCredentialStatus());
    }

    @Test
    public void testOtpSuccessWithOperationNoCredential() throws NextStepClientException {
        CreateOperationResponse r1 = nextStepClient.createOperation("auth_otp", "test_operation_1", "A1", null, null).getResponseObject();
        assertEquals("test_operation_1", r1.getOperationId());
        assertEquals("A1", r1.getOperationData());
        assertEquals(AuthResult.CONTINUE, r1.getResult());
        CreateOtpResponse r2 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, "test_operation_1").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertNotNull(r2.getOtpId());
        assertNotNull(r2.getOtpValue());
        OtpAuthenticationResponse r3 = nextStepClient.authenticateWithOtp(null, "test_operation_1", r2.getOtpValue(), true, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r3.getAuthenticationResult());
        assertEquals(OtpStatus.USED, r3.getOtpStatus());
        assertEquals("test_user_1", r3.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r3.getUserIdentityStatus());
        GetOperationDetailResponse r4 = nextStepClient.getOperationDetail("test_operation_1").getResponseObject();
        assertEquals(AuthResult.DONE, r4.getResult());
    }

    @Test
    public void testOtpSuccessNoUser() throws NextStepClientException {
        CreateOperationResponse r1 = nextStepClient.createOperation("auth_otp", "test_operation_2", "A1", null, null).getResponseObject();
        assertEquals(AuthResult.CONTINUE, r1.getResult());
        CreateOtpResponse r2 = nextStepClient.createOtp(null, "TEST_OTP", null, null, "test_operation_2").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertNotNull(r2.getOtpId());
        assertNotNull(r2.getOtpValue());
        OtpAuthenticationResponse r3 = nextStepClient.authenticateWithOtp(null, "test_operation_2", r2.getOtpValue(), true, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r3.getAuthenticationResult());
        assertEquals(OtpStatus.USED, r3.getOtpStatus());
        assertNull(r3.getUserId());
        assertNull(r3.getUserIdentityStatus());
        GetOperationDetailResponse r4 = nextStepClient.getOperationDetail("test_operation_2").getResponseObject();
        assertEquals(AuthResult.DONE, r4.getResult());
    }

    @Test
    public void testOtpSuccessExternalUser() throws NextStepClientException {
        CreateOperationResponse r1 = nextStepClient.createOperation("auth_otp", "test_operation_3", "A1", null, null).getResponseObject();
        assertEquals(AuthResult.CONTINUE, r1.getResult());
        CreateOtpResponse r2 = nextStepClient.createOtp("external_user_id", "TEST_OTP", null, null, "test_operation_3").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertNotNull(r2.getOtpId());
        assertNotNull(r2.getOtpValue());
        OtpAuthenticationResponse r3 = nextStepClient.authenticateWithOtp(null, "test_operation_3", r2.getOtpValue(), true, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r3.getAuthenticationResult());
        assertEquals(OtpStatus.USED, r3.getOtpStatus());
        assertEquals("external_user_id", r3.getUserId());
        assertNull(r3.getUserIdentityStatus());
        GetOperationDetailResponse r4 = nextStepClient.getOperationDetail("test_operation_3").getResponseObject();
        assertEquals(AuthResult.DONE, r4.getResult());
    }

    @Test
    public void testTwoOtpsPerOperationFirstRemoved() throws NextStepClientException {
        CreateOperationResponse r1 = nextStepClient.createOperation("auth_otp", "test_operation_4", "A1", null, null).getResponseObject();
        assertEquals(AuthResult.CONTINUE, r1.getResult());
        CreateOtpResponse r2 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, "test_operation_4").getResponseObject();
        CreateOtpResponse r3 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, "test_operation_4").getResponseObject();
        GetOtpDetailResponse r4 = nextStepClient.getOtpDetail(r2.getOtpId(), null).getResponseObject();
        assertEquals(OtpStatus.REMOVED, r4.getOtpDetail().getOtpStatus());
        GetOtpDetailResponse r5 = nextStepClient.getOtpDetail(r3.getOtpId(), null).getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r5.getOtpDetail().getOtpStatus());
    }

    @Test
    public void testOperationFailureByOtpFailures() throws NextStepClientException {
        CreateOperationResponse r1 = nextStepClient.createOperation("auth_otp", "test_operation_5", "A1", null, null).getResponseObject();
        assertEquals(AuthResult.CONTINUE, r1.getResult());
        CreateOtpResponse r2 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, "test_operation_5").getResponseObject();
        OtpAuthenticationResponse r2a = nextStepClient.authenticateWithOtp(r2.getOtpId(), "test_operation_5", "00000000000", true, null).getResponseObject();
        assertEquals(2, (int) r2a.getRemainingAttempts());
        OtpAuthenticationResponse r2b = nextStepClient.authenticateWithOtp(r2.getOtpId(), "test_operation_5", "00000000000", true, null).getResponseObject();
        assertEquals(1, (int) r2b.getRemainingAttempts());
        OtpAuthenticationResponse r2c = nextStepClient.authenticateWithOtp(r2.getOtpId(), "test_operation_5", "00000000000", true, null).getResponseObject();
        assertEquals(0, (int) r2c.getRemainingAttempts());
        CreateOtpResponse r3 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, "test_operation_5").getResponseObject();
        OtpAuthenticationResponse r3a = nextStepClient.authenticateWithOtp(r3.getOtpId(), "test_operation_5", "00000000000", true, null).getResponseObject();
        assertEquals(1, (int) r3a.getRemainingAttempts());
        OtpAuthenticationResponse r4 = nextStepClient.authenticateWithOtp(r3.getOtpId(), "test_operation_5", "00000000000", true, null).getResponseObject();
        assertEquals(0, (int) r4.getRemainingAttempts());
        assertEquals(OtpStatus.BLOCKED, r4.getOtpStatus());
        assertTrue(r4.isOperationFailed());
        // Operation should fail now because of maximum failed attempts per operation
        GetOperationDetailResponse r5 = nextStepClient.getOperationDetail("test_operation_5").getResponseObject();
        assertEquals(AuthResult.FAILED, r5.getResult());
    }

    @Test
    public void testCredentialSuccessNoOperation() throws NextStepClientException {
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "s3cret").getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r1.getAuthenticationResult());
    }

    @Test
    public void testCredentialSuccessWithOperation() throws NextStepClientException {
        nextStepClient.createOperation("auth_otp", "test_operation_6", "A1", null, null);
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "s3cret", "test_operation_6", true, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r1.getAuthenticationResult());
        GetOperationDetailResponse r2 = nextStepClient.getOperationDetail("test_operation_6").getResponseObject();
        assertEquals(AuthResult.DONE, r2.getResult());
    }

    @Test
    public void testCredentialFailNoOperation() throws NextStepClientException {
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "secret").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
    }

    @Test
    public void testCredentialFailWithOperation() throws NextStepClientException {
        nextStepClient.createOperation("auth_otp", "test_operation_7", "A1", null, null);
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "secret", "test_operation_7", true, null).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        GetOperationDetailResponse r2 = nextStepClient.getOperationDetail("test_operation_7").getResponseObject();
        assertEquals(AuthResult.CONTINUE, r2.getResult());
    }

    @Test
    public void testCredentialFailUnknownUser() {
        try {
            nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "unknown_user", "secret");
        } catch (NextStepClientException ex) {
            assertEquals(UserNotFoundException.CODE, ex.getError().getCode());
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testCredentialAndOtpSuccessNoOperation() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA").getResponseObject();
        CombinedAuthenticationResponse r2 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "s3cret", r1.getOtpId(), r1.getOtpValue()).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getAuthenticationResult());
    }

    @Test
    public void testCredentialAndOtpSuccessWithOperation() throws NextStepClientException {
        nextStepClient.createOperation("auth_otp", "test_operation_8", "A1", null, null);
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA", "test_operation_8").getResponseObject();
        CombinedAuthenticationResponse r2 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "s3cret", r1.getOtpId(), "test_operation_8", r1.getOtpValue(), true, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getAuthenticationResult());
        GetOperationDetailResponse r3 = nextStepClient.getOperationDetail("test_operation_8").getResponseObject();
        assertEquals(AuthResult.DONE, r3.getResult());
    }

    @Test
    public void testCredentialAndOtpFailNoOperation1() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA").getResponseObject();
        CombinedAuthenticationResponse r2 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "secret", r1.getOtpId(), r1.getOtpValue()).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(AuthenticationResult.FAILED, r2.getCredentialAuthenticationResult());
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getOtpAuthenticationResult());
    }

    @Test
    public void testCredentialAndOtpFailNoOperation2() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA").getResponseObject();
        CombinedAuthenticationResponse r2 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "s3cret", r1.getOtpId(), "0000000000").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getCredentialAuthenticationResult());
        assertEquals(AuthenticationResult.FAILED, r2.getOtpAuthenticationResult());
    }

    @Test
    public void testCredentialAndOtpFailWithOperation1() throws NextStepClientException {
        nextStepClient.createOperation("auth_otp", "test_operation_9a", "A1", null, null);
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA", "test_operation_9a").getResponseObject();
        CombinedAuthenticationResponse r2 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "secret", r1.getOtpId(), "test_operation_9a", r1.getOtpValue(), true, null).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(AuthenticationResult.FAILED, r2.getCredentialAuthenticationResult());
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getOtpAuthenticationResult());
        GetOperationDetailResponse r3 = nextStepClient.getOperationDetail("test_operation_9a").getResponseObject();
        assertEquals(AuthResult.CONTINUE, r3.getResult());
    }

    @Test
    public void testCredentialAndOtpFailWithOperation2() throws NextStepClientException {
        nextStepClient.createOperation("auth_otp", "test_operation_9b", "A1", null, null);
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA", "test_operation_9b").getResponseObject();
        CombinedAuthenticationResponse r2 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "s3cret", r1.getOtpId(), "test_operation_9b", "0000000000", true, null).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getCredentialAuthenticationResult());
        assertEquals(AuthenticationResult.FAILED, r2.getOtpAuthenticationResult());
        GetOperationDetailResponse r3 = nextStepClient.getOperationDetail("test_operation_9b").getResponseObject();
        assertEquals(AuthResult.CONTINUE, r3.getResult());
    }

    @Test
    public void testUsersLookupSuccess() throws NextStepClientException {
        LookupUsersRequest request = new LookupUsersRequest();
        request.setUsername("testuser");
        request.setCredentialName("TEST_CREDENTIAL");
        LookupUsersResponse r1 = nextStepClient.lookupUsers(request).getResponseObject();
        assertEquals(1, r1.getUsers().size());
        assertEquals("test_user_1", r1.getUsers().get(0).getUserId());
    }

    @Test
    public void testUsersLookupFail() {
        LookupUsersRequest request = new LookupUsersRequest();
        request.setUsername("testuser_unknown");
        request.setCredentialName("TEST_CREDENTIAL");
        try {
            nextStepClient.lookupUsers(request);
        } catch (NextStepClientException ex) {
            assertEquals(UserNotFoundException.CODE, ex.getError().getCode());
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testUserLookupSuccess() throws NextStepClientException {
        LookupUserResponse r1 = nextStepClient.lookupUser("testuser", "TEST_CREDENTIAL").getResponseObject();
        assertEquals("test_user_1", r1.getUser().getUserId());
    }

    @Test
    public void testUserLookupFail() {
        try {
            nextStepClient.lookupUser("testuser_unknown", "TEST_CREDENTIAL");
        } catch (NextStepClientException ex) {
            assertEquals(UserNotFoundException.CODE, ex.getError().getCode());
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testOtpCreateBlockedUser() throws NextStepClientException {
        nextStepClient.blockUser("test_user_1");
        try {
            nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA");
        } catch (NextStepClientException ex) {
            assertEquals(UserNotActiveException.CODE, ex.getError().getCode());
            nextStepClient.unblockUser("test_user_1");
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testOtpVerifyBlockedUser() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        nextStepClient.blockUser("test_user_1");
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), r1.getOtpValue()).getResponseObject();
        assertEquals(UserIdentityStatus.BLOCKED, r2.getUserIdentityStatus());
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(0, (int) r2.getRemainingAttempts());
        nextStepClient.unblockUser("test_user_1");
    }

    @Test
    public void testCredentialVerifyBlockedUser() throws NextStepClientException {
        nextStepClient.blockUser("test_user_1");
        CredentialAuthenticationResponse r2 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "s3cret").getResponseObject();
        assertEquals(UserIdentityStatus.BLOCKED, r2.getUserIdentityStatus());
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(0, (int) r2.getRemainingAttempts());
        nextStepClient.unblockUser("test_user_1");
    }

    @Test
    public void testOtpAndCredentialVerifyBlockedUser() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        nextStepClient.blockUser("test_user_1");
        CombinedAuthenticationResponse r2 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "s3cret", r1.getOtpId(), r1.getOtpValue()).getResponseObject();
        assertEquals(UserIdentityStatus.BLOCKED, r2.getUserIdentityStatus());
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(AuthenticationResult.FAILED, r2.getOtpAuthenticationResult());
        assertEquals(AuthenticationResult.FAILED, r2.getCredentialAuthenticationResult());
        assertEquals(0, (int) r2.getRemainingAttempts());
        nextStepClient.unblockUser("test_user_1");
    }

    @Test
    public void testOtpFailBlockedCredential() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA").getResponseObject();
        nextStepClient.blockCredential("test_user_1", "TEST_CREDENTIAL");
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), r1.getOtpValue()).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(CredentialStatus.BLOCKED_PERMANENT, r2.getCredentialStatus());
        nextStepClient.unblockCredential("test_user_1", "TEST_CREDENTIAL");
    }

    @Test
    public void testCredentialFailBlockedCredential() throws NextStepClientException {
        nextStepClient.blockCredential("test_user_1", "TEST_CREDENTIAL");
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "s3cret").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        assertEquals(CredentialStatus.BLOCKED_PERMANENT, r1.getCredentialStatus());
        nextStepClient.unblockCredential("test_user_1", "TEST_CREDENTIAL");
    }

    @Test
    public void testUpdateCounterNoBlock() throws NextStepClientException {
        for (int i = 0; i < 6; i++) {
            CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "secret").getResponseObject();
            assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
            assertEquals(CredentialStatus.ACTIVE, r1.getCredentialStatus());
            nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.SUCCEEDED);
        }
    }

    @Test
    public void testUpdateCounterFailed() throws NextStepClientException {
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        GetUserCredentialListResponse r1 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.BLOCKED_TEMPORARY, r1.getCredentials().get(0).getCredentialStatus());
        try {
            nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.SUCCEEDED);
        } catch (NextStepClientException ex) {
            assertEquals(CredentialNotActiveException.CODE, ex.getError().getCode());
            nextStepClient.unblockCredential("test_user_1", "TEST_CREDENTIAL");
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testSoftCounterResetActive() throws NextStepClientException {
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        GetUserCredentialListResponse r1 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.ACTIVE, r1.getCredentials().get(0).getCredentialStatus());
        ResetCountersRequest resetCountersRequest = new ResetCountersRequest();
        resetCountersRequest.setResetMode(CounterResetMode.RESET_ACTIVE_AND_BLOCKED_TEMPORARY);
        ResetCountersResponse r2 = nextStepClient.resetAllCounters(resetCountersRequest).getResponseObject();
        assertEquals(1, r2.getResetCounterCount());
        GetUserCredentialListResponse r3 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.ACTIVE, r3.getCredentials().get(0).getCredentialStatus());
    }

    @Test
    public void testSoftCounterResetWithUnblock() throws NextStepClientException {
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        GetUserCredentialListResponse r1 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.BLOCKED_TEMPORARY, r1.getCredentials().get(0).getCredentialStatus());
        ResetCountersRequest resetCountersRequest = new ResetCountersRequest();
        resetCountersRequest.setResetMode(CounterResetMode.RESET_BLOCKED_TEMPORARY);
        ResetCountersResponse r2 = nextStepClient.resetAllCounters(resetCountersRequest).getResponseObject();
        assertEquals(1, r2.getResetCounterCount());
        GetUserCredentialListResponse r3 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.ACTIVE, r3.getCredentials().get(0).getCredentialStatus());
    }

    @Test
    public void testOperationMaxAuthFailsCredential() throws NextStepClientException {
        nextStepClient.createOperation("auth_otp", "test_operation_10", "A1", null, null);
        for (int i = 0; i < 4; i++) {
            CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "secret", "test_operation_10", true, null).getResponseObject();
            assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
            if (i < 3) {
                // 2 attempts for credential
                assertEquals(2, (int) r1.getRemainingAttempts());
            } else {
                // Last attempt for operation
                assertEquals(1, (int) r1.getRemainingAttempts());
            }
            GetOperationDetailResponse r2 = nextStepClient.getOperationDetail("test_operation_10").getResponseObject();
            assertEquals(AuthResult.CONTINUE, r2.getResult());
            // Avoid blocking the credential
            nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.SUCCEEDED);
        }
        // Operation status changes to FAILED due to 5th failed attempt
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", "secret", "test_operation_10", true, null).getResponseObject();
        assertEquals(0, (int) r1.getRemainingAttempts());
        assertTrue(r1.isOperationFailed());
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        GetOperationDetailResponse r2 = nextStepClient.getOperationDetail("test_operation_10").getResponseObject();
        assertEquals(AuthResult.FAILED, r2.getResult());
    }

    @Test
    public void testOperationMaxAuthFailsCombined() throws NextStepClientException {
        nextStepClient.createOperation("auth_otp", "test_operation_11", "A1", null, null);
        for (int i = 0; i < 4; i++) {
            nextStepClient.createOtp("test_user_1", "TEST_OTP", "TEST_CREDENTIAL", "TEST_DATA", "test_operation_11");
            CombinedAuthenticationResponse r1 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "secret", null, "test_operation_11", "0000000000", true, null).getResponseObject();
            assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
            if (i < 3) {
                // 2 attempts for OTP, 2 attempts for credential
                assertEquals(2, (int) r1.getRemainingAttempts());
            } else {
                // Last attempt for operation
                assertEquals(1, (int) r1.getRemainingAttempts());
            }
            GetOperationDetailResponse r2 = nextStepClient.getOperationDetail("test_operation_11").getResponseObject();
            assertEquals(AuthResult.CONTINUE, r2.getResult());
            // Avoid blocking the credential
            nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.SUCCEEDED);
        }
        // Operation status changes to FAILED due to 5th failed attempt
        CombinedAuthenticationResponse r1 = nextStepClient.authenticateCombined("TEST_CREDENTIAL", "test_user_1", "secret", null, "test_operation_11", "0000000000", true, null).getResponseObject();
        assertEquals(0, (int) r1.getRemainingAttempts());
        assertTrue(r1.isOperationFailed());
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        GetOperationDetailResponse r2 = nextStepClient.getOperationDetail("test_operation_11").getResponseObject();
        assertEquals(AuthResult.FAILED, r2.getResult());
    }

    @Test
    public void testCredentialSuccessE2EEncryption() throws NextStepClientException, CryptoProviderException, GenericCryptoException, InvalidKeyException {
        String credentialValue = "s3cret";
        String secretKeyBase64 = nextStepServerConfiguration.getE2eEncryptionKey();
        byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyBase64);
        SecretKey secretKey = keyConvertor.convertBytesToSharedSecretKey(secretKeyBytes);
        byte[] ivBytes = keyGenerator.generateRandomBytes(16);
        // Encrypt credential bytes using random IV, secret key and transformation
        byte[] credentialBytes = credentialValue.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedCredentialBytes = aes.encrypt(credentialBytes, ivBytes, secretKey, "AES/CBC/PKCS7Padding");
        String encryptedCredentialBase64 = Base64.getEncoder().encodeToString(encryptedCredentialBytes);
        String ivBase64 = Base64.getEncoder().encodeToString(ivBytes);
        String encryptedCredentialValue = ivBase64 + ":" + encryptedCredentialBase64;
        // Enable end-to-end encryption
        UpdateCredentialDefinitionRequest credentialDefinitionRequest = new UpdateCredentialDefinitionRequest();
        credentialDefinitionRequest.setCredentialDefinitionName("TEST_CREDENTIAL");
        credentialDefinitionRequest.setApplicationName("TEST_APP");
        credentialDefinitionRequest.setCredentialPolicyName("TEST_CREDENTIAL_POLICY");
        credentialDefinitionRequest.setCategory(CredentialCategory.PASSWORD);
        credentialDefinitionRequest.setHashingEnabled(true);
        credentialDefinitionRequest.setHashConfigName("ARGON2_TEST");
        credentialDefinitionRequest.setE2eEncryptionEnabled(true);
        credentialDefinitionRequest.setE2eEncryptionAlgorithm(EndToEndEncryptionAlgorithm.AES);
        credentialDefinitionRequest.setE2eEncryptionCipherTransformation("AES/CBC/PKCS7Padding");
        credentialDefinitionRequest.setE2eEncryptionForTemporaryCredentialEnabled(true);
        nextStepClient.updateCredentialDefinition(credentialDefinitionRequest);

        // Test authentication
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", encryptedCredentialValue).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r1.getAuthenticationResult());
        // Disable end-to-end encryption
        credentialDefinitionRequest.setE2eEncryptionEnabled(false);
        credentialDefinitionRequest.setE2eEncryptionAlgorithm(null);
        credentialDefinitionRequest.setE2eEncryptionCipherTransformation(null);
        credentialDefinitionRequest.setE2eEncryptionForTemporaryCredentialEnabled(false);
        nextStepClient.updateCredentialDefinition(credentialDefinitionRequest);
        // Test authentication
        CredentialAuthenticationResponse r2 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", credentialValue).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getAuthenticationResult());
    }

    @Test
    public void testOtpCheckSuccessNoOperation() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        assertEquals(8, r1.getOtpValue().length());
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), null, r1.getOtpValue(), true, false, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getAuthenticationResult());
        // OTP value was only checked, status OTP must stay ACTIVE for successful verification
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertEquals("test_user_1", r2.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUserIdentityStatus());
        assertEquals(3, (int) r2.getRemainingAttempts());
        OtpAuthenticationResponse r3 = nextStepClient.authenticateWithOtp(r1.getOtpId(), null, r1.getOtpValue(), false, false, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r3.getAuthenticationResult());
        // OTP value was used for actual authentication
        assertEquals(OtpStatus.USED, r3.getOtpStatus());
        assertEquals("test_user_1", r3.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r3.getUserIdentityStatus());
        assertEquals(0, (int) r3.getRemainingAttempts());
    }

    @Test
    public void testOtpCheckFailNoOperation() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), null, "00000000000", true, false, null).getResponseObject();
        // FAILED authentication result is reported in checkOnly mode as usual
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertEquals("test_user_1", r2.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUserIdentityStatus());
        assertEquals(2, (int) r2.getRemainingAttempts());
    }

    @Test
    public void testOtpCheckBlockNoOperation() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        nextStepClient.authenticateWithOtp(r1.getOtpId(), null, "00000000000", true, false, null);
        nextStepClient.authenticateWithOtp(r1.getOtpId(), null, "00000000000", true, false, null);
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), null, "00000000000", true, false, null).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r2.getAuthenticationResult());
        // OTP is blocked in checkOnly mode as usual
        assertEquals(OtpStatus.BLOCKED, r2.getOtpStatus());
        assertEquals("test_user_1", r2.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUserIdentityStatus());
        assertEquals(0, (int) r2.getRemainingAttempts());
    }

    @Test
    public void testOtpCheckSuccessWithOperation() throws NextStepClientException {
        CreateOperationResponse r1 = nextStepClient.createOperation("auth_otp", "test_operation_check_1", "A1", null, null).getResponseObject();
        assertEquals("test_operation_check_1", r1.getOperationId());
        assertEquals("A1", r1.getOperationData());
        assertEquals(AuthResult.CONTINUE, r1.getResult());
        CreateOtpResponse r2 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, "test_operation_check_1").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertNotNull(r2.getOtpId());
        assertNotNull(r2.getOtpValue());
        OtpAuthenticationResponse r3 = nextStepClient.authenticateWithOtp(null, "test_operation_check_1", r2.getOtpValue(), true, false, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r3.getAuthenticationResult());
        assertEquals(OtpStatus.ACTIVE, r3.getOtpStatus());
        assertEquals("test_user_1", r3.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r3.getUserIdentityStatus());
        GetOperationDetailResponse r4 = nextStepClient.getOperationDetail("test_operation_check_1").getResponseObject();
        assertEquals(AuthResult.CONTINUE, r4.getResult());
        OtpAuthenticationResponse r5 = nextStepClient.authenticateWithOtp(null, "test_operation_check_1", r2.getOtpValue(), false, true, null).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r5.getAuthenticationResult());
        assertEquals(OtpStatus.USED, r5.getOtpStatus());
        assertEquals("test_user_1", r5.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r5.getUserIdentityStatus());
        GetOperationDetailResponse r6 = nextStepClient.getOperationDetail("test_operation_check_1").getResponseObject();
        assertEquals(AuthResult.DONE, r6.getResult());
    }

    @Test
    public void testOtpCheckFailWithOperation() throws NextStepClientException {
        CreateOperationResponse r1 = nextStepClient.createOperation("auth_otp", "test_operation_check_2", "A1", null, null).getResponseObject();
        assertEquals("test_operation_check_2", r1.getOperationId());
        assertEquals("A1", r1.getOperationData());
        assertEquals(AuthResult.CONTINUE, r1.getResult());
        CreateOtpResponse r2 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, "test_operation_check_2").getResponseObject();
        assertEquals(OtpStatus.ACTIVE, r2.getOtpStatus());
        assertNotNull(r2.getOtpId());
        assertNotNull(r2.getOtpValue());
        nextStepClient.authenticateWithOtp(null, "test_operation_check_2", "00000000000", true, false, null);
        nextStepClient.authenticateWithOtp(null, "test_operation_check_2", "00000000000", true, false, null);
        OtpAuthenticationResponse r3 = nextStepClient.authenticateWithOtp(null, "test_operation_check_2", "00000000000", true, false, null).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r3.getAuthenticationResult());
        assertEquals(OtpStatus.BLOCKED, r3.getOtpStatus());
        assertEquals("test_user_1", r3.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r3.getUserIdentityStatus());
        GetOperationDetailResponse r4 = nextStepClient.getOperationDetail("test_operation_check_2").getResponseObject();
        assertEquals(AuthResult.CONTINUE, r4.getResult());
        OtpAuthenticationResponse r5 = nextStepClient.authenticateWithOtp(null, "test_operation_check_2", r2.getOtpValue(), false, true, null).getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r5.getAuthenticationResult());
        assertEquals(OtpStatus.BLOCKED, r5.getOtpStatus());
        assertEquals("test_user_1", r5.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r5.getUserIdentityStatus());
        GetOperationDetailResponse r6 = nextStepClient.getOperationDetail("test_operation_check_2").getResponseObject();
        assertEquals(AuthResult.FAILED, r6.getResult());
    }

}

