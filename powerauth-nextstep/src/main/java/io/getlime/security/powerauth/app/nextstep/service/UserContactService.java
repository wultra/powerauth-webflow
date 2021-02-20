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

import io.getlime.security.powerauth.app.nextstep.converter.UserContactConverter;
import io.getlime.security.powerauth.app.nextstep.repository.UserContactRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserContactEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserContactDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserContactAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserContactNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateUserContactRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteUserContactRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetUserContactListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateUserContactRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateUserContactResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteUserContactResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserContactListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateUserContactResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * This service handles persistence of user contacts.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserContactService {

    private final Logger logger = LoggerFactory.getLogger(UserContactService.class);

    private final UserIdentityLookupService userIdentityLookupService;
    private final UserContactRepository userContactRepository;

    private final UserContactConverter userContactConverter = new UserContactConverter();

    /**
     * User contact service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param userContactRepository User contact repository.
     */
    @Autowired
    public UserContactService(UserIdentityLookupService userIdentityLookupService, UserContactRepository userContactRepository) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.userContactRepository = userContactRepository;
    }

    /**
     * Create contact for a user identity.
     * @param request Create user contact request.
     * @return Create user contact response
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserContactAlreadyExistsException Thrown when user contact already exists.
     */
    @Transactional
    public CreateUserContactResponse createUserContact(CreateUserContactRequest request) throws UserNotFoundException, UserContactAlreadyExistsException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<UserContactEntity> contactOptional = userContactRepository.findByUserIdAndName(user, request.getContactName());
        if (contactOptional.isPresent()) {
            throw new UserContactAlreadyExistsException("User contact already exists: " + request.getContactName() + ", user ID: " + user.getUserId());
        }
        UserContactEntity contact = new UserContactEntity();
        contact.setUserId(user);
        contact.setName(request.getContactName());
        contact.setType(request.getContactType());
        contact.setValue(request.getContactValue());
        contact.setPrimary(request.isPrimary());
        contact.setTimestampCreated(new Date());
        userContactRepository.save(contact);
        CreateUserContactResponse response = new CreateUserContactResponse();
        response.setUserId(user.getUserId());
        response.setContactName(contact.getName());
        response.setContactType(contact.getType());
        response.setContactValue(contact.getValue());
        response.setPrimary(contact.isPrimary());
        return response;
    }

    /**
     * Get contacts for user identity.
     * @param request Get user contact list request.
     * @return Get user contact list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Transactional
    public GetUserContactListResponse getUserContactList(GetUserContactListRequest request) throws UserNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        List<UserContactEntity> contacts = userContactRepository.findAllByUserId(user);
        GetUserContactListResponse response = new GetUserContactListResponse();
        response.setUserId(user.getUserId());
        for (UserContactEntity contact : contacts) {
            UserContactDetail contactDetail = userContactConverter.fromEntity(contact);
            response.getContacts().add(contactDetail);
        }
        return response;
    }

    /**
     * Update contact for a user identity.
     * @param request Update user contact request.
     * @return Update user contact response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserContactNotFoundException Thrown when user contact is not found.
     */
    @Transactional
    public UpdateUserContactResponse updateUserContact(UpdateUserContactRequest request) throws UserNotFoundException, UserContactNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<UserContactEntity> contactOptional = userContactRepository.findByUserIdAndName(user, request.getContactName());
        if (!contactOptional.isPresent()) {
            throw new UserContactNotFoundException("User contact not found: " + request.getContactName() + ", user ID: " + user.getUserId());
        }
        UserContactEntity contact = contactOptional.get();
        contact.setUserId(user);
        contact.setName(request.getContactName());
        contact.setType(request.getContactType());
        contact.setValue(request.getContactValue());
        contact.setPrimary(request.isPrimary());
        contact.setTimestampCreated(new Date());
        userContactRepository.save(contact);
        UpdateUserContactResponse response = new UpdateUserContactResponse();
        response.setUserId(user.getUserId());
        response.setContactName(contact.getName());
        response.setContactType(contact.getType());
        response.setContactValue(contact.getValue());
        response.setPrimary(contact.isPrimary());
        return response;
    }

    /**
     * Delete contact for a user identity.
     * @param request Delete user contact request.
     * @return Delete user contact response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserContactNotFoundException Thrown when user contact is not found.
     */
    @Transactional
    public DeleteUserContactResponse deleteUserContact(DeleteUserContactRequest request) throws UserNotFoundException, UserContactNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<UserContactEntity> contactOptional = userContactRepository.findByUserIdAndName(user, request.getContactName());
        if (!contactOptional.isPresent()) {
            throw new UserContactNotFoundException("User contact not found: " + request.getContactName() + ", user ID: " + user.getUserId());
        }
        UserContactEntity contact = contactOptional.get();
        userContactRepository.delete(contact);
        DeleteUserContactResponse response = new DeleteUserContactResponse();
        response.setUserId(user.getUserId());
        response.setContactName(contact.getName());
        return response;
    }

}