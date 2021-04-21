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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.exception.EncryptionException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

/**
 * This service handles persistence of credential history.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialHistoryService {

    private final Logger logger = LoggerFactory.getLogger(CredentialHistoryService.class);

    private final ServiceCatalogue serviceCatalogue;

    /**
     * Service constructor.
     * @param serviceCatalogue Service catalogue.
     */
    @Autowired
    public CredentialHistoryService(@Lazy ServiceCatalogue serviceCatalogue) {
        this.serviceCatalogue = serviceCatalogue;
    }

    /**
     * Create a credential history record.
     * @param credential Credential entity.
     */
    public void createCredentialHistory(UserIdentityEntity user, CredentialEntity credential, Date createdDate) {
        final CredentialHistoryEntity credentialHistory = new CredentialHistoryEntity();
        credentialHistory.setCredentialDefinition(credential.getCredentialDefinition());
        credentialHistory.setUser(credential.getUser());
        credentialHistory.setUsername(credential.getUsername());
        credentialHistory.setValue(credential.getValue());
        credentialHistory.setHashingConfig(credential.getHashingConfig());
        if (credential.getCredentialDefinition().isEncryptionEnabled()) {
            credentialHistory.setEncryptionAlgorithm(credential.getEncryptionAlgorithm());
        }
        credentialHistory.setTimestampCreated(createdDate);
        user.getCredentialHistory().add(credentialHistory);
    }

    /**
     * Check credential history.
     * @param user User identity entity.
     * @param credentialValue Credential value to check.
     * @param credentialDefinition Credential definition.
     * @return True if credential check succeeded, false when credential check failed.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public boolean checkCredentialHistory(UserIdentityEntity user, String credentialValue, CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException, EncryptionException {
        final CredentialProtectionService credentialProtectionService = serviceCatalogue.getCredentialProtectionService();
        final CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        final int credentialHistoryCount = credentialPolicy.getCheckHistoryCount();
        if (credentialHistoryCount == 0) {
            return true;
        }
        int historyPassCounter = 0;
        final Set<CredentialHistoryEntity> history = user.getCredentialHistory();
        for (CredentialHistoryEntity h : history) {
            final boolean matchFound = credentialProtectionService.verifyCredentialHistory(credentialValue, h);
            if (matchFound) {
                return false;
            }
            historyPassCounter++;
            if (historyPassCounter == credentialHistoryCount) {
                // Check is complete
                break;
            }
        }
        return true;
    }

}