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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldif.LDIFReader;
import com.wultra.security.powerauth.lib.nextstep.client.NextStepClientException;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialLocation;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import com.wultra.security.powerauth.lib.nextstep.model.request.CreateUserRequest;
import com.wultra.security.powerauth.lib.nextstep.model.response.CredentialAuthenticationResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Next Step authentication tests with credentials stored in LDAP.
 *
 * @author Zdenek Cerny, zdenek.cerny@wultra.com
 */
class NextStepLdapAuthenticationTest extends NextStepTest {

    private static InMemoryDirectoryServer ds;

    @BeforeAll
    static void startLdap() throws Exception {
        var cfg = new InMemoryDirectoryServerConfig("dc=example,dc=com");
        cfg.setSchema(null);
        cfg.setEnforceSingleStructuralObjectClass(false);
        cfg.setEnforceAttributeSyntaxCompliance(false);
        cfg.addAdditionalBindCredentials(
                "cn=svc_ldap_search,dc=example,dc=com", "serviceSecret");
        cfg.addAdditionalBindCredentials(
                "cn=svc_ldap_search2,dc=example,dc=com", "serviceSecret");
        // Listen on localhost:10389 (change if that port is occupied)
        cfg.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig(
                "default", InetAddress.getByName("localhost"), 10389, null));

        ds = new InMemoryDirectoryServer(cfg);
        ds.startListening();

        try (InputStream ldif = new ClassPathResource("users.ldif").getInputStream()) {
            ds.importFromLDIF(true, new LDIFReader(ldif));
        }
    }

    @AfterAll
    static void stopLdap() {
        if (ds != null) ds.shutDown(true);
    }

    @BeforeEach
    public void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);
    }

    private void prepareUser(String userIdentification) throws NextStepClientException {
        prepareUser(userIdentification, null);
    }

    private void prepareUser(String userIdentification, String externalReference) throws NextStepClientException {
        // Create user identity with LDAP credentials
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userIdentification);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        credential.setUsername(userIdentification);
        credential.setCredentialSource(CredentialLocation.LDAP);
        if (externalReference != null) {
            credential.setExternalReference(externalReference);
        }
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
    }

    @Test
   void testCredentialLdapVerify() throws NextStepClientException {
        prepareUser("test_user_ldap_1");
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_ldap_1", "correct-password").getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r1.getAuthenticationResult());
        assertEquals(CredentialStatus.ACTIVE, r1.getCredentialStatus());
    }

    @Test
    public void testCredentialLdapVerifyFailNoLdapAttributes() throws NextStepClientException {
        prepareUser("test_user_ldap_2");
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_ldap_2", "incorrect-password").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        assertEquals(3, r1.getRemainingAttempts());
        assertEquals(CredentialStatus.ACTIVE, r1.getCredentialStatus());
    }

    @Test
    public void testCredentialLdapVerifyFail() throws NextStepClientException {
        prepareUser("test_user_ldap_3");
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_ldap_3", "incorrect-password").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        assertEquals(1, r1.getRemainingAttempts());
        assertEquals(CredentialStatus.ACTIVE, r1.getCredentialStatus());
    }

    @Test
    public void testCredentialLdapVerifyFailUnexpectedMetaData() throws NextStepClientException {
        prepareUser("test_user_ldap_4");
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_ldap_4", "incorrect-password").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        assertEquals(3, r1.getRemainingAttempts());
        assertEquals(CredentialStatus.ACTIVE, r1.getCredentialStatus());
    }

    @Test
    public void testCredentialLdapVerifyUserNotFound() throws NextStepClientException {
        prepareUser("test_user_ldap_5");
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_ldap_5", "irrelevant-password").getResponseObject();
        assertEquals(AuthenticationResult.FAILED, r1.getAuthenticationResult());
        assertEquals(3, r1.getRemainingAttempts());
        assertEquals(CredentialStatus.ACTIVE, r1.getCredentialStatus());
    }

    @Test
    public void testCredentialLdapVerifyUserCustomReference() throws NextStepClientException {
        prepareUser("test_user_ldap_6", "o=exampleorg,c=cs");
        CredentialAuthenticationResponse r1 = nextStepClient.authenticateWithCredential("TEST_CREDENTIAL", "test_user_ldap_6", "correct-password").getResponseObject();
        assertEquals(AuthenticationResult.SUCCEEDED, r1.getAuthenticationResult());
        assertEquals(CredentialStatus.ACTIVE, r1.getCredentialStatus());
        assertEquals(3, r1.getRemainingAttempts());
    }
}