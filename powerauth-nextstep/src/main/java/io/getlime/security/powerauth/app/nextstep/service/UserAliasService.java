/*
 * Copyright 2012 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.converter.ExtrasConverter;
import io.getlime.security.powerauth.app.nextstep.converter.UserAliasConverter;
import io.getlime.security.powerauth.app.nextstep.repository.UserAliasRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserAliasEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAliasDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAliasStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserAliasAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserAliasNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateUserAliasRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteUserAliasRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetUserAliasListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateUserAliasRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateUserAliasResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteUserAliasResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserAliasListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateUserAliasResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * This service handles persistence of user aliases.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserAliasService {

    private final Logger logger = LoggerFactory.getLogger(UserAliasService.class);

    private final UserIdentityLookupService userIdentityLookupService;
    private final UserAliasRepository userAliasRepository;

    private final UserAliasConverter userAliasConverter = new UserAliasConverter();
    private final ExtrasConverter extrasConverter = new ExtrasConverter();

    /**
     * User alias service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param userAliasRepository User alias repository.
     */
    @Autowired
    public UserAliasService(UserIdentityLookupService userIdentityLookupService, UserAliasRepository userAliasRepository) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.userAliasRepository = userAliasRepository;
    }

    /**
     * Create a user alias.
     * @param request Create user alias request.
     * @return Create user alias response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserAliasAlreadyExistsException Thrown when user alias already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public CreateUserAliasResponse createUserAlias(CreateUserAliasRequest request) throws UserNotFoundException, UserAliasAlreadyExistsException, InvalidRequestException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<UserAliasEntity> aliasOptional = userAliasRepository.findByUserAndName(user, request.getAliasName());
        UserAliasEntity alias;
        if (aliasOptional.isPresent()) {
            alias = aliasOptional.get();
            if (alias.getStatus() == UserAliasStatus.ACTIVE) {
                throw new UserAliasAlreadyExistsException("User alias already exists: " + request.getAliasName() + ", user ID: " + user.getUserId());
            }
            alias.setTimestampLastUpdated(new Date());
        } else {
            alias = new UserAliasEntity();
            alias.setUser(user);
            alias.setName(request.getAliasName());
            alias.setTimestampCreated(new Date());
        }
        alias.setValue(request.getAliasValue());
        alias.setStatus(UserAliasStatus.ACTIVE);
        if (request.getExtras() != null) {
            try {
                String extras = extrasConverter.fromMap(request.getExtras());
                alias.setExtras(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        userAliasRepository.save(alias);
        CreateUserAliasResponse response = new CreateUserAliasResponse();
        response.setUserId(user.getUserId());
        response.setAliasName(alias.getName());
        response.setAliasValue(alias.getValue());
        response.setUserAliasStatus(alias.getStatus());
        response.getExtras().putAll(request.getExtras());
        return response;
    }

    /**
     * Get alias list for a user identity.
     * @param request Get user alias list request.
     * @return Get user alias list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public GetUserAliasListResponse getUserAliasList(GetUserAliasListRequest request) throws UserNotFoundException, InvalidRequestException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        List<UserAliasEntity> aliases;
        if (request.isIncludeRemoved()) {
            aliases = userAliasRepository.findAllByUser(user);
        } else {
            aliases = userAliasRepository.findAllByUserAndStatus(user, UserAliasStatus.ACTIVE);
        }
        GetUserAliasListResponse response = new GetUserAliasListResponse();
        response.setUserId(user.getUserId());
        for (UserAliasEntity alias : aliases) {
            try {
                UserAliasDetail aliasDetail = userAliasConverter.fromEntity(alias);
                response.getAliases().add(aliasDetail);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        return response;
    }

    /**
     * Update alias for a user identity.
     * @param request Update user alias request.
     * @return Update user alias response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserAliasNotFoundException Thrown when user alias is not found.
     */
    @Transactional
    public UpdateUserAliasResponse updateUserAlias(UpdateUserAliasRequest request) throws UserNotFoundException, InvalidRequestException, UserAliasNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<UserAliasEntity> aliasOptional = userAliasRepository.findByUserAndName(user, request.getAliasName());
        UserAliasEntity alias;
        if (!aliasOptional.isPresent()) {
            throw new UserAliasNotFoundException("User alias not found: " + request.getAliasName() + ", user ID: " + request.getUserId());
        } else {
            alias = aliasOptional.get();
            if (alias.getStatus() == UserAliasStatus.REMOVED && request.getUserAliasStatus() != UserAliasStatus.ACTIVE) {
                throw new UserAliasNotFoundException("User alias is REMOVED: " + request.getAliasName() + ", user ID: " + user.getUserId());
            }
        }
        alias.setTimestampLastUpdated(new Date());
        alias.setValue(request.getAliasValue());
        if (request.getUserAliasStatus() != null) {
            alias.setStatus(request.getUserAliasStatus());
        }
        if (request.getExtras() != null) {
            try {
                String extras = extrasConverter.fromMap(request.getExtras());
                alias.setExtras(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        userAliasRepository.save(alias);
        UpdateUserAliasResponse response = new UpdateUserAliasResponse();
        response.setUserId(user.getUserId());
        response.setAliasName(alias.getName());
        response.setAliasValue(alias.getValue());
        response.setUserAliasStatus(alias.getStatus());
        response.getExtras().putAll(request.getExtras());
        return response;
    }

    /**
     * Delete user alias.
     * @param request Delete user alias request.
     * @return Delete user alias response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserAliasNotFoundException Thrown when user alias is not found.
     */
    @Transactional
    public DeleteUserAliasResponse deleteUserAlias(DeleteUserAliasRequest request) throws UserNotFoundException, UserAliasNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<UserAliasEntity> aliasOptional = userAliasRepository.findByUserAndName(user, request.getAliasName());
        UserAliasEntity alias;
        if (!aliasOptional.isPresent()) {
            throw new UserAliasNotFoundException("User alias not found: " + request.getAliasName() + ", user ID: " + request.getUserId());
        } else {
            alias = aliasOptional.get();
            if (alias.getStatus() == UserAliasStatus.REMOVED) {
                throw new UserAliasNotFoundException("User alias is already REMOVED: " + request.getAliasName());
            }
        }
        alias.setStatus(UserAliasStatus.REMOVED);
        userAliasRepository.save(alias);
        DeleteUserAliasResponse response = new DeleteUserAliasResponse();
        response.setUserId(user.getUserId());
        response.setAliasName(alias.getName());
        response.setUserAliasStatus(alias.getStatus());
        return response;
    }

}