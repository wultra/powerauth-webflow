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
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

/**
 * This service handles persistence of credential counters.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialCounterService {

    private final Logger logger = LoggerFactory.getLogger(CredentialCounterService.class);

    private final CredentialRepository credentialRepository;
    private final ServiceCatalogue serviceCatalogue;

    /**
     * Credential counter service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     */
    @Autowired
    public CredentialCounterService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue) {
        this.credentialRepository = repositoryCatalogue.getCredentialRepository();
        this.serviceCatalogue = serviceCatalogue;
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();

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
        logger.info("Credential counter updated, user ID: {}, credential definition name: {}, attempt counter: {}, soft counter: {}, hard counter: {}, status: {}",
                credential.getUser().getUserId(), credential.getCredentialDefinition().getName(),
                credential.getAttemptCounter(), credential.getFailedAttemptCounterSoft(),
                credential.getFailedAttemptCounterHard(), credential.getStatus());
    }

    /**
     * Reset all soft failed attempt counters.
     *
     * Method behavior depends on the counter reset mode:
     * <ul>
     *     <li>RESET_BLOCKED_TEMPORARY - reset soft failed attempt counters for credentials with BLOCKED_TEMPORARY status, change status to ACTIVE</li>
     *     <li>RESET_ACTIVE_AND_BLOCKED_TEMPORARY - reset soft failed attempt counters for credentials with ACTIVE and BLOCKED_TEMPORARY statuses, change status to ACTIVE if required</li>
     * </ul>
     * @param request Reset counters request.
     * @return Reset counters response.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public ResetCountersResponse resetCounters(ResetCountersRequest request) throws InvalidRequestException {
        int resetCounter = 0;
        switch (request.getResetMode()) {
            case RESET_BLOCKED_TEMPORARY:
                resetCounter += credentialRepository.resetSoftFailedCountersForBlockedTemporaryStatus();
                logger.info("Soft failed attempt credential counters were reset for status BLOCKED_TEMPORARY and status was changed to ACTIVE, updated record count: {}", resetCounter);
                break;

            case RESET_ACTIVE_AND_BLOCKED_TEMPORARY:
                resetCounter += credentialRepository.resetSoftFailedCountersForBlockedTemporaryStatus();
                resetCounter += credentialRepository.resetSoftFailedCountersForActiveStatus();
                logger.info("Soft failed attempt credential counters were reset for statuses ACTIVE and BLOCKED_TEMPORARY, status was changed to ACTIVE, updated record count: {}", resetCounter);
                break;

            default:
                throw new InvalidRequestException("Invalid counter reset mode: " + request.getResetMode());

        }
        final ResetCountersResponse response = new ResetCountersResponse();
        response.setResetCounterCount(resetCounter);
        return response;
    }

}