/*
 * Copyright 2012 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialDefinitionNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.ResetCountersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCounterRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.ResetCountersResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCounterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * This service handles persistence of credential counters.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialCounterService {

    private final Logger logger = LoggerFactory.getLogger(CredentialCounterService.class);

    private final UserIdentityLookupService userIdentityLookupService;
    private final CredentialDefinitionService credentialDefinitionService;
    private final CredentialRepository credentialRepository;

    /**
     * Credential counter service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param credentialDefinitionService Credential definition service.
     * @param credentialRepository Credential repository.
     */
    @Autowired
    public CredentialCounterService(UserIdentityLookupService userIdentityLookupService, CredentialDefinitionService credentialDefinitionService, CredentialRepository credentialRepository) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.credentialDefinitionService = credentialDefinitionService;
        this.credentialRepository = credentialRepository;
    }

    /**
     * Update credential soft and hard failed attempt counters based on authentication result.
     * @param request Update counter request.
     * @return Update counter response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public UpdateCounterResponse updateCredentialCounter(UpdateCounterRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidRequestException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        credential.setAttemptCounter(credential.getAttemptCounter() + 1);
        Integer softLimit = credentialDefinition.getCredentialPolicy().getLimitSoft();
        Integer hardLimit = credentialDefinition.getCredentialPolicy().getLimitHard();
        switch (request.getAuthenticationResult()) {
            case SUCCEEDED:
                if (credential.getStatus() != CredentialStatus.ACTIVE) {
                    throw new InvalidRequestException("Credential is not ACTIVE: " + request.getCredentialName() + ", user ID: " + user.getUserId());
                }
                credential.setFailedAttemptCounterSoft(0L);
                credential.setFailedAttemptCounterHard(0L);
                break;

            case FAILED:
                if (credential.getStatus() == CredentialStatus.ACTIVE) {
                    credential.setFailedAttemptCounterSoft(credential.getFailedAttemptCounterSoft() + 1);
                    credential.setFailedAttemptCounterHard(credential.getFailedAttemptCounterHard() + 1);
                } else if (credential.getStatus() == CredentialStatus.BLOCKED_TEMPORARY) {
                    credential.setFailedAttemptCounterHard(credential.getFailedAttemptCounterHard() + 1);
                }
                if (hardLimit != null && credential.getFailedAttemptCounterHard() >= hardLimit) {
                    if (credential.getStatus() != CredentialStatus.BLOCKED_PERMANENT) {
                        credential.setStatus(CredentialStatus.BLOCKED_PERMANENT);
                        credential.setTimestampBlocked(new Date());
                    }
                } else if (softLimit != null && credential.getFailedAttemptCounterSoft() >= softLimit
                        && credential.getStatus() != CredentialStatus.BLOCKED_TEMPORARY) {
                    if (credential.getStatus() != CredentialStatus.BLOCKED_TEMPORARY) {
                        credential.setStatus(CredentialStatus.BLOCKED_TEMPORARY);
                        credential.setTimestampBlocked(new Date());
                    }
                }
                break;

            default:
                throw new InvalidRequestException("Invalid authentication result: " + request.getAuthenticationResult());

        }
        UpdateCounterResponse response = new UpdateCounterResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialStatus(credential.getStatus());
        return response;
    }

    /**
     * Reset all soft counters and unblock temporarily blocked credentials.
     * @param request Reset counters request.
     * @return Reset counters response.
     */
    @Transactional
    public ResetCountersResponse resetCounters(ResetCountersRequest request) {
        List<CredentialEntity> blockedCredentials = credentialRepository.findAllByStatus(CredentialStatus.BLOCKED_TEMPORARY);
        for (CredentialEntity credential: blockedCredentials) {
            credential.setStatus(CredentialStatus.ACTIVE);
            credential.setFailedAttemptCounterSoft(0L);
            credential.setTimestampBlocked(null);
        }
        credentialRepository.saveAll(blockedCredentials);
        ResetCountersResponse response = new ResetCountersResponse();
        response.setResetCounterCount(blockedCredentials.size());
        return response;
    }
}