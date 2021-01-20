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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.nextstep.model.request.ResetCountersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCounterRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.ResetCountersResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCounterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for counter management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/counter")
public class CredentialCounterController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialCounterController.class);

    @RequestMapping(value = "counter", method = RequestMethod.PUT)
    public ObjectResponse<UpdateCounterResponse> updateCredential(@RequestBody ObjectRequest<UpdateCounterRequest> request) {
        return new ObjectResponse<>(new UpdateCounterResponse());
    }

    @RequestMapping(value = "counter/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCounterResponse> updateCredentialPost(@RequestBody ObjectRequest<UpdateCounterRequest> request) {
        return new ObjectResponse<>(new UpdateCounterResponse());
    }

    @RequestMapping(value = "counter/reset-all", method = RequestMethod.PUT)
    public ObjectResponse<ResetCountersResponse> resetAllCounters(@RequestBody ObjectRequest<ResetCountersRequest> request) {
        return new ObjectResponse<>(new ResetCountersResponse());
    }

}
