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

import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
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
    private final CredentialService credentialService;
    private final CredentialRepository credentialRepository;

    /**
     * Credential counter service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param credentialDefinitionService Credential definition service.
     * @param credentialService Credential service.
     * @param credentialRepository Credential repository.
     */
    @Autowired
    public CredentialCounterService(UserIdentityLookupService userIdentityLookupService, CredentialDefinitionService credentialDefinitionService, CredentialService credentialService, CredentialRepository credentialRepository) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.credentialDefinitionService = credentialDefinitionService;
        this.credentialService = credentialService;
        this.credentialRepository = credentialRepository;
    }

    /**
     * Update credential soft and hard failed attempt counters based on authentication result.
     * @param request Update counter request.
     * @return Update counter response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public UpdateCounterResponse updateCredentialCounter(UpdateCounterRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidRequestException, CredentialNotActiveException {
        final UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final CredentialEntity credential = credentialService.findActiveCredential(credentialDefinition, user);
        updateCredentialCounter(credential, request.getAuthenticationResult());
        UpdateCounterResponse response = new UpdateCounterResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialStatus(credential.getStatus());
        return response;
    }

    /**
     * Update credential counter. This method is not transactional.
     * @param credential Credential entity.
     * @param authenticationResult Authentication result.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    public void updateCredentialCounter(CredentialEntity credential, AuthenticationResult authenticationResult) throws InvalidRequestException {
        final CredentialDefinitionEntity credentialDefinition = credential.getCredentialDefinition();
        final Integer softLimit = credentialDefinition.getCredentialPolicy().getLimitSoft();
        final Integer hardLimit = credentialDefinition.getCredentialPolicy().getLimitHard();
        switch (authenticationResult) {
            case SUCCEEDED:
                credential.setFailedAttemptCounterSoft(0);
                credential.setFailedAttemptCounterHard(0);
                break;

            case FAILED:
                credential.setFailedAttemptCounterSoft(credential.getFailedAttemptCounterSoft() + 1);
                credential.setFailedAttemptCounterHard(credential.getFailedAttemptCounterHard() + 1);
                if (hardLimit != null && credential.getFailedAttemptCounterHard() >= hardLimit
                        && credential.getStatus() != CredentialStatus.BLOCKED_PERMANENT) {
                    credential.setStatus(CredentialStatus.BLOCKED_PERMANENT);
                    credential.setTimestampBlocked(new Date());
                } else if (softLimit != null && credential.getFailedAttemptCounterSoft() >= softLimit
                        && credential.getStatus() != CredentialStatus.BLOCKED_TEMPORARY) {
                    credential.setStatus(CredentialStatus.BLOCKED_TEMPORARY);
                    credential.setTimestampBlocked(new Date());
                }
                break;

            default:
                throw new InvalidRequestException("Invalid authentication result: " + authenticationResult);

        }
        credential = credentialRepository.save(credential);
        logger.info("Credential counter updated, user ID: {}, credential name: {}, attempt counter: {}, soft counter: {}, hard counter: {}, status: {}",
                credential.getUser().getUserId(), credential.getCredentialDefinition().getName(),
                credential.getAttemptCounter(), credential.getFailedAttemptCounterSoft(),
                credential.getFailedAttemptCounterHard(), credential.getStatus());
    }

    /**
     * Reset all soft counters and unblock temporarily blocked credentials.
     * @param request Reset counters request.
     * @return Reset counters response.
     */
    @Transactional
    public ResetCountersResponse resetCounters(ResetCountersRequest request) {
        final List<CredentialEntity> blockedCredentials = credentialRepository.findAllByStatus(CredentialStatus.BLOCKED_TEMPORARY);
        int resetCounter = 0;
        // Reset soft counters for credentials with status BLOCKED_TEMPORARY and set status to ACTIVE
        for (CredentialEntity credential: blockedCredentials) {
            credential.setStatus(CredentialStatus.ACTIVE);
            credential.setFailedAttemptCounterSoft(0);
            credential.setTimestampBlocked(null);
            resetCounter++;
        }
        credentialRepository.saveAll(blockedCredentials);
        // Reset soft counters for credentials with status ACTIVE with non-zero soft counter
        final List<CredentialEntity> activeCredentials = credentialRepository.findAllByStatus(CredentialStatus.ACTIVE);
        for (CredentialEntity credential: activeCredentials) {
            if (credential.getFailedAttemptCounterSoft() != 0) {
                credential.setFailedAttemptCounterSoft(0);
                resetCounter++;
            }
        }
        credentialRepository.saveAll(activeCredentials);
        final ResetCountersResponse response = new ResetCountersResponse();
        response.setResetCounterCount(resetCounter);
        return response;
    }
}