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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.ExtrasConverter;
import io.getlime.security.powerauth.app.nextstep.converter.UserAliasConverter;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserAliasEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This service handles persistence of user aliases.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserAliasService {

    private final Logger logger = LoggerFactory.getLogger(UserAliasService.class);
    private static final String AUDIT_TYPE_USER_IDENTITY = "USER_IDENTITY";

    private final UserIdentityRepository userIdentityRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final Audit audit;

    private final UserAliasConverter userAliasConverter = new UserAliasConverter();
    private final ExtrasConverter extrasConverter = new ExtrasConverter();

    /**
     * User alias service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public UserAliasService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, Audit audit) {
        this.serviceCatalogue = serviceCatalogue;
        this.userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();
        this.audit = audit;
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final Optional<UserAliasEntity> aliasOptional = user.getAliases().stream().filter(alias -> alias.getName().equals(request.getAliasName())).findFirst();
        final UserAliasEntity alias;
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
            user.getAliases().add(alias);
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
        user = userIdentityRepository.save(user);
        logger.debug("User alias was created, user ID: {}, alias name: {}", user.getUserId(), alias.getName());
        audit.info("User alias was created", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("aliasName", alias.getName())
                .build());
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final Set<UserAliasEntity> aliases;
        if (request.isIncludeRemoved()) {
            aliases = user.getAliases();
        } else {
            aliases = user.getAliases().stream().filter(alias -> alias.getStatus() == UserAliasStatus.ACTIVE).collect(Collectors.toSet());
        }
        final  GetUserAliasListResponse response = new GetUserAliasListResponse();
        response.setUserId(user.getUserId());
        for (UserAliasEntity alias : aliases) {
            try {
                final UserAliasDetail aliasDetail = userAliasConverter.fromEntity(alias);
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final Optional<UserAliasEntity> aliasOptional = user.getAliases().stream().filter(alias -> alias.getName().equals(request.getAliasName())).findFirst();
        final UserAliasEntity alias;
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
                final String extras = extrasConverter.fromMap(request.getExtras());
                alias.setExtras(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        user = userIdentityRepository.save(user);
        logger.debug("User alias was updated, user ID: {}, alias name: {}", user.getUserId(), alias.getName());
        audit.info("User alias was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("aliasName", alias.getName())
                .build());
        final UpdateUserAliasResponse response = new UpdateUserAliasResponse();
        response.setUserId(user.getUserId());
        response.setAliasName(alias.getName());
        response.setAliasValue(alias.getValue());
        response.setUserAliasStatus(alias.getStatus());
        if (request.getExtras() != null) {
            response.getExtras().putAll(request.getExtras());
        }
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final Optional<UserAliasEntity> aliasOptional = user.getAliases().stream().filter(alias -> alias.getName().equals(request.getAliasName())).findFirst();
        final UserAliasEntity alias;
        if (!aliasOptional.isPresent()) {
            throw new UserAliasNotFoundException("User alias not found: " + request.getAliasName() + ", user ID: " + request.getUserId());
        } else {
            alias = aliasOptional.get();
            if (alias.getStatus() == UserAliasStatus.REMOVED) {
                throw new UserAliasNotFoundException("User alias is already REMOVED: " + request.getAliasName());
            }
        }
        alias.setStatus(UserAliasStatus.REMOVED);
        alias.setTimestampLastUpdated(new Date());
        user = userIdentityRepository.save(user);
        logger.debug("User alias was removed, user ID: {}, alias name: {}", user.getUserId(), alias.getName());
        audit.info("User alias was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("aliasName", alias.getName())
                .build());
        final DeleteUserAliasResponse response = new DeleteUserAliasResponse();
        response.setUserId(user.getUserId());
        response.setAliasName(alias.getName());
        response.setUserAliasStatus(alias.getStatus());
        return response;
    }

}