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
import io.getlime.security.powerauth.app.nextstep.service.HashConfigService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashConfigAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashConfigNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetHashConfigListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetHashConfigListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateHashConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("hashconfig")
public class HashConfigController {

    private static final Logger logger = LoggerFactory.getLogger(HashConfigController.class);

    private final HashConfigService hashConfigService;

    @Autowired
    public HashConfigController(HashConfigService hashConfigService) {
        this.hashConfigService = hashConfigService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateHashConfigResponse> createHashConfig(@RequestBody ObjectRequest<CreateHashConfigRequest> request) throws InvalidRequestException, HashConfigAlreadyExistsException {
        // TODO - request validation
        CreateHashConfigResponse response = hashConfigService.createHashConfig(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateHashConfigResponse> updateCredentialDefinition(@RequestBody ObjectRequest<UpdateHashConfigRequest> request) throws HashConfigNotFoundException, InvalidRequestException {
        // TODO - request validation
        UpdateHashConfigResponse response = hashConfigService.updateHashConfig(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateHashConfigResponse> updateCredentialDefinitionPost(@RequestBody ObjectRequest<UpdateHashConfigRequest> request) throws HashConfigNotFoundException, InvalidRequestException {
        // TODO - request validation
        UpdateHashConfigResponse response = hashConfigService.updateHashConfig(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetHashConfigListResponse> listHashConfigs(@RequestBody ObjectRequest<GetHashConfigListRequest> request) throws InvalidConfigurationException {
        GetHashConfigListResponse response = hashConfigService.getHashConfigList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteHashConfigResponse> deleteHashConfig(@RequestBody ObjectRequest<DeleteHashConfigRequest> request) throws HashConfigNotFoundException {
        // TODO - request validation
        DeleteHashConfigResponse response = hashConfigService.deleteHashConfig(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
