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
import io.getlime.security.powerauth.app.tppengine.exception.ConsentNotFoundException;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentDetailResponse;
import io.getlime.security.powerauth.app.tppengine.service.ConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for providing information about consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping("consent")
public class ConsentInfoController {

    private final ConsentService consentService;

    @Autowired
    public ConsentInfoController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<ConsentDetailResponse> consentDetail(@RequestParam("id") String id) throws ConsentNotFoundException {
        final ConsentDetailResponse response = consentService.consentDetail(id);
        if (response == null) {
            throw new ConsentNotFoundException(id);
        } else {
            return new ObjectResponse<>(response);
        }
    }

}
