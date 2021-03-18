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
package io.getlime.security.powerauth.app.nextstep;

import com.google.common.io.BaseEncoding;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.crypto.lib.generator.KeyGenerator;
import io.getlime.security.powerauth.crypto.lib.model.exception.CryptoProviderException;
import io.getlime.security.powerauth.crypto.lib.model.exception.GenericCryptoException;
import io.getlime.security.powerauth.crypto.lib.util.AESEncryptionUtils;
import io.getlime.security.powerauth.crypto.lib.util.KeyConvertor;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.LookupUsersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import static org.junit.Assert.*;

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

    @Before
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
        nextStepClient.resetAllCounters();
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
            assertEquals(UserNotFoundException.CODE, ex.getNextStepError().getCode());
            return;
        }
        Assert.fail();
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
            assertEquals(UserNotFoundException.CODE, ex.getNextStepError().getCode());
            return;
        }
        Assert.fail();
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
            assertEquals(UserNotFoundException.CODE, ex.getNextStepError().getCode());
            return;
        }
        Assert.fail();
    }

    @Test
    public void testOtpCreateBlockedUser() throws NextStepClientException {
        nextStepClient.blockUser("test_user_1");
        try {
            nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA");
        } catch (NextStepClientException ex) {
            assertEquals(UserNotActiveException.CODE, ex.getNextStepError().getCode());
            nextStepClient.unblockUser("test_user_1");
            return;
        }
        Assert.fail();
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
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "TEST_DATA").getResponseObject();
        nextStepClient.blockUser("test_user_1");
        OtpAuthenticationResponse r2 = nextStepClient.authenticateWithOtp(r1.getOtpId(), r1.getOtpValue()).getResponseObject();
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
            assertEquals(CredentialNotActiveException.CODE, ex.getNextStepError().getCode());
            nextStepClient.unblockCredential("test_user_1", "TEST_CREDENTIAL");
            return;
        }
        Assert.fail();
    }

    @Test
    public void testSoftCounterReset() throws NextStepClientException {
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        nextStepClient.updateCredentialCounter("test_user_1", "TEST_CREDENTIAL", AuthenticationResult.FAILED);
        GetUserCredentialListResponse r1 = nextStepClient.getUserCredentialList("test_user_1", false).getResponseObject();
        assertEquals(CredentialStatus.BLOCKED_TEMPORARY, r1.getCredentials().get(0).getCredentialStatus());
        ResetCountersResponse r2 = nextStepClient.resetAllCounters().getResponseObject();
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
        byte[] secretKeyBytes = BaseEncoding.base64().decode(secretKeyBase64);
        SecretKey secretKey = keyConvertor.convertBytesToSharedSecretKey(secretKeyBytes);
        byte[] ivBytes = keyGenerator.generateRandomBytes(16);
        // Encrypt credential bytes using random IV, secret key and transformation
        byte[] credentialBytes = credentialValue.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedCredentialBytes = aes.encrypt(credentialBytes, ivBytes, secretKey, "AES/CBC/PKCS7Padding");
        String encryptedCredentialBase64 = BaseEncoding.base64().encode(encryptedCredentialBytes);
        String ivBase64 = BaseEncoding.base64().encode(ivBytes);
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
        credentialDefinitionRequest.setE2eEncryptionAlgorithm("AES");
        credentialDefinitionRequest.setE2eEncryptionCipherTransformation("AES/CBC/PKCS7Padding");
        nextStepClient.updateCredentialDefinition(credentialDefinitionRequest);

        // Test authentication
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", encryptedCredentialValue).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r1.getAuthenticationResult());
        // Disable end-to-end encryption
        credentialDefinitionRequest.setE2eEncryptionEnabled(false);
        credentialDefinitionRequest.setE2eEncryptionAlgorithm(null);
        credentialDefinitionRequest.setE2eEncryptionCipherTransformation(null);
        nextStepClient.updateCredentialDefinition(credentialDefinitionRequest);
        // Test authentication
        CredentialAuthenticationResponse r2 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_1", credentialValue).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r2.getAuthenticationResult());
    }

}

