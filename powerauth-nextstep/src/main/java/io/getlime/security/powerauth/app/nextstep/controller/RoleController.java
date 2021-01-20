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
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateRoleRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteRoleRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetRoleListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateRoleResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteRoleResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetRoleListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("role")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateRoleResponse> createRole(@RequestBody ObjectRequest<CreateRoleRequest> request) {
        return new ObjectResponse<>(new CreateRoleResponse());
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetRoleListResponse> listRoles(@RequestBody ObjectRequest<GetRoleListRequest> request) {
        return new ObjectResponse<>(new GetRoleListResponse());
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteRoleResponse> deleteRole(@RequestBody ObjectRequest<DeleteRoleRequest> request) {
        return new ObjectResponse<>(new DeleteRoleResponse());
    }

}
