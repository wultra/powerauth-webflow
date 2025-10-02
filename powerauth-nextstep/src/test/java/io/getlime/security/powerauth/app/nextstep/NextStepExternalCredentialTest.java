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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldif.LDIFReader;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialLocation;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CredentialAuthenticationResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Next Step authentication tests with credentials stored in LDAP.
 *
 * @author Zdenek Cerny, zdenek.cerny@wultra.com
 */
public class NextStepExternalCredentialTest extends NextStepTest {

    @Autowired
    private NextStepServerConfiguration nextStepServerConfiguration;

    @BeforeEach
    public void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);
    }

    private void prepareUser(String userIdentification) throws NextStepClientException {
        // Create user identity with LDAP credentials
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId(userIdentification);
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        credential.setUsername(userIdentification);
        credential.setCredentialSource(CredentialLocation.LDAP);
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
    }

    @Test
    public void testCreateUserWithExternalCredentials() throws NextStepClientException {
        prepareUser("test_user_ldap_1");
    }

    // create user
    // update user
    // create credential
    // update credential


}

