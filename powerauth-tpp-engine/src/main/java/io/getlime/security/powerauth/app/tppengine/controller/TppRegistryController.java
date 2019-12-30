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

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppAppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.UnableToCreateAppException;
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
            @RequestParam("tppLicense") String tppLicense) throws TppAppNotFoundException, TppNotFoundException {
        final TppAppDetailResponse response = tppService.fetchAppDetailByClientId(clientId, tppLicense);
        return new ObjectResponse<>(response);
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
    public ObjectResponse<TppAppDetailResponse> createApp(@RequestBody CreateTppAppRequest request) throws UnableToCreateAppException {
        final List<String> errors = CreateTppAppRequestValidator.validate(request);
        if (errors != null) { // request was not valid
            throw new UnableToCreateAppException(errors);
        }
        final TppAppDetailResponse tppAppDetailResponse = tppService.createApp(request);
        return new ObjectResponse<>(tppAppDetailResponse);
    }

    @RequestMapping(value = "app", method = RequestMethod.PUT)
    public ObjectResponse<TppAppDetailResponse> updateApp(
            @RequestParam("clientId") String clientId,
            @RequestBody CreateTppAppRequest request) throws UnableToCreateAppException, TppNotFoundException, TppAppNotFoundException {
        final List<String> errors = CreateTppAppRequestValidator.validate(request);
        if (errors != null) { // request was not valid
            throw new UnableToCreateAppException(errors);
        }
        final TppAppDetailResponse tppAppDetailResponse = tppService.updateApp(clientId, request);
        return new ObjectResponse<>(tppAppDetailResponse);
    }

    @RequestMapping(value = "app/renewSecret", method = RequestMethod.POST)
    public ObjectResponse<TppAppDetailResponse> updateApp(
            @RequestParam("clientId") String clientId,
            @RequestParam("tppLicense") String tppLicense) throws UnableToCreateAppException, TppNotFoundException, TppAppNotFoundException {
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

}
