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
import io.getlime.security.powerauth.app.nextstep.service.UserIdentityService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserIdentityService userIdentityService;

    @Autowired
    public UserController(UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateUserResponse> createUser(@RequestBody ObjectRequest<CreateUserRequest> request) throws UserAlreadyExistsException, InvalidConfigurationException, InvalidRequestException {
        CreateUserResponse response = userIdentityService.createUserIdentity(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserResponse> updateUser(@RequestBody ObjectRequest<UpdateUserRequest> request) {
        return new ObjectResponse<>(new UpdateUserResponse());
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserResponse> updateUserPost(@RequestBody ObjectRequest<UpdateUserRequest> request) {
        return new ObjectResponse<>(new UpdateUserResponse());
    }

    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetUserDetailResponse> getUserDetail(@RequestBody ObjectRequest<GetUserDetailRequest> request) {
        return new ObjectResponse<>(new GetUserDetailResponse());
    }

    @RequestMapping(value = "lookup", method = RequestMethod.POST)
    public ObjectResponse<LookupUserResponse> lookupUser(@RequestBody ObjectRequest<LookupUserRequest> request) {
        return new ObjectResponse<>(new LookupUserResponse());
    }

    @RequestMapping(value = "multi", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUsersResponse> updateMultipleUsers(@RequestBody ObjectRequest<UpdateUsersRequest> request) {
        return new ObjectResponse<>(new UpdateUsersResponse());
    }

    @RequestMapping(value = "update/multi", method = RequestMethod.POST)
    public ObjectResponse<UpdateUsersResponse> updateMultipleUsersPost(@RequestBody ObjectRequest<UpdateUsersRequest> request) {
        return new ObjectResponse<>(new UpdateUsersResponse());
    }

    @RequestMapping(value = "role", method = RequestMethod.POST)
    public ObjectResponse<AddUserRoleResponse> assignRole(@RequestBody ObjectRequest<AddUserRoleRequest> request) {
        return new ObjectResponse<>(new AddUserRoleResponse());
    }

    @RequestMapping(value = "role/remove", method = RequestMethod.POST)
    public ObjectResponse<RemoveUserRoleResponse> removeRole(@RequestBody ObjectRequest<RemoveUserRoleRequest> request) {
        return new ObjectResponse<>(new RemoveUserRoleResponse());
    }

    @RequestMapping(value = "contact", method = RequestMethod.POST)
    public ObjectResponse<CreateUserContactResponse> createUserContact(@RequestBody ObjectRequest<CreateUserContactRequest> request) {
        return new ObjectResponse<>(new CreateUserContactResponse());
    }

    @RequestMapping(value = "contact/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserContactListResponse> getUserContactList(@RequestBody ObjectRequest<GetUserContactListRequest> request) {
        return new ObjectResponse<>(new GetUserContactListResponse());
    }

    @RequestMapping(value = "contact", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserContactResponse> updateUserContact(@RequestBody ObjectRequest<UpdateUserContactRequest> request) {
        return new ObjectResponse<>(new UpdateUserContactResponse());
    }

    @RequestMapping(value = "contact/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserContactResponse> updateUserContactPost(@RequestBody ObjectRequest<UpdateUserContactRequest> request) {
        return new ObjectResponse<>(new UpdateUserContactResponse());
    }

    @RequestMapping(value = "contact/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserContactResponse> deleteUserContact(@RequestBody ObjectRequest<DeleteUserContactRequest> request) {
        return new ObjectResponse<>(new DeleteUserContactResponse());
    }

    @RequestMapping(value = "alias", method = RequestMethod.POST)
    public ObjectResponse<CreateUserAliasResponse> createUserAlias(@RequestBody ObjectRequest<CreateUserAliasRequest> request) {
        return new ObjectResponse<>(new CreateUserAliasResponse());
    }

    @RequestMapping(value = "alias/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAliasListResponse> getUserAliasList(@RequestBody ObjectRequest<GetUserAliasListRequest> request) {
        return new ObjectResponse<>(new GetUserAliasListResponse());
    }

    @RequestMapping(value = "alias", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserAliasResponse> updateUserAlias(@RequestBody ObjectRequest<UpdateUserAliasRequest> request) {
        return new ObjectResponse<>(new UpdateUserAliasResponse());
    }

    @RequestMapping(value = "alias/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserAliasResponse> updateUserAliasPost(@RequestBody ObjectRequest<UpdateUserAliasRequest> request) {
        return new ObjectResponse<>(new UpdateUserAliasResponse());
    }

    @RequestMapping(value = "alias/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserAliasResponse> deleteUserAlias(@RequestBody ObjectRequest<DeleteUserAliasRequest> request) {
        return new ObjectResponse<>(new DeleteUserAliasResponse());
    }

    @RequestMapping(value = "credential/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserCredentialListResponse> getUserCredentialList(@RequestBody ObjectRequest<GetUserCredentialListRequest> request) {
        return new ObjectResponse<>(new GetUserCredentialListResponse());
    }

    @RequestMapping(value = "authentication/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthenticationListResponse> getUserAuthenticationList(@RequestBody ObjectRequest<GetUserAuthenticationListRequest> request) {
        return new ObjectResponse<>(new GetUserAuthenticationListResponse());
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserResponse> deleteUser(@RequestBody ObjectRequest<DeleteUserRequest> request) {
        return new ObjectResponse<>(new DeleteUserResponse());
    }

    @RequestMapping(value = "block", method = RequestMethod.POST)
    public ObjectResponse<BlockUserResponse> blockUser(@RequestBody ObjectRequest<BlockUserRequest> request) {
        return new ObjectResponse<>(new BlockUserResponse());
    }

    @RequestMapping(value = "unblock", method = RequestMethod.POST)
    public ObjectResponse<UnblockUserResponse> unblockUser(@RequestBody ObjectRequest<UnblockUserRequest> request) {
        return new ObjectResponse<>(new UnblockUserResponse());
    }

}
