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

import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotBlockedException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Next Step user identity tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepUserIdentityTest extends NextStepTest {

    @Before
    public void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);
    }

    @Test
    public void userIdentityLifecycleTest() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.TEMPORARY);
        createUserRequest.getCredentials().add(credential);
        createUserRequest.getRoles().add("TEST_ROLE");
        CreateUserRequest.NewContact testContact = new CreateUserRequest.NewContact();
        testContact.setContactName("TEST_CONTACT");
        testContact.setContactType(ContactType.EMAIL);
        testContact.setContactValue("test@test.test");
        testContact.setPrimary(true);
        createUserRequest.getContacts().add(testContact);
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("TEST_EXTRA", "TEST_VALUE");
        createUserRequest.getExtras().putAll(extras);
        CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r1.getUserIdentityStatus());
        assertEquals(userId, r1.getUserId());
        assertEquals(1, r1.getCredentials().size());
        assertEquals(1, r1.getContacts().size());
        assertEquals(1, r1.getExtras().size());
        List<CredentialSecretDetail> credentials = r1.getCredentials();
        CredentialSecretDetail c1 = credentials.get(0);
        String username = c1.getUsername();
        String credentialValue = c1.getCredentialValue();
        assertNotNull(username);
        assertNotNull(credentialValue);
        LookupUserResponse r2 = nextStepClient.lookupUser(username, "TEST_CREDENTIAL").getResponseObject();
        assertEquals(userId, r2.getUser().getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUser().getUserIdentityStatus());
        CredentialAuthenticationResponse r3 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", r2.getUser().getUserId(), credentialValue).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r3.getAuthenticationResult());
        assertEquals(CredentialStatus.ACTIVE, r3.getCredentialStatus());
        GetUserDetailResponse r4 = nextStepClient.getUserDetail(userId, false).getResponseObject();
        assertEquals(userId, r4.getUserId());
        assertEquals(1, r4.getCredentials().size());
        assertEquals(1, r4.getContacts().size());
        assertEquals(1, r4.getExtras().size());
        assertNotNull(r4.getTimestampCreated());
        assertNull(r4.getTimestampLastUpdated());
        // Delete user identity
        DeleteUserResponse r5 = nextStepClient.deleteUser(userId).getResponseObject();
        assertEquals(UserIdentityStatus.REMOVED, r5.getUserIdentityStatus());
        GetUserDetailResponse r6 = nextStepClient.getUserDetail(userId, true).getResponseObject();
        assertEquals(CredentialStatus.REMOVED, r6.getCredentials().get(0).getCredentialStatus());
        // Revive user identity
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUserId(userId);
        updateRequest.setUserIdentityStatus(UserIdentityStatus.ACTIVE);
        UpdateUserResponse r7 = nextStepClient.updateUser(updateRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r7.getUserIdentityStatus());
        GetUserDetailResponse r8 = nextStepClient.getUserDetail(userId, true).getResponseObject();
        // Check that the credential is removed
        assertEquals(CredentialStatus.REMOVED, r8.getCredentials().get(0).getCredentialStatus());
        // Delete user identity
        DeleteUserResponse r9 = nextStepClient.deleteUser(userId).getResponseObject();
        assertEquals(UserIdentityStatus.REMOVED, r9.getUserIdentityStatus());
        // Revive user identity again, but this time generate new credential
        UpdateUserRequest updateRequest2 = new UpdateUserRequest();
        updateRequest2.setUserId(userId);
        updateRequest2.setUserIdentityStatus(UserIdentityStatus.ACTIVE);
        UpdateUserRequest.UpdatedCredential updatedCredential = new UpdateUserRequest.UpdatedCredential();
        updatedCredential.setCredentialName("TEST_CREDENTIAL");
        updatedCredential.setCredentialType(CredentialType.PERMANENT);
        updateRequest2.setCredentials(Collections.singletonList(updatedCredential));
        UpdateUserResponse r10 = nextStepClient.updateUser(updateRequest2).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r10.getUserIdentityStatus());
        assertNotNull(r10.getCredentials().get(0).getCredentialValue());
        assertFalse(r10.getCredentials().get(0).isCredentialChangeRequired());
        GetUserDetailResponse r11 = nextStepClient.getUserDetail(userId, false).getResponseObject();
        // Check that the credential is active
        assertEquals(CredentialStatus.ACTIVE, r11.getCredentials().get(0).getCredentialStatus());
        assertNotNull(r11.getTimestampLastUpdated());
        // Generated username should be different
        assertNotEquals(username, r11.getCredentials().get(0).getUsername());
        // Change username to a fixed one
        UpdateUserRequest updateRequest3 = new UpdateUserRequest();
        updateRequest3.setUserId(userId);
        Map<String, Object> newExtras = new LinkedHashMap<>();
        updateRequest3.setExtras(newExtras);
        UpdateUserRequest.UpdatedCredential updatedCredential2 = new UpdateUserRequest.UpdatedCredential();
        updatedCredential2.setCredentialName("TEST_CREDENTIAL");
        updatedCredential2.setCredentialType(CredentialType.PERMANENT);
        updatedCredential2.setUsername("test_username");
        updatedCredential2.setCredentialValue("tops3cret");
        updateRequest3.setCredentials(Collections.singletonList(updatedCredential2));
        UpdateUserResponse r12 = nextStepClient.updateUser(updateRequest3).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r12.getUserIdentityStatus());
        assertTrue(r12.getExtras().isEmpty());
        assertEquals("test_username", r12.getCredentials().get(0).getUsername());
        // Credential is not returned when it is not generated
        assertNull(r12.getCredentials().get(0).getCredentialValue());
        assertFalse(r12.getCredentials().get(0).isCredentialChangeRequired());
        // Block and unblock test
        BlockUserResponse r13 = nextStepClient.blockUser(userId).getResponseObject();
        assertEquals(userId, r13.getUserId());
        assertEquals(UserIdentityStatus.BLOCKED, r13.getUserIdentityStatus());
        try {
            nextStepClient.blockUser(userId);
        } catch (NextStepClientException ex) {
            assertEquals(UserNotActiveException.CODE, ex.getNextStepError().getCode());
        }
        UnblockUserResponse r15 = nextStepClient.unblockUser(userId).getResponseObject();
        assertEquals(userId, r15.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r15.getUserIdentityStatus());
        try {
            nextStepClient.unblockUser(userId);
        } catch (NextStepClientException ex) {
            assertEquals(UserNotBlockedException.CODE, ex.getNextStepError().getCode());
        }
    }

    @Test
    public void userIdentityContactsTest() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r1.getUserIdentityStatus());
        assertEquals(userId, r1.getUserId());
        assertTrue(r1.getContacts().isEmpty());
        CreateUserContactResponse r2 = nextStepClient.createUserContact(userId, "TEST_CONTACT", ContactType.PHONE, "2335321", true).getResponseObject();
        assertEquals(userId, r2.getUserId());
        assertEquals("TEST_CONTACT", r2.getContactName());
        assertEquals(ContactType.PHONE, r2.getContactType());
        assertEquals("2335321", r2.getContactValue());
        assertTrue(r2.isPrimary());
        UpdateUserContactResponse r3 = nextStepClient.updateUserContact(userId, "TEST_CONTACT", ContactType.PHONE, "6623234", true).getResponseObject();
        assertEquals("6623234", r3.getContactValue());
        GetUserContactListResponse r4 = nextStepClient.getUserContactList(userId).getResponseObject();
        assertEquals(1, r4.getContacts().size());
        assertEquals("6623234", r4.getContacts().get(0).getContactValue());
        assertNotNull(r4.getContacts().get(0).getTimestampLastUpdated());
        DeleteUserContactResponse r5 = nextStepClient.deleteUserContact(userId,"TEST_CONTACT", ContactType.PHONE).getResponseObject();
        assertEquals(userId, r5.getUserId());
    }

    @Test
    public void userIdentityRolesTest() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r1.getUserIdentityStatus());
        assertEquals(userId, r1.getUserId());
        assertTrue(r1.getRoles().isEmpty());
        AddUserRoleResponse r2 = nextStepClient.addUserRole(userId, "TEST_ROLE").getResponseObject();
        assertEquals(userId, r2.getUserId());
        assertEquals("TEST_ROLE", r2.getRoleName());
        assertEquals(UserRoleStatus.ACTIVE, r2.getUserRoleStatus());
        GetUserDetailResponse r3 = nextStepClient.getUserDetail(userId, false).getResponseObject();
        assertEquals(1, r3.getRoles().size());
        assertEquals("TEST_ROLE", r3.getRoles().get(0));
        RemoveUserRoleResponse r4 = nextStepClient.removeUserRole(userId, "TEST_ROLE").getResponseObject();
        assertEquals(userId, r4.getUserId());
        assertEquals("TEST_ROLE", r4.getRoleName());
        assertEquals(UserRoleStatus.REMOVED, r4.getUserRoleStatus());
        GetUserDetailResponse r5 = nextStepClient.getUserDetail(userId, false).getResponseObject();
        assertTrue(r5.getRoles().isEmpty());
    }

    @Test
    public void userIdentityAliasesTest() throws NextStepClientException {
        String userId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r1.getUserIdentityStatus());
        assertEquals(userId, r1.getUserId());
        assertTrue(r1.getRoles().isEmpty());
        CreateUserAliasResponse r2 = nextStepClient.createUserAlias(userId, "TEST_ALIAS", "TEST_VALUE", null).getResponseObject();
        assertEquals(userId, r2.getUserId());
        assertEquals("TEST_ALIAS", r2.getAliasName());
        assertEquals("TEST_VALUE", r2.getAliasValue());
        assertTrue(r2.getExtras().isEmpty());
        GetUserAliasListResponse r3 = nextStepClient.getUserAliasList(userId, false).getResponseObject();
        assertEquals(userId, r3.getUserId());
        assertEquals(1, r3.getAliases().size());
        assertEquals("TEST_ALIAS", r3.getAliases().get(0).getAliasName());
        assertEquals("TEST_VALUE", r3.getAliases().get(0).getAliasValue());
        UpdateUserAliasResponse r4 = nextStepClient.updateUserAlias(userId, "TEST_ALIAS", "TEST_VALUE_2", null).getResponseObject();
        assertEquals(userId, r4.getUserId());
        assertEquals("TEST_ALIAS", r4.getAliasName());
        assertEquals("TEST_VALUE_2", r4.getAliasValue());
        assertTrue(r4.getExtras().isEmpty());
        GetUserAliasListResponse r5 = nextStepClient.getUserAliasList(userId, false).getResponseObject();
        assertEquals(userId, r5.getUserId());
        assertEquals(1, r5.getAliases().size());
        assertEquals("TEST_ALIAS", r5.getAliases().get(0).getAliasName());
        assertEquals("TEST_VALUE_2", r5.getAliases().get(0).getAliasValue());
        DeleteUserAliasResponse r6 = nextStepClient.deleteUserAlias(userId, "TEST_ALIAS").getResponseObject();
        assertEquals(UserAliasStatus.REMOVED, r6.getUserAliasStatus());
        GetUserAliasListResponse r7 = nextStepClient.getUserAliasList(userId, false).getResponseObject();
        assertEquals(userId, r7.getUserId());
        assertTrue(r7.getAliases().isEmpty());
    }

}