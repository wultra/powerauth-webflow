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
                logger.debug("Credential history check failed for user: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
                return false;
            }
            historyPassCounter++;
            if (historyPassCounter == credentialHistoryCount) {
                // Check is complete
                break;
            }
        }
        logger.debug("Credential history check succeeded for user: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        return true;
    }

}