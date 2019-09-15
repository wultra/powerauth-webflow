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
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppAppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.model.response.TppAppDetailResponse;
import io.getlime.security.powerauth.app.tppengine.service.TppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ObjectResponse<TppAppDetailResponse> fetchAppInfoFromClientId(@RequestParam("clientId") String clientId) throws TppAppNotFoundException {
        final TppAppDetailResponse response = tppService.fetchAppDetailByClientId(clientId);
        if (response != null) {
            return new ObjectResponse<>(response);
        } else {
            throw new TppAppNotFoundException(clientId);
        }
    }

}
