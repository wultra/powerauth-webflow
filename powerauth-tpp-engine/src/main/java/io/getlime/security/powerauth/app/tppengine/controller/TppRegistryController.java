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

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppAppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.UnableToCreateAppException;
import io.getlime.security.powerauth.app.tppengine.model.entity.TppInfo;
import io.getlime.security.powerauth.app.tppengine.model.request.CreateTppAppRequest;
import io.getlime.security.powerauth.app.tppengine.model.response.TppAppDetailResponse;
import io.getlime.security.powerauth.app.tppengine.model.validator.CreateTppAppRequestValidator;
import io.getlime.security.powerauth.app.tppengine.service.TppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for lookup and registration of TPP service providers
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping("tpp")
public class TppRegistryController {

    private final TppService tppService;

    @Autowired
    public TppRegistryController(TppService tppService) {
        this.tppService = tppService;
    }

    @RequestMapping(value = "app", method = RequestMethod.GET)
    public ObjectResponse<TppAppDetailResponse> fetchAppInfoFromClientId(
            @RequestParam("clientId") String clientId,
            @RequestParam(value = "tppLicense", required = false) String tppLicense) throws TppAppNotFoundException, TppNotFoundException {
        if (tppLicense != null) {
            final TppAppDetailResponse response = tppService.fetchAppDetailByClientId(clientId, tppLicense);
            return new ObjectResponse<>(response);
        } else {
            final TppAppDetailResponse response = tppService.fetchAppDetailByClientId(clientId);
            return new ObjectResponse<>(response);
        }
    }

    @RequestMapping(value = "app/list", method = RequestMethod.GET)
    public ObjectResponse<List<TppAppDetailResponse>> getAppList(@RequestParam("tppLicense") String tppLicense) throws TppNotFoundException {
        final List<TppAppDetailResponse> response = tppService.fetchAppListByTppLicense(tppLicense);
        if (response != null) {
            return new ObjectResponse<>(response);
        } else {
            throw new TppNotFoundException("tpp.notFound", tppLicense);
        }
    }

    @RequestMapping(value = "app", method = RequestMethod.POST)
    public ObjectResponse<TppAppDetailResponse> createApp(@RequestBody ObjectRequest<CreateTppAppRequest> request) throws UnableToCreateAppException {
        final CreateTppAppRequest requestObject = request.getRequestObject();
        final List<String> errors = CreateTppAppRequestValidator.validate(requestObject);
        if (errors != null) { // request was not valid
            throw new UnableToCreateAppException(errors);
        }
        final TppAppDetailResponse tppAppDetailResponse = tppService.createApp(requestObject);
        return new ObjectResponse<>(tppAppDetailResponse);
    }

    @RequestMapping(value = "app", method = RequestMethod.PUT)
    public ObjectResponse<TppAppDetailResponse> updateApp(
            @RequestParam("clientId") String clientId,
            @RequestBody ObjectRequest<CreateTppAppRequest> request) throws UnableToCreateAppException, TppNotFoundException, TppAppNotFoundException {
        final CreateTppAppRequest requestObject = request.getRequestObject();
        final List<String> errors = CreateTppAppRequestValidator.validate(requestObject);
        if (errors != null) { // request was not valid
            throw new UnableToCreateAppException(errors);
        }
        final TppAppDetailResponse tppAppDetailResponse = tppService.updateApp(clientId, requestObject);
        return new ObjectResponse<>(tppAppDetailResponse);
    }

    @RequestMapping(value = "app/renewSecret", method = RequestMethod.POST)
    public ObjectResponse<TppAppDetailResponse> updateApp(
            @RequestParam("clientId") String clientId,
            @RequestParam("tppLicense") String tppLicense) throws TppNotFoundException, TppAppNotFoundException {
        final TppAppDetailResponse tppAppDetailResponse = tppService.renewAppSecret(clientId, tppLicense);
        return new ObjectResponse<>(tppAppDetailResponse);
    }

    @RequestMapping(value = "app", method = RequestMethod.DELETE)
    public Response deleteApp(
            @RequestParam("clientId") String clientId,
            @RequestParam("tppLicense") String tppLicense) throws TppNotFoundException, TppAppNotFoundException {
        tppService.deleteApp(clientId, tppLicense);
        return new Response();
    }

    @RequestMapping(value = "block", method = RequestMethod.POST)
    public ObjectResponse<TppInfo> blockTpp(@RequestParam("tppLicense") String tppLicense) throws TppNotFoundException {
        final TppInfo tppInfo = tppService.blockTpp(tppLicense);
        return new ObjectResponse<>(tppInfo);
    }

    @RequestMapping(value = "unblock", method = RequestMethod.POST)
    public ObjectResponse<TppInfo> unblockTpp(@RequestParam("tppLicense") String tppLicense) throws TppNotFoundException {
        final TppInfo tppInfo = tppService.unblockTpp(tppLicense);
        return new ObjectResponse<>(tppInfo);
    }

}
