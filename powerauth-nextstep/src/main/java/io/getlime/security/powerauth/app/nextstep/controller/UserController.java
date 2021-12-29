/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.service.*;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * REST controller for user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("user")
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserIdentityService userIdentityService;
    private final UserIdentityLookupService userIdentityLookupService;
    private final UserRoleService userRoleService;
    private final UserContactService userContactService;
    private final UserAliasService userAliasService;
    private final CredentialService credentialService;
    private final AuthenticationService authenticationService;

    /**
     * REST controller constructor.
     * @param userIdentityService User identity service.
     * @param userIdentityLookupService User identity lookup service.
     * @param userRoleService User role service.
     * @param userContactService User contact service.
     * @param userAliasService User alias service.
     * @param credentialService Credential service.
     * @param authenticationService Authentication service.
     */
    @Autowired
    public UserController(UserIdentityService userIdentityService, UserIdentityLookupService userIdentityLookupService, UserRoleService userRoleService, UserContactService userContactService, UserAliasService userAliasService, CredentialService credentialService, AuthenticationService authenticationService) {
        this.userIdentityService = userIdentityService;
        this.userIdentityLookupService = userIdentityLookupService;
        this.userRoleService = userRoleService;
        this.userContactService = userContactService;
        this.userAliasService = userAliasService;
        this.credentialService = credentialService;
        this.authenticationService = authenticationService;
    }

    /**
     * Create a user identity.
     * @param request Create user request.
     * @return Create user response.
     * @throws UserAlreadyExistsException Thrown when user already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Operation(summary = "Create a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_ALREADY_EXISTS, INVALID_REQUEST, CREDENTIAL_DEFINITION_NOT_FOUND, INVALID_CONFIGURATION, CREDENTIAL_VALIDATION_FAILED, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateUserResponse> createUser(@Valid @RequestBody ObjectRequest<CreateUserRequest> request) throws UserAlreadyExistsException, InvalidRequestException, CredentialDefinitionNotFoundException, InvalidConfigurationException, CredentialValidationFailedException, EncryptionException {
        logger.info("Received createUser request, user ID: {}", request.getRequestObject().getUserId());
        final CreateUserResponse response = userIdentityService.createUserIdentity(request.getRequestObject());
        logger.info("The createUser request succeeded, user ID: {}", request.getRequestObject().getUserId());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a user identity via PUT method.
     * @param request Update user request.
     * @return Update user response.
     * @throws UserNotFoundException Throw when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation is failed.s
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Operation(summary = "Update a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_REQUEST, CREDENTIAL_DEFINITION_NOT_FOUND, INVALID_CONFIGURATION, CREDENTIAL_VALIDATION_FAILED, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserResponse> updateUser(@Valid @RequestBody ObjectRequest<UpdateUserRequest> request) throws UserNotFoundException, InvalidRequestException, CredentialDefinitionNotFoundException, InvalidConfigurationException, CredentialValidationFailedException, EncryptionException {
        logger.info("Received updateUser request, user ID: {}", request.getRequestObject().getUserId());
        final UpdateUserResponse response = userIdentityService.updateUserIdentity(request.getRequestObject());
        logger.info("The updateUser request succeeded, user ID: {}", request.getRequestObject().getUserId());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a user identity via POST method.
     * @param request Update user request.
     * @return Update user response.
     * @throws UserNotFoundException Throw when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation is failed.s
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Operation(summary = "Update a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_REQUEST, CREDENTIAL_DEFINITION_NOT_FOUND, INVALID_CONFIGURATION, CREDENTIAL_VALIDATION_FAILED, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserResponse> updateUserPost(@Valid @RequestBody ObjectRequest<UpdateUserRequest> request) throws UserNotFoundException, InvalidRequestException, CredentialDefinitionNotFoundException, InvalidConfigurationException, CredentialValidationFailedException, EncryptionException {
        logger.info("Received updateUserPost request, user ID: {}", request.getRequestObject().getUserId());
        final UpdateUserResponse response = userIdentityService.updateUserIdentity(request.getRequestObject());
        logger.info("The updateUserPost request succeeded, user ID: {}", request.getRequestObject().getUserId());
        return new ObjectResponse<>(response);
    }

    /**
     * Get user identity detail.
     * @param userId User ID.
     * @param credentialName Credential definition name in case response should only include specific credential.
     * @param includeRemoved Whether removed objects should be included.
     * @return Get user detail response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     */
    @Operation(summary = "Get user identity detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_REQUEST, INVALID_CONFIGURATION, ENCRYPTION_FAILED, CREDENTIAL_DEFINITION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "detail", method = RequestMethod.GET)
    public ObjectResponse<GetUserDetailResponse> getUserDetail(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId, @RequestParam @Nullable @Size(min = 2, max = 256) String credentialName, @RequestParam boolean includeRemoved) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException, CredentialDefinitionNotFoundException {
        GetUserDetailRequest request = new GetUserDetailRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setIncludeRemoved(includeRemoved);
        logger.debug("Received getUserDetail request, user ID: {}", userId);
        final GetUserDetailResponse response = userIdentityService.getUserDetail(request);
        logger.debug("The getUserDetail request succeeded, user ID: {}", userId);
        return new ObjectResponse<>(response);
    }

    /**
     * Get user identity detail using POST method.
     * @param request Get user detail request.
     * @return Get user detail response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     */
    @Operation(summary = "Get user identity detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_REQUEST, INVALID_CONFIGURATION, ENCRYPTION_FAILED, CREDENTIAL_DEFINITION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetUserDetailResponse> getUserDetailPost(@Valid @RequestBody ObjectRequest<GetUserDetailRequest> request) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException, CredentialDefinitionNotFoundException {
        logger.debug("Received getUserDetailPost request, user ID: {}", request.getRequestObject().getUserId());
        final GetUserDetailResponse response = userIdentityService.getUserDetail(request.getRequestObject());
        logger.debug("The getUserDetailPost request succeeded, user ID: {}", request.getRequestObject().getUserId());
        return new ObjectResponse<>(response);
    }

    /**
     * Lookup a user identity.
     * @param request Lookup user request.
     * @return Lookup user response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Operation(summary = "Lookup user identities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_REQUEST, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "lookup", method = RequestMethod.POST)
    public ObjectResponse<LookupUsersResponse> lookupUsers(@Valid @RequestBody ObjectRequest<LookupUsersRequest> request) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException {
        logger.info("Received lookupUsers request");
        final LookupUsersResponse response = userIdentityLookupService.lookupUsers(request.getRequestObject());
        logger.info("The lookupUsers request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Lookup a single user identity.
     * @param request Lookup user request.
     * @return Lookup user response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Operation(summary = "Lookup user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_REQUEST, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "lookup/single", method = RequestMethod.POST)
    public ObjectResponse<LookupUserResponse> lookupSingleUser(@Valid @RequestBody ObjectRequest<LookupUserRequest> request) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, OperationNotFoundException, EncryptionException {
        logger.info("Received lookupSingleUser request, username: {}, credential name: {}", request.getRequestObject().getUsername(), request.getRequestObject().getCredentialName());
        final LookupUserResponse response = userIdentityLookupService.lookupUser(request.getRequestObject());
        logger.info("The lookupSingleUser request, username: {}, credential name: {}, user ID: {}", request.getRequestObject().getUsername(), request.getRequestObject().getCredentialName(), response.getUser().getUserId());
        return new ObjectResponse<>(response);
    }

    /**
     * Update multiple user identity status via PUT method.
     * @param request Update users request.
     * @return Update users response.
     * @throws UserNotFoundException Thrown when no user identity is found.
     */
    @Operation(summary = "Update multiple user identities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identities were updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "multi", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUsersResponse> updateMultipleUsers(@Valid @RequestBody ObjectRequest<UpdateUsersRequest> request) throws UserNotFoundException {
        logger.info("Received updateMultipleUsers request");
        final UpdateUsersResponse response = userIdentityService.updateUsers(request.getRequestObject());
        logger.info("The updateMultipleUsers request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Update multiple user identity status via POST method.
     * @param request Update users request.
     * @return Update users response.
     * @throws UserNotFoundException Thrown when no user identity is found.
     */
    @Operation(summary = "Update multiple user identities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identities were updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "update/multi", method = RequestMethod.POST)
    public ObjectResponse<UpdateUsersResponse> updateMultipleUsersPost(@Valid @RequestBody ObjectRequest<UpdateUsersRequest> request) throws UserNotFoundException {
        logger.info("Received updateMultipleUsersPost request");
        final UpdateUsersResponse response = userIdentityService.updateUsers(request.getRequestObject());
        logger.info("The updateMultipleUsersPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Assign a role to a user identity.
     * @param request Add user role request.
     * @return Add user role response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserRoleAlreadyAssignedException Thrown when user role is already assigned.
     * @throws InvalidRequestException Thrown when request is not found.
     */
    @Operation(summary = "Assign a role to a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role was assigned"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_ROLE_ALREADY_ASSIGNED, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "role", method = RequestMethod.POST)
    public ObjectResponse<AddUserRoleResponse> addRole(@Valid @RequestBody ObjectRequest<AddUserRoleRequest> request) throws UserNotFoundException, UserRoleAlreadyAssignedException, InvalidRequestException {
        logger.info("Received addRole request, user ID: {}, role name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getRoleName());
        final AddUserRoleResponse response = userRoleService.addUserRole(request.getRequestObject());
        logger.info("The addRole request succeeded, user ID: {}, role name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getRoleName());
        return new ObjectResponse<>(response);
    }

    /**
     * Remove a user role from a user identity.
     * @param request Remove user role request.
     * @return Remove user role response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserRoleNotAssignedException Thrown when user role is not assigned.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Remove a role from a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role was removed"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_ROLE_NOT_ASSIGNED, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "role/remove", method = RequestMethod.POST)
    public ObjectResponse<RemoveUserRoleResponse> removeRole(@Valid @RequestBody ObjectRequest<RemoveUserRoleRequest> request) throws UserNotFoundException, UserRoleNotAssignedException, InvalidRequestException {
        logger.info("Received removeRole request, user ID: {}, role name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getRoleName());
        final RemoveUserRoleResponse response = userRoleService.removeUserRole(request.getRequestObject());
        logger.info("The removeRole request succeeded, user ID: {}, role name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getRoleName());
        return new ObjectResponse<>(response);
    }

    /**
     * Create a user contact.
     * @param request Create user contact request.
     * @return Create user contact response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserContactAlreadyExistsException Thrown when user contact already exists.
     */
    @Operation(summary = "Create a user contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_CONTACT_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "contact", method = RequestMethod.POST)
    public ObjectResponse<CreateUserContactResponse> createUserContact(@Valid @RequestBody ObjectRequest<CreateUserContactRequest> request) throws UserNotFoundException, UserContactAlreadyExistsException {
        logger.info("Received createUserContact request, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        final CreateUserContactResponse response = userContactService.createUserContact(request.getRequestObject());
        logger.info("The createUserContact request succeeded, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get list of contacts for a user identity.
     * @param userId User ID.
     * @return Get user contact list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Operation(summary = "Get user contact list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "contact", method = RequestMethod.GET)
    public ObjectResponse<GetUserContactListResponse> getUserContactList(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId) throws UserNotFoundException {
        logger.info("Received getUserContactList request, user ID: {}", userId);
        GetUserContactListRequest request = new GetUserContactListRequest();
        request.setUserId(userId);
        final GetUserContactListResponse response = userContactService.getUserContactList(request);
        logger.info("The getUserContactList request succeeded, user ID: {}, contact list size: {}", userId, response.getContacts().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get list of contacts for a user identity using POST method.
     * @param request Get user contact list request.
     * @return Get user contact list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Operation(summary = "Get user contact list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "contact/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserContactListResponse> getUserContactListPost(@Valid @RequestBody ObjectRequest<GetUserContactListRequest> request) throws UserNotFoundException {
        logger.info("Received getUserContactListPost request, user ID: {}", request.getRequestObject().getUserId());
        final GetUserContactListResponse response = userContactService.getUserContactList(request.getRequestObject());
        logger.info("The getUserContactListPost request succeeded, user ID: {}, contact list size: {}", request.getRequestObject().getUserId(), response.getContacts().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a user contact via PUT method.
     * @param request Update user contact request.
     * @return Update user contact response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserContactNotFoundException Thrown when user contact is not found.
     */
    @Operation(summary = "Update a user contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_CONTACT_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "contact", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserContactResponse> updateUserContact(@Valid @RequestBody ObjectRequest<UpdateUserContactRequest> request) throws UserNotFoundException, UserContactNotFoundException {
        logger.info("Received updateUserContact request, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        final UpdateUserContactResponse response = userContactService.updateUserContact(request.getRequestObject());
        logger.info("The updateUserContact request succeeded, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a user contact via POST method.
     * @param request Update user contact request.
     * @return Update user contact response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserContactNotFoundException Thrown when user contact is not found.
     */
    @Operation(summary = "Update a user contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_CONTACT_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "contact/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserContactResponse> updateUserContactPost(@Valid @RequestBody ObjectRequest<UpdateUserContactRequest> request) throws UserNotFoundException, UserContactNotFoundException {
        logger.info("Received updateUserContactPost request, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        final UpdateUserContactResponse response = userContactService.updateUserContact(request.getRequestObject());
        logger.info("The updateUserContactPost request succeeded, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a user contact.
     * @param request Delete user contact request.
     * @return Delete user contact response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserContactNotFoundException Thrown when user contact is not found.
     */
    @Operation(summary = "Delete a user contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_CONTACT_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "contact/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserContactResponse> deleteUserContact(@Valid @RequestBody ObjectRequest<DeleteUserContactRequest> request) throws UserNotFoundException, UserContactNotFoundException {
        logger.info("Received deleteUserContact request, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        final DeleteUserContactResponse response = userContactService.deleteUserContact(request.getRequestObject());
        logger.info("The deleteUserContact request succeeded, user ID: {}, contact name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getContactName());
        return new ObjectResponse<>(response);
    }

    /**
     * Create a user alias.
     * @param request Create user alias request.
     * @return Create user alias response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserAliasAlreadyExistsException Thrown when user alias already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Create a user alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_ALIAS_ALREADY_EXISTS, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "alias", method = RequestMethod.POST)
    public ObjectResponse<CreateUserAliasResponse> createUserAlias(@Valid @RequestBody ObjectRequest<CreateUserAliasRequest> request) throws UserNotFoundException, UserAliasAlreadyExistsException, InvalidRequestException {
        logger.info("Received createUserAlias request, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        final CreateUserAliasResponse response = userAliasService.createUserAlias(request.getRequestObject());
        logger.info("The createUserAlias request succeeded, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get alias list for a user identity.
     * @param userId User ID.
     * @param includeRemoved Whether removed aliases should be included.
     * @return Get user alias list response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Operation(summary = "Get user alias list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User alias list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "alias", method = RequestMethod.GET)
    public ObjectResponse<GetUserAliasListResponse> getUserAliasList(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId, @RequestParam boolean includeRemoved) throws InvalidRequestException, UserNotFoundException {
        logger.info("Received getUserAliasList request, user ID: {}", userId);
        GetUserAliasListRequest request = new GetUserAliasListRequest();
        request.setUserId(userId);
        request.setIncludeRemoved(includeRemoved);
        final GetUserAliasListResponse response = userAliasService.getUserAliasList(request);
        logger.info("The getUserAliasList request succeeded, user ID: {}, alias list size: {}", userId, response.getAliases().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get alias list for a user identity using POST method.
     * @param request Get user alias list request.
     * @return Get user alias list response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Operation(summary = "Get user alias list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User alias list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "alias/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAliasListResponse> getUserAliasListPost(@Valid @RequestBody ObjectRequest<GetUserAliasListRequest> request) throws InvalidRequestException, UserNotFoundException {
        logger.info("Received getUserAliasListPost request, user ID: {}", request.getRequestObject().getUserId());
        final GetUserAliasListResponse response = userAliasService.getUserAliasList(request.getRequestObject());
        logger.info("The getUserAliasListPost request succeeded, user ID: {}, alias list size: {}", request.getRequestObject().getUserId(), response.getAliases().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a user alias via PUT method.
     * @param request Update user alias request.
     * @return Update user alias response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserAliasNotFoundException Thrown when user alias is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update a user alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User alias was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_ALIAS_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "alias", method = RequestMethod.PUT)
    public ObjectResponse<UpdateUserAliasResponse> updateUserAlias(@Valid @RequestBody ObjectRequest<UpdateUserAliasRequest> request) throws UserNotFoundException, UserAliasNotFoundException, InvalidRequestException {
        logger.info("Received updateUserAlias request, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        final UpdateUserAliasResponse response = userAliasService.updateUserAlias(request.getRequestObject());
        logger.info("The updateUserAlias request succeeded, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a user alias via POST method.
     * @param request Update user alias request.
     * @return Update user alias response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserAliasNotFoundException Thrown when user alias is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update a user alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User alias was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_ALIAS_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "alias/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateUserAliasResponse> updateUserAliasPost(@Valid @RequestBody ObjectRequest<UpdateUserAliasRequest> request) throws UserNotFoundException, UserAliasNotFoundException, InvalidRequestException {
        logger.info("Received updateUserAliasPost request, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        final UpdateUserAliasResponse response = userAliasService.updateUserAlias(request.getRequestObject());
        logger.info("The updateUserAliasPost request succeeded, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a user alias.
     * @param request Delete user alias request.
     * @return Delete user alias response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserAliasNotFoundException Thrown when user alias is not found.
     */
    @Operation(summary = "Delete a user alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User alias was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_ALIAS_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "alias/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserAliasResponse> deleteUserAlias(@Valid @RequestBody ObjectRequest<DeleteUserAliasRequest> request) throws UserNotFoundException, UserAliasNotFoundException {
        logger.info("Received deleteUserAlias request, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        final DeleteUserAliasResponse response = userAliasService.deleteUserAlias(request.getRequestObject());
        logger.info("The deleteUserAlias request succeeded, user ID: {}, alias name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAliasName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential list for a user identity.
     * @param userId User ID.
     * @param includeRemoved Whether removed credentials should be included.
     * @return Get user credential list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Operation(summary = "Get user credential list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User credential list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "credential", method = RequestMethod.GET)
    public ObjectResponse<GetUserCredentialListResponse> getUserCredentialList(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId, @RequestParam boolean includeRemoved) throws UserNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received getUserCredentialList request, user ID: {}", userId);
        GetUserCredentialListRequest request = new GetUserCredentialListRequest();
        request.setUserId(userId);
        request.setIncludeRemoved(includeRemoved);
        final GetUserCredentialListResponse response = credentialService.getCredentialList(request);
        logger.info("The getUserCredentialList request succeeded, user ID: {}, credential list size: {}", userId, response.getCredentials().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential list for a user identity using POST method.
     * @param request Get user credential list request.
     * @return Get user credential list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Operation(summary = "Get user credential list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User credential list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "credential/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserCredentialListResponse> getUserCredentialListPost(@Valid @RequestBody ObjectRequest<GetUserCredentialListRequest> request) throws UserNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received getUserCredentialListPost request, user ID: {}", request.getRequestObject().getUserId());
        final GetUserCredentialListResponse response = credentialService.getCredentialList(request.getRequestObject());
        logger.info("The getUserCredentialListPost request succeeded, user ID: {}, credential list size: {}", request.getRequestObject().getUserId(), response.getCredentials().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get authentication list for a user identity using.
     * @param userId User ID.
     * @param createdStartDate Credential start date.
     * @param createdEndDate Credential end date.
     * @return Get user authentication list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Operation(summary = "Get user authentication list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authentication list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "authentication", method = RequestMethod.GET)
    public ObjectResponse<GetUserAuthenticationListResponse> getUserAuthenticationList(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId, @RequestParam @Nullable Date createdStartDate, @RequestParam @Nullable Date createdEndDate) throws UserNotFoundException {
        logger.info("Received getUserAuthenticationList request, user ID: {}", userId);
        GetUserAuthenticationListRequest request = new GetUserAuthenticationListRequest();
        request.setUserId(userId);
        request.setCreatedStartDate(createdStartDate);
        request.setCreatedEndDate(createdEndDate);
        final GetUserAuthenticationListResponse response = authenticationService.getUserAuthenticationList(request);
        logger.info("The getUserAuthenticationList request succeeded, user ID: {}, authentication list size: {}", userId, response.getAuthentications().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get authentication list for a user identity using POST method.
     * @param request Get user authentication list request.
     * @return Get user authentication list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Operation(summary = "Get user authentication list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authentication list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "authentication/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthenticationListResponse> getUserAuthenticationListPost(@Valid @RequestBody ObjectRequest<GetUserAuthenticationListRequest> request) throws UserNotFoundException {
        logger.info("Received getUserAuthenticationListPost request, user ID: {}", request.getRequestObject().getUserId());
        final GetUserAuthenticationListResponse response = authenticationService.getUserAuthenticationList(request.getRequestObject());
        logger.info("The getUserAuthenticationListPost request succeeded, user ID: {}, authentication list size: {}", request.getRequestObject().getUserId(), response.getAuthentications().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a user identity.
     * @param request Delete user identity request.
     * @return Delete user identity response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Operation(summary = "Delete a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteUserResponse> deleteUser(@Valid @RequestBody ObjectRequest<DeleteUserRequest> request) throws UserNotFoundException {
        logger.info("Received deleteUser request, user ID: {}", request.getRequestObject().getUserId());
        final DeleteUserResponse response = userIdentityService.deleteUser(request.getRequestObject());
        logger.info("The deleteUser request succeeded, user ID: {}", request.getRequestObject().getUserId());
        return new ObjectResponse<>(response);
    }

    /**
     * Block a user identity.
     * @param request Block user identity request.
     * @return Block user identity response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserNotActiveException Thrown when user identity is not active.
     */
    @Operation(summary = "Block a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity was blocked"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_IDENTITY_NOT_ACTIVE"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "block", method = RequestMethod.POST)
    public ObjectResponse<BlockUserResponse> blockUser(@Valid @RequestBody ObjectRequest<BlockUserRequest> request) throws UserNotFoundException, UserNotActiveException {
        logger.info("Received blockUser request, user ID: {}", request.getRequestObject().getUserId());
        final BlockUserResponse response = userIdentityService.blockUser(request.getRequestObject());
        logger.info("The blockUser request succeeded, user ID: {}", request.getRequestObject().getUserId());
        return new ObjectResponse<>(response);
    }

    /**
     * Unblock a user identity.
     * @param request Unblock user identity request.
     * @return Unblock user identity response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserNotBlockedException Thrown when user identity is not blocked.
     */
    @Operation(summary = "Unblock a user identity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User identity was unblocked"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, USER_IDENTITY_NOT_BLOCKED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "unblock", method = RequestMethod.POST)
    public ObjectResponse<UnblockUserResponse> unblockUser(@Valid @RequestBody ObjectRequest<UnblockUserRequest> request) throws UserNotFoundException, UserNotBlockedException {
        logger.info("Received unblockUser request, user ID: {}", request.getRequestObject().getUserId());
        final UnblockUserResponse response = userIdentityService.unblockUser(request.getRequestObject());
        logger.info("The unblockUser request succeeded, user ID: {}", request.getRequestObject().getUserId());
        return new ObjectResponse<>(response);
    }

}
