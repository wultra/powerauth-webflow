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
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotActiveException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotBlockedException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Next Step user identity tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
class NextStepUserIdentityTest extends NextStepTest {

    @BeforeEach
    void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);
    }

    @Test
    void testUserIdentityLifecycle() throws NextStepClientException {
        final String userId = UUID.randomUUID().toString();

        final CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.TEMPORARY);

        final CreateUserRequest.NewContact testContact = new CreateUserRequest.NewContact();
        testContact.setContactName("TEST_CONTACT");
        testContact.setContactType(ContactType.EMAIL);
        testContact.setContactValue("test@test.test");
        testContact.setPrimary(true);

        final CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId);
        createUserRequest.getCredentials().add(credential);
        createUserRequest.getRoles().add("TEST_ROLE");
        createUserRequest.getContacts().add(testContact);
        createUserRequest.getExtras().put("TEST_EXTRA", "TEST_VALUE");

        final CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r1.getUserIdentityStatus());
        assertEquals(userId, r1.getUserId());
        assertEquals(1, r1.getCredentials().size());
        assertEquals(1, r1.getContacts().size());
        assertEquals(1, r1.getExtras().size());

        final List<CredentialSecretDetail> credentials = r1.getCredentials();
        final CredentialSecretDetail c1 = credentials.get(0);
        final String username = c1.getUsername();
        final String credentialValue = c1.getCredentialValue();
        assertNotNull(username);
        assertNotNull(credentialValue);

        final LookupUserResponse r2 = nextStepClient.lookupUser(username, "TEST_CREDENTIAL").getResponseObject();
        assertEquals(userId, r2.getUser().getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r2.getUser().getUserIdentityStatus());

        final CredentialAuthenticationResponse r3 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", r2.getUser().getUserId(), credentialValue).getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r3.getAuthenticationResult());
        assertEquals(CredentialStatus.ACTIVE, r3.getCredentialStatus());

        final GetUserDetailResponse r4 = nextStepClient.getUserDetail(userId, false).getResponseObject();
        assertEquals(userId, r4.getUserId());
        assertEquals(1, r4.getCredentials().size());
        assertEquals(1, r4.getContacts().size());
        assertEquals(1, r4.getExtras().size());
        assertNotNull(r4.getTimestampCreated());
        assertNull(r4.getTimestampLastUpdated());

        // Delete user identity
        final DeleteUserResponse r5 = nextStepClient.deleteUser(userId).getResponseObject();
        assertEquals(UserIdentityStatus.REMOVED, r5.getUserIdentityStatus());

        final GetUserDetailResponse r6 = nextStepClient.getUserDetail(userId, true).getResponseObject();
        assertEquals(CredentialStatus.REMOVED, r6.getCredentials().get(0).getCredentialStatus());
        assertNull(r6.getCredentials().get(0).getUsername());

        // Revive user identity
        final UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUserId(userId);
        updateRequest.setUserIdentityStatus(UserIdentityStatus.ACTIVE);

        final UpdateUserResponse r7 = nextStepClient.updateUser(updateRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r7.getUserIdentityStatus());

        final GetUserDetailResponse r8 = nextStepClient.getUserDetail(userId, true).getResponseObject();
        // Check that the credential is removed
        assertEquals(CredentialStatus.REMOVED, r8.getCredentials().get(0).getCredentialStatus());

        // Delete user identity
        final DeleteUserResponse r9 = nextStepClient.deleteUser(userId).getResponseObject();
        assertEquals(UserIdentityStatus.REMOVED, r9.getUserIdentityStatus());

        // Revive user identity again, but this time generate new credential
        final UpdateUserRequest.UpdatedCredential updatedCredential = new UpdateUserRequest.UpdatedCredential();
        updatedCredential.setCredentialName("TEST_CREDENTIAL");
        updatedCredential.setCredentialType(CredentialType.PERMANENT);

        final UpdateUserRequest updateRequest2 = new UpdateUserRequest();
        updateRequest2.setUserId(userId);
        updateRequest2.setUserIdentityStatus(UserIdentityStatus.ACTIVE);
        updateRequest2.setCredentials(Collections.singletonList(updatedCredential));

        final UpdateUserResponse r10 = nextStepClient.updateUser(updateRequest2).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r10.getUserIdentityStatus());
        assertNotNull(r10.getCredentials().get(0).getCredentialValue());
        assertFalse(r10.getCredentials().get(0).isCredentialChangeRequired());

        final GetUserDetailResponse r11 = nextStepClient.getUserDetail(userId, false).getResponseObject();
        // Check that the credential is active
        assertEquals(CredentialStatus.ACTIVE, r11.getCredentials().get(0).getCredentialStatus());
        assertNotNull(r11.getTimestampLastUpdated());
        // Generated username should be different
        assertNotEquals(username, r11.getCredentials().get(0).getUsername());

        // Change username to a fixed one
        final UpdateUserRequest.UpdatedCredential updatedCredential2 = new UpdateUserRequest.UpdatedCredential();
        updatedCredential2.setCredentialName("TEST_CREDENTIAL");
        updatedCredential2.setCredentialType(CredentialType.PERMANENT);
        updatedCredential2.setUsername("test_username");
        updatedCredential2.setCredentialValue("tops3cret");

        final UpdateUserRequest updateRequest3 = new UpdateUserRequest();
        updateRequest3.setUserId(userId);
        updateRequest3.setExtras(new LinkedHashMap<>());
        updateRequest3.setCredentials(Collections.singletonList(updatedCredential2));

        final UpdateUserResponse r12 = nextStepClient.updateUser(updateRequest3).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r12.getUserIdentityStatus());
        assertTrue(r12.getExtras().isEmpty());
        assertEquals("test_username", r12.getCredentials().get(0).getUsername());
        // Credential is not returned when it is not generated
        assertNull(r12.getCredentials().get(0).getCredentialValue());
        assertFalse(r12.getCredentials().get(0).isCredentialChangeRequired());

        // Block and unblock test
        final BlockUserResponse r13 = nextStepClient.blockUser(userId).getResponseObject();
        assertEquals(userId, r13.getUserId());
        assertEquals(UserIdentityStatus.BLOCKED, r13.getUserIdentityStatus());

        final NextStepClientException thrown1 = assertThrows(NextStepClientException.class, () -> nextStepClient.blockUser(userId));
        assertEquals(UserNotActiveException.CODE, thrown1.getError().getCode());

        final UnblockUserResponse r15 = nextStepClient.unblockUser(userId).getResponseObject();
        assertEquals(userId, r15.getUserId());
        assertEquals(UserIdentityStatus.ACTIVE, r15.getUserIdentityStatus());

        final NextStepClientException thrown2 = assertThrows(NextStepClientException.class, () -> nextStepClient.unblockUser(userId));
        assertEquals(UserNotBlockedException.CODE, thrown2.getError().getCode());
    }

    @Test
    void testCreateUserWithSameUsername() throws Exception {
        final String userId1 = UUID.randomUUID().toString();

        final CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.TEMPORARY);

        final CreateUserRequest.NewContact testContact = new CreateUserRequest.NewContact();
        testContact.setContactName("TEST_CONTACT");
        testContact.setContactType(ContactType.EMAIL);
        testContact.setContactValue("test@test.test");
        testContact.setPrimary(true);

        final CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userId1);
        createUserRequest.getCredentials().add(credential);
        createUserRequest.getRoles().add("TEST_ROLE");
        createUserRequest.getContacts().add(testContact);
        createUserRequest.getExtras().put("TEST_EXTRA", "TEST_VALUE");

        final CreateUserResponse r1 = nextStepClient.createUser(createUserRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r1.getUserIdentityStatus());
        assertEquals(userId1, r1.getUserId());
        assertEquals(1, r1.getCredentials().size());
        final String username1 = r1.getCredentials().get(0).getUsername();
        assertNotNull(username1);

        // Delete user identity
        final DeleteUserResponse r2 = nextStepClient.deleteUser(userId1).getResponseObject();
        assertEquals(UserIdentityStatus.REMOVED, r2.getUserIdentityStatus());

        final GetUserDetailResponse r3 = nextStepClient.getUserDetail(userId1, true).getResponseObject();
        assertEquals(CredentialStatus.REMOVED, r3.getCredentials().get(0).getCredentialStatus());
        assertNull(r3.getCredentials().get(0).getUsername());

        // Create a new user with the username existed before
        final String userId2 = UUID.randomUUID().toString();
        createUserRequest.setUserId(userId2);
        credential.setUsername(username1);

        final CreateUserResponse r4 = nextStepClient.createUser(createUserRequest).getResponseObject();
        assertEquals(UserIdentityStatus.ACTIVE, r4.getUserIdentityStatus());
        assertEquals(userId2, r4.getUserId());
        assertEquals(1, r4.getCredentials().size());
        final String username2 = r1.getCredentials().get(0).getUsername();
        assertEquals(username1, username2);
    }

    @Test
    void testUserIdentityContacts() throws NextStepClientException {
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
        GetUserContactListResponse r6 = nextStepClient.getUserContactList(userId).getResponseObject();
        assertTrue(r6.getContacts().isEmpty());
    }

    @Test
    void testUserIdentityRoles() throws NextStepClientException {
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
    void testUserIdentityAliases() throws NextStepClientException {
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