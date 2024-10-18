/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2024 Wultra s.r.o.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for password hashing.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CredentialProtectionServiceTest {

    @Autowired
    private CredentialProtectionService credentialProtectionService;

    @Test
    void testBcrypt() throws Exception {
        final CredentialEntity credentialEntity = new CredentialEntity();
        final UserIdentityEntity user = new UserIdentityEntity();
        final HashConfigEntity hashConfig = new HashConfigEntity();
        final CredentialDefinitionEntity credentialDefinition = new CredentialDefinitionEntity();
        credentialDefinition.setEncryptionAlgorithm(EncryptionAlgorithm.NO_ENCRYPTION);
        credentialDefinition.setHashingConfig(hashConfig);
        hashConfig.setAlgorithm(HashAlgorithm.BCRYPT);
        hashConfig.setParameters("{}");
        user.setUserId("test");
        credentialEntity.setUser(user);
        credentialEntity.setCredentialDefinition(credentialDefinition);
        credentialEntity.setHashingConfig(hashConfig);
        final CredentialValue hashed = credentialProtectionService.protectCredential("test", credentialEntity);
        credentialEntity.setValue(hashed.getValue());
        assertEquals(EncryptionAlgorithm.NO_ENCRYPTION, hashed.getEncryptionAlgorithm());
        assertNotEquals("test", hashed.getValue());
        assertTrue(hashed.getValue().startsWith("$2a$10$"));
        assertTrue(credentialProtectionService.verifyCredential("test", credentialEntity));
    }

    @Test
    void testArgon2() throws Exception {
        final CredentialEntity credentialEntity = new CredentialEntity();
        final UserIdentityEntity user = new UserIdentityEntity();
        final HashConfigEntity hashConfig = new HashConfigEntity();
        final CredentialDefinitionEntity credentialDefinition = new CredentialDefinitionEntity();
        credentialDefinition.setEncryptionAlgorithm(EncryptionAlgorithm.NO_ENCRYPTION);
        credentialDefinition.setHashingConfig(hashConfig);
        hashConfig.setAlgorithm(HashAlgorithm.ARGON_2I);
        final Map<String, String> params = Map.of(
                "version", "19",
                "iterations", "4",
                "memory", "16",
                "parallelism", "2",
                "outputLength", "32"
        );
        hashConfig.setParameters(new ObjectMapper().writeValueAsString(params));
        user.setUserId("test");
        credentialEntity.setUser(user);
        credentialEntity.setCredentialDefinition(credentialDefinition);
        credentialEntity.setHashingConfig(hashConfig);
        final CredentialValue hashed = credentialProtectionService.protectCredential("test", credentialEntity);
        credentialEntity.setValue(hashed.getValue());
        assertEquals(EncryptionAlgorithm.NO_ENCRYPTION, hashed.getEncryptionAlgorithm());
        assertNotEquals("test", hashed.getValue());
        assertTrue(hashed.getValue().startsWith("$argon2i$v=19$m=65536,t=4,p=2$"));
        assertTrue(credentialProtectionService.verifyCredential("test", credentialEntity));
    }

}