/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2025 Wultra s.r.o.
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
package com.wultra.security.powerauth.app.nextstep;

import com.wultra.security.powerauth.lib.nextstep.client.NextStepClientException;
import com.wultra.security.powerauth.lib.nextstep.model.entity.CredentialDetail;
import com.wultra.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialLocation;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import com.wultra.security.powerauth.lib.nextstep.model.request.*;
import com.wultra.security.powerauth.lib.nextstep.model.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Next Step management tests with credentials stored in external system e.g. LDAP.
 *
 * @author Zdenek Cerny, zdenek.cerny@wultra.com
 */
class NextStepExternalCredentialTest extends NextStepTest {

    public static final String TEST_CREDENTIAL_NAME = "TEST_CREDENTIAL";

    @BeforeEach
    void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);
    }

    private CreateUserResponse prepareUser(String userIdentification, CredentialLocation target) throws NextStepClientException {
        // Create user identity with LDAP credentials
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userIdentification);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName(TEST_CREDENTIAL_NAME);
        credential.setCredentialType(CredentialType.PERMANENT);
        credential.setUsername(userIdentification);
        credential.setCredentialSource(CredentialLocation.LDAP);
        credential.setCredentialTarget(target);
        createUserRequest.getCredentials().add(credential);
        return nextStepClient.createUser(createUserRequest).getResponseObject();
    }

    @Test
    void testCreateUpdateExternalCredentials() throws NextStepClientException {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId("test_user_external_cred_1");
        nextStepClient.createUser(createUserRequest);

        CreateCredentialRequest credential = new CreateCredentialRequest();
        credential.setCredentialName(TEST_CREDENTIAL_NAME);
        credential.setCredentialType(CredentialType.PERMANENT);
        credential.setUserId("test_user_external_cred_1");
        credential.setUsername("test_user_external_cred_1");
        credential.setCredentialSource(CredentialLocation.LDAP);

        CreateCredentialResponse createCredentialResponse = nextStepClient.createCredential(credential).getResponseObject();
        assertEquals(CredentialLocation.LOCAL, createCredentialResponse.getCredentialTarget());
        assertEquals(CredentialLocation.LDAP, createCredentialResponse.getCredentialSource());

        UpdateCredentialRequest updateCredentialRequest = new UpdateCredentialRequest();
        updateCredentialRequest.setCredentialName(TEST_CREDENTIAL_NAME);
        updateCredentialRequest.setUserId("test_user_external_cred_1");
        updateCredentialRequest.setCredentialTarget(CredentialLocation.LDAP);

        UpdateCredentialResponse updateCredentialResponse = nextStepClient.updateCredential(updateCredentialRequest).getResponseObject();
        assertEquals(CredentialLocation.LDAP, updateCredentialResponse.getCredentialTarget());
        assertEquals(CredentialLocation.LDAP, updateCredentialResponse.getCredentialSource());

        updateCredentialRequest = new UpdateCredentialRequest();
        updateCredentialRequest.setCredentialName(TEST_CREDENTIAL_NAME);
        updateCredentialRequest.setUserId("test_user_external_cred_1");
        updateCredentialRequest.setCredentialValue("s3cret");
        updateCredentialRequest.setCredentialTarget(CredentialLocation.LOCAL);

        updateCredentialResponse = nextStepClient.updateCredential(updateCredentialRequest).getResponseObject();
        assertEquals(CredentialLocation.LOCAL, updateCredentialResponse.getCredentialTarget());
        assertEquals(CredentialLocation.LOCAL, updateCredentialResponse.getCredentialSource());
    }

    @Test
    public void testResetUpdateExternalCredentials() throws NextStepClientException {
        prepareUser("test_user_external_cred_2", CredentialLocation.LDAP);

        ResetCredentialRequest resetCredentialRequest = new ResetCredentialRequest();
        resetCredentialRequest.setUserId("test_user_external_cred_2");
        resetCredentialRequest.setCredentialName(TEST_CREDENTIAL_NAME);
        nextStepClient.resetCredential(resetCredentialRequest);

        UpdateCredentialRequest updateCredentialRequest = new UpdateCredentialRequest();
        updateCredentialRequest.setCredentialName(TEST_CREDENTIAL_NAME);
        updateCredentialRequest.setUserId("test_user_external_cred_2");
        updateCredentialRequest.setCredentialValue("s3cret");

        UpdateCredentialResponse updateCredentialResponse = nextStepClient.updateCredential(updateCredentialRequest).getResponseObject();
        assertEquals(CredentialLocation.LDAP, updateCredentialResponse.getCredentialTarget());
        assertEquals(CredentialLocation.LDAP, updateCredentialResponse.getCredentialSource());
    }

    @Test
    public void testCreateUpdateUserWithExternalCredentials() throws NextStepClientException {
        CreateUserResponse user = prepareUser("test_user_external_cred_3", null);
        CredentialSecretDetail credential = user.getCredentials().get(0);
        assertEquals(CredentialLocation.LOCAL, credential.getCredentialTarget());
        assertEquals(CredentialLocation.LDAP, credential.getCredentialSource());

        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setUserId("test_user_external_cred_3");
        UpdateUserRequest.UpdatedCredential updateCredential = new UpdateUserRequest.UpdatedCredential();
        updateCredential.setCredentialName(TEST_CREDENTIAL_NAME);
        updateCredential.setCredentialTarget(CredentialLocation.LDAP);
        updateCredential.setCredentialType(CredentialType.PERMANENT);
        userRequest.setCredentials(List.of(updateCredential));

        // update user actually creates a new credential, not updating the new one.
        UpdateUserResponse updateUserResponse = nextStepClient.updateUserPost(userRequest).getResponseObject();
        CredentialSecretDetail credentialSecretDetail = updateUserResponse.getCredentials().get(0);
        assertEquals(CredentialLocation.LDAP, credentialSecretDetail.getCredentialTarget());
        assertEquals(CredentialLocation.LOCAL, credentialSecretDetail.getCredentialSource());

        updateCredential.setCredentialName(TEST_CREDENTIAL_NAME);
        updateCredential.setCredentialSource(CredentialLocation.LDAP);
        updateCredential.setCredentialTarget(CredentialLocation.LOCAL);
        userRequest.setCredentials(List.of(updateCredential));

        updateUserResponse = nextStepClient.updateUserPost(userRequest).getResponseObject();
        credentialSecretDetail = updateUserResponse.getCredentials().get(0);
        assertEquals(CredentialLocation.LOCAL, credentialSecretDetail.getCredentialTarget());
        assertEquals(CredentialLocation.LDAP, credentialSecretDetail.getCredentialSource());

        ResetCredentialRequest resetCredentialRequest = new ResetCredentialRequest();
        resetCredentialRequest.setUserId("test_user_external_cred_3");
        resetCredentialRequest.setCredentialName(TEST_CREDENTIAL_NAME);
        nextStepClient.resetCredential(resetCredentialRequest);

        // RESET only reset the credentials, not change the location
        GetUserDetailResponse userDetailResponse = nextStepClient.getUserDetail("test_user_external_cred_3", false).getResponseObject();
        CredentialDetail credentialDetail = userDetailResponse.getCredentials().get(0);
        assertEquals(CredentialLocation.LOCAL, credentialDetail.getCredentialTarget());
        assertEquals(CredentialLocation.LDAP, credentialDetail.getCredentialSource());
    }
}