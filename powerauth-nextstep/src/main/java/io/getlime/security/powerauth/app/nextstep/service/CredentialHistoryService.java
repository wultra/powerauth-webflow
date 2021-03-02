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

import io.getlime.security.powerauth.app.nextstep.repository.CredentialHistoryRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * This service handles persistence of credential history.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialHistoryService {

    private final Logger logger = LoggerFactory.getLogger(CredentialHistoryService.class);

    private final CredentialHistoryRepository credentialHistoryRepository;

    @Autowired
    public CredentialHistoryService(CredentialHistoryRepository credentialHistoryRepository) {
        this.credentialHistoryRepository = credentialHistoryRepository;
    }

    /**
     * Create a credential history record.
     * @param credential Credential entity.
     */
    public void createCredentialHistory(CredentialEntity credential) {
        CredentialHistoryEntity credentialHistory = new CredentialHistoryEntity();
        credentialHistory.setCredentialDefinition(credential.getCredentialDefinition());
        credentialHistory.setUser(credential.getUser());
        credentialHistory.setUsername(credential.getUsername());
        credentialHistory.setValue(credential.getValue());
        credentialHistory.setTimestampCreated(new Date());
        credentialHistoryRepository.save(credentialHistory);
    }

    /**
     * Check credential history.
     * @param user User identity entity.
     * @param credentialValue Credential value to check.
     * @param credentialDefinition Credential definition.
     * @return True if credential check succeeded, false when credential check failed.
     */
    public boolean checkCredentialHistory(UserIdentityEntity user, String credentialValue, CredentialDefinitionEntity credentialDefinition) {
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        int credentialHistoryCount = credentialPolicy.getCheckHistoryCount();
        if (credentialHistoryCount == 0) {
            return true;
        }
        int historyPassCount = 0;
        List<CredentialHistoryEntity> history = credentialHistoryRepository.findAllByUserOrderByTimestampCreatedDesc(user);
        for (CredentialHistoryEntity h : history) {
            String originalValue = h.getValue();
            if (credentialValue.equals(originalValue)) {
                return false;
            }
            historyPassCount++;
            if (historyPassCount == credentialHistoryCount) {
                // Check is complete
                break;
            }
        }
        return true;
    }

}