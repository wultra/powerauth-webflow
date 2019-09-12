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

package io.getlime.security.powerauth.app.tppengine.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.tppengine.exception.ConsentNotFoundException;
import io.getlime.security.powerauth.app.tppengine.model.request.GiveConsentRequest;
import io.getlime.security.powerauth.app.tppengine.model.request.RemoveConsentRequest;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentHistoryListResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentListResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.GiveConsentResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.UserConsentDetailResponse;
import io.getlime.security.powerauth.app.tppengine.service.UserConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for providing information about consents given or rejected by given users.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping("user")
public class UserConsentController {

    private final UserConsentService userConsentService;

    @Autowired
    public UserConsentController(UserConsentService userConsentService) {
        this.userConsentService = userConsentService;
    }

    /**
     * Create a new approved consent to a TPP app for given user ID.
     *
     * @param request Information about the consent to create.
     * @return Response with consent created.
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent", method = RequestMethod.POST)
    public ObjectResponse<GiveConsentResponse> giveConsent(@RequestBody ObjectRequest<GiveConsentRequest> request) throws ConsentNotFoundException {
        final GiveConsentRequest requestObject = request.getRequestObject();
        //TODO: add validator
        GiveConsentResponse response = userConsentService.giveConsent(requestObject);
        return new ObjectResponse<>(response);
    }

    /**
     * Remove (reject) a consent from a TPP app for given user ID.
     *
     * @param request Information about consent to remove.
     * @return Information about success or failure.
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent", method = RequestMethod.DELETE)
    public Response removeConsent(@RequestBody ObjectRequest<RemoveConsentRequest> request) throws ConsentNotFoundException {
        RemoveConsentRequest requestObject = request.getRequestObject();
        //TODO: add validator
        userConsentService.removeConsent(requestObject);
        return new Response();
    }

    /**
     * Remove (reject) a consent from a TPP app for given user ID. Backup method using POST, instead of DELETE.
     *
     * @param request Information about consent to remove.
     * @return Information about success or failure.
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent/delete", method = RequestMethod.POST)
    public Response removeConsentPost(@RequestBody ObjectRequest<RemoveConsentRequest> request) throws ConsentNotFoundException {
        RemoveConsentRequest requestObject = request.getRequestObject();
        //TODO: add validator
        userConsentService.removeConsent(requestObject);
        return new Response();
    }

    /**
     * Remove (reject) a consent with given ID.
     *
     * @param consentId Consent ID.
     * @return Information about success or failure.
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent/id", method = RequestMethod.DELETE)
    public Response removeConsent(@RequestParam("cid") Long consentId) throws ConsentNotFoundException {
        userConsentService.removeConsent(consentId);
        return new Response();
    }

    /**
     * Remove (reject) a consent with given ID. Backup method using POST, instead of DELETE.
     *
     * @param consentId Consent ID.
     * @return Information about success or failure.
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent/id/delete", method = RequestMethod.POST)
    public Response removeConsentPost(@RequestParam("cid") Long consentId) throws ConsentNotFoundException {
        userConsentService.removeConsent(consentId);
        return new Response();
    }

    /**
     * Return the list of all current consents for a given user and optionally, filtered for given TPP app.
     *
     * @param userId User ID.
     * @param clientId (optional) TPP App Client ID.
     * @return List of consents that are currently active.
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent", method = RequestMethod.GET)
    public ObjectResponse<ConsentListResponse> consentList(@RequestParam("userId") String userId, @RequestParam(value = "clientId", required = false) String clientId) throws ConsentNotFoundException {
        final ConsentListResponse response = userConsentService.consentListForUser(userId, clientId);
        return new ObjectResponse<>(response);
    }

    /**
     * Return the detail of current status of a consent for a given user and third party app.
     *
     * @param userId User ID.
     * @param clientId TPP App Client ID.
     * @param consentId Consent ID.
     * @return Check the consent status
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent/status", method = RequestMethod.GET)
    public ObjectResponse<UserConsentDetailResponse> consentStatus(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId, @RequestParam("consentId") String consentId) throws ConsentNotFoundException {
        UserConsentDetailResponse response = userConsentService.consentStatus(userId, consentId, clientId);
        return new ObjectResponse<>(response);
    }

    /**
     * History of consent approval or rejection for given user. Optionally, a TPP app may be specified to
     * narrow down the results.
     *
     * @param userId User ID.
     * @param clientId (optional) TPP App Client ID.
     * @return List of history items.
     * @throws ConsentNotFoundException In case the consent with given ID is not found.
     */
    @RequestMapping(value = "consent/history", method = RequestMethod.GET)
    public ObjectResponse<ConsentHistoryListResponse> consentHistory(@RequestParam("userId") String userId, @RequestParam(value = "clientId", required = false) String clientId) throws ConsentNotFoundException {
        ConsentHistoryListResponse response = userConsentService.consentHistoryForUser(userId, clientId);
        return new ObjectResponse<>(response);
    }

}
