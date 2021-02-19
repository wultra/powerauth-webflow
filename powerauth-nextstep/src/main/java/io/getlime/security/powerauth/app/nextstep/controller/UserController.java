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
import io.getlime.security.powerauth.app.nextstep.service.UserAliasService;
import io.getlime.security.powerauth.app.nextstep.service.UserContactService;
import io.getlime.security.powerauth.app.nextstep.service.UserIdentityService;
import io.getlime.security.powerauth.app.nextstep.service.UserRoleService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
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
    private final UserRoleService userRoleService;
    private final UserContactService userContactService;
    private final UserAliasService userAliasService;

    @Autowired
    public UserController(UserIdentityService userIdentityService, UserRoleService userRoleService, UserContactService userContactService, UserAliasService userAliasService) {
        this.userIdentityService = userIdentityService;
        this.userRoleService = userRoleService;
        this.userContactService = userContactService;
        this.userAliasService = userAliasService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateUserResponse> createUser(@RequestBody ObjectRequest<CreateUserRequest> request) throws UserAlreadyExistsException, InvalidRequestException, CredentialDefinitionNotFoundException {
        // TODO - request validation
        CreateUserResponse response = userIdentityService.createUserIdentity(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserResponse> updateUser(@RequestBody ObjectRequest<UpdateUserRequest> request) throws UserNotFoundException, InvalidRequestException, CredentialDefinitionNotFoundException {
        // TODO - request validation
        UpdateUserResponse response = userIdentityService.updateUserIdentity(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserResponse> updateUserPost(@RequestBody ObjectRequest<UpdateUserRequest> request) throws UserNotFoundException, InvalidRequestException, CredentialDefinitionNotFoundException {
        // TODO - request validation
        UpdateUserResponse response = userIdentityService.updateUserIdentity(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetUserDetailResponse> getUserDetail(@RequestBody ObjectRequest<GetUserDetailRequest> request) throws UserNotFoundException, InvalidRequestException {
        // TODO - request validation
        GetUserDetailResponse response = userIdentityService.getUserDetail(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "lookup", method = RequestMethod.POST)
    public ObjectResponse<LookupUserResponse> lookupUser(@RequestBody ObjectRequest<LookupUserRequest> request) throws UserNotFoundException, InvalidRequestException {
        // TODO - request validation
        LookupUserResponse response = userIdentityService.lookupUser(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "multi", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUsersResponse> updateMultipleUsers(@RequestBody ObjectRequest<UpdateUsersRequest> request) throws UserNotFoundException {
        // TODO - request validation
        UpdateUsersResponse response = userIdentityService.updateUsers(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "update/multi", method = RequestMethod.POST)
    public ObjectResponse<UpdateUsersResponse> updateMultipleUsersPost(@RequestBody ObjectRequest<UpdateUsersRequest> request) throws UserNotFoundException {
        // TODO - request validation
        UpdateUsersResponse response = userIdentityService.updateUsers(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "role", method = RequestMethod.POST)
    public ObjectResponse<AddUserRoleResponse> addRole(@RequestBody ObjectRequest<AddUserRoleRequest> request) throws UserNotFoundException, InvalidRequestException, UserRoleAlreadyAssignedException {
        // TODO - request validation
        AddUserRoleResponse response = userRoleService.addUserRole(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "role/remove", method = RequestMethod.POST)
    public ObjectResponse<RemoveUserRoleResponse> removeRole(@RequestBody ObjectRequest<RemoveUserRoleRequest> request) throws UserNotFoundException, UserRoleNotAssignedException, InvalidRequestException {
        // TODO - request validation
        RemoveUserRoleResponse response = userRoleService.removeUserRole(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "contact", method = RequestMethod.POST)
    public ObjectResponse<CreateUserContactResponse> createUserContact(@RequestBody ObjectRequest<CreateUserContactRequest> request) throws UserNotFoundException, UserContactAlreadyExistsException {
        // TODO - request validation
        CreateUserContactResponse response = userContactService.createUserContact(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "contact/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserContactListResponse> getUserContactList(@RequestBody ObjectRequest<GetUserContactListRequest> request) throws UserNotFoundException {
        // TODO - request validation
        GetUserContactListResponse response = userContactService.getUserContactList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "contact", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserContactResponse> updateUserContact(@RequestBody ObjectRequest<UpdateUserContactRequest> request) throws UserNotFoundException, UserContactNotFoundException {
        // TODO - request validation
        UpdateUserContactResponse response = userContactService.updateUserContact(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "contact/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserContactResponse> updateUserContactPost(@RequestBody ObjectRequest<UpdateUserContactRequest> request) throws UserNotFoundException, UserContactNotFoundException {
        // TODO - request validation
        UpdateUserContactResponse response = userContactService.updateUserContact(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "contact/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserContactResponse> deleteUserContact(@RequestBody ObjectRequest<DeleteUserContactRequest> request) throws UserNotFoundException, UserContactNotFoundException {
        // TODO - request validation
        DeleteUserContactResponse response = userContactService.deleteUserContact(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "alias", method = RequestMethod.POST)
    public ObjectResponse<CreateUserAliasResponse> createUserAlias(@RequestBody ObjectRequest<CreateUserAliasRequest> request) throws UserNotFoundException, UserAliasAlreadyExistsException, InvalidRequestException {
        // TODO - request validation
        CreateUserAliasResponse response = userAliasService.createUserAlias(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "alias/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAliasListResponse> getUserAliasList(@RequestBody ObjectRequest<GetUserAliasListRequest> request) throws InvalidRequestException, UserNotFoundException {
        // TODO - request validation
        GetUserAliasListResponse response = userAliasService.getUserAliasList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "alias", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserAliasResponse> updateUserAlias(@RequestBody ObjectRequest<UpdateUserAliasRequest> request) throws UserNotFoundException, UserAliasNotFoundException, InvalidRequestException {
        // TODO - request validation
        UpdateUserAliasResponse response = userAliasService.updateUserAlias(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "alias/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserAliasResponse> updateUserAliasPost(@RequestBody ObjectRequest<UpdateUserAliasRequest> request) throws UserNotFoundException, UserAliasNotFoundException, InvalidRequestException {
        // TODO - request validation
        UpdateUserAliasResponse response = userAliasService.updateUserAlias(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "alias/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserAliasResponse> deleteUserAlias(@RequestBody ObjectRequest<DeleteUserAliasRequest> request) throws UserNotFoundException, UserAliasNotFoundException {
        // TODO - request validation
        DeleteUserAliasResponse response = userAliasService.deleteUserAlias(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "credential/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserCredentialListResponse> getUserCredentialList(@RequestBody ObjectRequest<GetUserCredentialListRequest> request) throws UserNotFoundException {
        // TODO - request validation
        GetUserCredentialListResponse response = userIdentityService.getCredentialList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "authentication/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthenticationListResponse> getUserAuthenticationList(@RequestBody ObjectRequest<GetUserAuthenticationListRequest> request) {
        // TODO - implement method
        return new ObjectResponse<>(new GetUserAuthenticationListResponse());
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserResponse> deleteUser(@RequestBody ObjectRequest<DeleteUserRequest> request) throws UserNotFoundException {
        // TODO - request validation
        DeleteUserResponse response = userIdentityService.deleteUser(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "block", method = RequestMethod.POST)
    public ObjectResponse<BlockUserResponse> blockUser(@RequestBody ObjectRequest<BlockUserRequest> request) throws UserNotFoundException {
        // TODO - request validation
        BlockUserResponse response = userIdentityService.blockUser(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "unblock", method = RequestMethod.POST)
    public ObjectResponse<UnblockUserResponse> unblockUser(@RequestBody ObjectRequest<UnblockUserRequest> request) throws UserNotFoundException, InvalidRequestException {
        // TODO - request validation
        UnblockUserResponse response = userIdentityService.unblockUser(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
