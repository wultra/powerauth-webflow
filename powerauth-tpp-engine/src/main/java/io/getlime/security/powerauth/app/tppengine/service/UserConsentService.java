/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.ConsentNotFoundException;
import io.getlime.security.powerauth.app.tppengine.model.entity.GivenConsent;
import io.getlime.security.powerauth.app.tppengine.model.entity.GivenConsentHistory;
import io.getlime.security.powerauth.app.tppengine.model.enumeration.ConsentChange;
import io.getlime.security.powerauth.app.tppengine.model.request.GiveConsentRequest;
import io.getlime.security.powerauth.app.tppengine.model.request.RemoveConsentRequest;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentHistoryListResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentListResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.GiveConsentResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.UserConsentDetailResponse;
import io.getlime.security.powerauth.app.tppengine.repository.ConsentRepository;
import io.getlime.security.powerauth.app.tppengine.repository.UserConsentHistoryRepository;
import io.getlime.security.powerauth.app.tppengine.repository.UserConsentRepository;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.ConsentEntity;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.UserConsentEntity;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.UserConsentHistoryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Service responsible for managing consent that users gave to the TPP apps.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class UserConsentService {

    private final ConsentRepository consentRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserConsentHistoryRepository userConsentHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static Logger logger = LoggerFactory.getLogger(UserConsentService.class);

    @Autowired
    public UserConsentService(ConsentRepository consentRepository, UserConsentRepository userConsentRepository, UserConsentHistoryRepository userConsentHistoryRepository) {
        this.consentRepository = consentRepository;
        this.userConsentRepository = userConsentRepository;
        this.userConsentHistoryRepository = userConsentHistoryRepository;
    }

    /**
     * Provide a list of all consents for given user, for a given TPP app.
     *
     * @param userId User ID.
     * @param clientId Client ID (TPP app ID).
     * @return List of all current consents for given TPP app.
     * @throws ConsentNotFoundException In case some approved consent is not found due to database inconsistency.
     */
    @Transactional
    public ConsentListResponse consentListForUser(String userId, String clientId) throws ConsentNotFoundException {

        // Get the consents
        final List<UserConsentEntity> consentEntities;
        if (clientId != null) {
            consentEntities = userConsentRepository.findConsentsGivenByUserToApp(userId, clientId);
        } else {
            consentEntities = userConsentRepository.findAllConsentsGivenByUser(userId);
        }
        final List<GivenConsent> givenConsents = listConsentsByUser(consentEntities);

        // Prepare and return the response object
        ConsentListResponse response = new ConsentListResponse();
        response.setUserId(userId);
        response.setConsents(givenConsents);
        return response;
    }

    /**
     * Provide a consent status for given user, TPP app and consent type.
     *
     * @param userId User ID.
     * @param consentId Consent ID.
     * @param clientId Client ID (TPP app ID).
     * @return Information about consent status.
     * @throws ConsentNotFoundException In case some approved consent is not found due to database inconsistency.
     */
    @Transactional
    public UserConsentDetailResponse consentStatus(String userId, String consentId, String clientId) throws ConsentNotFoundException {

        // Check if a consent with given consent ID exists
        final Optional<ConsentEntity> consentOptional = consentRepository.findFirstById(consentId);
        if (!consentOptional.isPresent()) {
            throw new ConsentNotFoundException(consentId);
        }
        final ConsentEntity consent = consentOptional.get();

        final Optional<UserConsentEntity> consentStatusOptional = userConsentRepository.findConsentStatus(userId, consentId, clientId);
        if (consentStatusOptional.isPresent()) {

            final UserConsentEntity consentStatus = consentStatusOptional.get();

            //TODO: Replace by converter
            GivenConsent givenConsent = new GivenConsent();
            givenConsent.setId(consentStatus.getId());
            givenConsent.setClientId(consentStatus.getClientId());
            givenConsent.setConsentId(consentStatus.getConsentId());
            givenConsent.setConsentParameters(consentStatus.getParameters());
            givenConsent.setExternalId(consentStatus.getExternalId());
            givenConsent.setTimestampCreated(consentStatus.getTimestampCreated());
            givenConsent.setTimestampUpdated(consentStatus.getTimestampUpdated());
            givenConsent.setConsentName(consent.getName());
            givenConsent.setConsentText(consent.getText());

            UserConsentDetailResponse response = new UserConsentDetailResponse();
            response.setUserId(userId);
            response.setConsent(givenConsent);

            return response;
        } else {
            UserConsentDetailResponse response = new UserConsentDetailResponse();
            response.setUserId(userId);
            response.setConsent(null); // no consent given
            return response;
        }
    }

    /**
     * Create a consent approval for given TPP app by provided user.
     *
     * @param ro Request object with details of the consent.
     * @return Response related to consent approval.
     * @throws ConsentNotFoundException In case some approved consent is not found due to database inconsistency.
     */
    @Transactional
    public GiveConsentResponse giveConsent(GiveConsentRequest ro) throws ConsentNotFoundException {

        // Check if a consent with given consent exists
        final Optional<ConsentEntity> consentOptional = consentRepository.findFirstById(ro.getConsentId());
        if (!consentOptional.isPresent()) {
            throw new ConsentNotFoundException(ro.getConsentId());
        }
        final ConsentEntity consent = consentOptional.get();

        // Find if a user already gave this consent, so that it can be updated on save
        final Optional<UserConsentEntity> consentStatusOptional = userConsentRepository.findConsentStatus(ro.getUserId(), ro.getConsentId(), ro.getClientId());
        final UserConsentEntity consentStatus;
        final Date date = new Date();
        final boolean isApproved;
        if (consentStatusOptional.isPresent()) {
            consentStatus = consentStatusOptional.get();
            consentStatus.setExternalId(ro.getExternalId());
            consentStatus.setTimestampUpdated(date);
            isApproved = true;
        } else {
            consentStatus = new UserConsentEntity();
            consentStatus.setUserId(ro.getUserId());
            consentStatus.setConsentId(ro.getConsentId());
            consentStatus.setClientId(ro.getClientId());
            consentStatus.setParameters(convertToJsonString(ro.getParameters()));
            consentStatus.setExternalId(ro.getExternalId());
            consentStatus.setTimestampCreated(date);
            consentStatus.setTimestampUpdated(date);
            isApproved = false;
        }
        final UserConsentEntity savedConsentStatus = userConsentRepository.save(consentStatus);

        // Log a record to the history table
        logConsentChange(
                savedConsentStatus.getUserId(),
                savedConsentStatus.getConsentId(),
                savedConsentStatus.getClientId(),
                savedConsentStatus.getExternalId(),
                savedConsentStatus.getParameters(),
                date,
                isApproved ? ConsentChange.PROLONG : ConsentChange.APPROVE
        );

        // Return the response with the consent details
        GiveConsentResponse response = new GiveConsentResponse();
        response.setId(savedConsentStatus.getId());
        response.setUserId(savedConsentStatus.getUserId());
        response.setConsentId(savedConsentStatus.getConsentId());
        response.setClientId(savedConsentStatus.getClientId());
        response.setConsentParameters(savedConsentStatus.getParameters());
        response.setExternalId(savedConsentStatus.getExternalId());
        response.setConsentName(consent.getName());
        response.setConsentText(consent.getText());
        return response;
    }

    /**
     * Remove consent a user gave to a TPP app. In case the consent is not given, this method is a no-op.
     *
     * @param ro Request object with a consent to be removed.
     * @throws ConsentNotFoundException In case a consent with given ID is not found.
     */
    @Transactional
    public void removeConsent(RemoveConsentRequest ro) throws ConsentNotFoundException {

        // Check if a consent with given consent exists
        final Optional<ConsentEntity> consentOptional = consentRepository.findFirstById(ro.getConsentId());
        if (!consentOptional.isPresent()) {
            throw new ConsentNotFoundException(ro.getConsentId());
        }

        // In case a consent exists, reject it
        Optional<UserConsentEntity> consentStatusOptional = userConsentRepository.findConsentStatus(ro.getUserId(), ro.getConsentId(), ro.getClientId());
        if (consentStatusOptional.isPresent()) {
            final UserConsentEntity consentStatus = consentStatusOptional.get();
            userConsentRepository.deleteById(consentStatus.getId());

            // Log a record to the history table
            logConsentChange(
                    consentStatus.getUserId(),
                    consentStatus.getConsentId(),
                    consentStatus.getClientId(),
                    ro.getExternalId(),
                    consentStatus.getParameters(),
                    new Date(),
                    ConsentChange.REJECT
            );
        }

    }

    /**
     * Remove consent a user gave to a TPP app. In case a user consent with given ID does not exist, this
     * operation is a no-op.
     *
     * @param id User consent ID to be removed.
     * @throws ConsentNotFoundException In case some approved consent is not found due to database inconsistency.
     */
    @Transactional
    public void removeConsent(Long id) throws ConsentNotFoundException {

        // In case a consent exists, reject it
        Optional<UserConsentEntity> consentStatusOptional = userConsentRepository.findById(id);
        if (consentStatusOptional.isPresent()) {
            final UserConsentEntity consentStatus = consentStatusOptional.get();

            // Check if a consent with given consent exists
            final Optional<ConsentEntity> consentOptional = consentRepository.findFirstById(consentStatus.getConsentId());
            if (!consentOptional.isPresent()) { // should happen only in case of data inconsistency.
                throw new ConsentNotFoundException(consentStatus.getConsentId());
            }

            // Delete the consent with provided ID
            userConsentRepository.deleteById(consentStatus.getId());

            // Log a record to the history table
            logConsentChange(
                    consentStatus.getUserId(),
                    consentStatus.getConsentId(),
                    consentStatus.getClientId(),
                    null,
                    consentStatus.getParameters(),
                    new Date(),
                    ConsentChange.REJECT
            );
        }

    }

    /**
     * Get the consent history for a given user. In case a client ID is specified, the results are filtered
     * by the TPP application.
     *
     * @param userId User ID.
     * @param clientId (optional) Client ID.
     * @return Response with a consent history for given user (and optionally, a given app).
     * @throws ConsentNotFoundException In case of inconsistency and deleted consent.
     */
    @Transactional
    public ConsentHistoryListResponse consentHistoryForUser(String userId, String clientId) throws ConsentNotFoundException {

        // Get the list of consent history items
        List<UserConsentHistoryEntity> userConsentHistoryEntities;
        if (clientId == null) {
            userConsentHistoryEntities = userConsentHistoryRepository.consentHistoryForUser(userId);
        } else {
            userConsentHistoryEntities = userConsentHistoryRepository.consentHistoryForUser(userId, clientId);
        }

        // Iterate and convert the objects
        List<GivenConsentHistory> givenConsentHistoryList = new ArrayList<>();
        for (UserConsentHistoryEntity uche: userConsentHistoryEntities) {

            // Find the consent template
            final Optional<ConsentEntity> consentEntityOptional = consentRepository.findFirstById(uche.getConsentId());
            if (!consentEntityOptional.isPresent()) {
                throw new ConsentNotFoundException(uche.getConsentId());
            }
            final ConsentEntity consent = consentEntityOptional.get();

            //TODO: Replace by converter
            GivenConsentHistory givenConsentHistory = new GivenConsentHistory();
            givenConsentHistory.setId(uche.getId());
            givenConsentHistory.setConsentId(uche.getConsentId());
            givenConsentHistory.setClientId(uche.getClientId());
            givenConsentHistory.setTimestampCreated(uche.getTimestampCreated());
            givenConsentHistory.setChange(uche.getChange().name());
            givenConsentHistory.setConsentParameters(uche.getParameters());
            givenConsentHistory.setExternalId(uche.getExternalId());
            givenConsentHistory.setConsentName(consent.getName());
            givenConsentHistory.setConsentText(consent.getText());

            givenConsentHistoryList.add(givenConsentHistory);
        }

        // Prepare and return a response
        ConsentHistoryListResponse response = new ConsentHistoryListResponse();
        response.setUserId(userId);
        response.setHistory(givenConsentHistoryList);
        return response;
    }

    /**
     * Log a consent approval, prolongation or rejection to the history table.
     *
     * @param userId User ID.
     * @param consentId Consent ID.
     * @param clientId Client ID.
     * @param parameters Consent parameters.
     * @param date Timestamp of the change.
     */
    private void logConsentChange(String userId, String consentId, String clientId, String externalId, String parameters, Date date, ConsentChange change) {
        UserConsentHistoryEntity uche = new UserConsentHistoryEntity();
        uche.setUserId(userId);
        uche.setConsentId(consentId);
        uche.setClientId(clientId);
        uche.setExternalId(externalId);
        uche.setParameters(parameters);
        uche.setChange(change);
        uche.setTimestampCreated(date);
        userConsentHistoryRepository.save(uche);
    }

    /**
     * Get list of all consents based on provided approved consent entities.
     *
     * @param consentEntities Consent entities approved by a user.
     * @return List of given consents.
     * @throws ConsentNotFoundException In case some approved consent is not found due to database inconsistency.
     */
    private List<GivenConsent> listConsentsByUser(List<UserConsentEntity> consentEntities) throws ConsentNotFoundException {
        // Find all given consents by a user

        final List<GivenConsent> givenConsents = new ArrayList<>();
        for (UserConsentEntity uce: consentEntities) {

            // Find the consent template
            final Optional<ConsentEntity> consentEntityOptional = consentRepository.findFirstById(uce.getConsentId());
            if (!consentEntityOptional.isPresent()) {
                throw new ConsentNotFoundException(uce.getConsentId());
            }
            final ConsentEntity consent = consentEntityOptional.get();

            // Prepare the consent entity for the response
            //TODO: Replace by converter
            GivenConsent givenConsent = new GivenConsent();
            givenConsent.setId(uce.getId());
            givenConsent.setClientId(uce.getClientId());
            givenConsent.setConsentId(uce.getConsentId());
            givenConsent.setConsentParameters(uce.getParameters());
            givenConsent.setExternalId(uce.getExternalId());
            givenConsent.setTimestampCreated(uce.getTimestampCreated());
            givenConsent.setTimestampUpdated(uce.getTimestampUpdated());
            givenConsent.setConsentName(consent.getName());
            givenConsent.setConsentText(consent.getText());

            // Add the consent entity to the list
            givenConsents.add(givenConsent);
        }

        return givenConsents;
    }

    /**
     * Convert Map to JSON String.
     * @param parameters Map.
     * @return JSON String.
     */
    private String convertToJsonString(Map<String, String> parameters) {
        try {
            return objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            logger.warn("Unable to serialize JSON string from object.", e);
            return null;
        }
    }

}
