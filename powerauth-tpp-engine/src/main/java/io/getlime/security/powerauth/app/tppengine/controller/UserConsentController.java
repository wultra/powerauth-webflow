/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.controller;

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.ConsentNotFoundException;
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
    @PostMapping("consent")
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
    @DeleteMapping("consent")
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
    @PostMapping("consent/delete")
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
    @DeleteMapping("consent/id")
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
    @PostMapping("consent/id/delete")
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
    @GetMapping("consent")
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
    @GetMapping("consent/status")
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
    @GetMapping("consent/history")
    public ObjectResponse<ConsentHistoryListResponse> consentHistory(@RequestParam("userId") String userId, @RequestParam(value = "clientId", required = false) String clientId) throws ConsentNotFoundException {
        ConsentHistoryListResponse response = userConsentService.consentHistoryForUser(userId, clientId);
        return new ObjectResponse<>(response);
    }

}
