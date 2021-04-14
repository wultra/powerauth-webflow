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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.converter.UserContactConverter;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserContactEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserContactDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ContactType;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This service handles persistence of user contacts.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserContactService {

    private final Logger logger = LoggerFactory.getLogger(UserContactService.class);

    private final UserIdentityLookupService userIdentityLookupService;
    private final UserIdentityRepository userIdentityRepository;

    private final UserContactConverter userContactConverter = new UserContactConverter();

    /**
     * User contact service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param userIdentityRepository User identity repository.
     */
    @Autowired
    public UserContactService(UserIdentityLookupService userIdentityLookupService, UserIdentityRepository userIdentityRepository) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.userIdentityRepository = userIdentityRepository;
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
        final Set<UserContactEntity> contacts = user.getContacts();
        final Optional<UserContactEntity> contactOptional = contacts.stream().filter(c -> c.getName().equals(request.getContactName()) && c.getType().equals(request.getContactType())).findFirst();
        if (contactOptional.isPresent()) {
            throw new UserContactAlreadyExistsException("User contact already exists: " + request.getContactName() + ", user ID: " + user.getUserId() + ", type: " + request.getContactType());
        }
        final UserContactEntity contact = new UserContactEntity();
        contact.setUser(user);
        contact.setName(request.getContactName());
        contact.setType(request.getContactType());
        contact.setValue(request.getContactValue());
        contact.setPrimary(request.isPrimary());
        contact.setTimestampCreated(new Date());
        user.getContacts().add(contact);
        // Ensure primary contacts are unique
        ensurePrimaryContactsAreUnique(user);
        user = userIdentityRepository.save(user);
        final CreateUserContactResponse response = new CreateUserContactResponse();
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
        Set<UserContactEntity> contacts = user.getContacts();
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
        Set<UserContactEntity> contacts = user.getContacts();
        Optional<UserContactEntity> contactOptional = contacts.stream().filter(c -> c.getName().equals(request.getContactName()) && c.getType().equals(request.getContactType())).findFirst();
        if (!contactOptional.isPresent()) {
            throw new UserContactNotFoundException("User contact not found: " + request.getContactName() + ", user ID: " + user.getUserId() + ", type: " + request.getContactType());
        }
        UserContactEntity contact = contactOptional.get();
        contact.setUser(user);
        contact.setName(request.getContactName());
        contact.setType(request.getContactType());
        contact.setValue(request.getContactValue());
        contact.setPrimary(request.isPrimary());
        contact.setTimestampLastUpdated(new Date());
        // Ensure primary contacts are unique
        ensurePrimaryContactsAreUnique(user);
        user = userIdentityRepository.save(user);
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
        Set<UserContactEntity> contacts = user.getContacts();
        Optional<UserContactEntity> contactOptional = contacts.stream().filter(c -> c.getName().equals(request.getContactName()) && c.getType().equals(request.getContactType())).findFirst();
        if (!contactOptional.isPresent()) {
            throw new UserContactNotFoundException("No user contact found: " + request.getContactName() + ", user ID: " + user.getUserId() + ", type: " + request.getContactType());
        }
        UserContactEntity contact = contactOptional.get();
        user.getContacts().remove(contact);
        user = userIdentityRepository.save(user);
        DeleteUserContactResponse response = new DeleteUserContactResponse();
        response.setUserId(user.getUserId());
        response.setContactName(request.getContactName());
        response.setContactType(request.getContactType());
        return response;
    }

    /**
     * In case multiple contacts are set as primary for the same contact type, the contact with newest created or last updated
     * date should be primary. This method is not transactional.
     */
    public void ensurePrimaryContactsAreUnique(UserIdentityEntity user) {
        Set<UserContactEntity> contacts = user.getContacts();
        Set<ContactType> contactTypes = contacts.stream().map(UserContactEntity::getType).collect(Collectors.toSet());
        for (ContactType ct : contactTypes) {
            // Find all primary contacts per contact type
            List<UserContactEntity> contactListPrimary = contacts.stream()
                    .filter(c -> c.getType().equals(ct))
                    .filter(UserContactEntity::isPrimary)
                    .collect(Collectors.toList());
            if (contactListPrimary.size() > 1) {
                // Multiple primary contacts exists, find the newest one by created or last updated date
                Date maxDate = new Date(0);
                for (UserContactEntity c : contactListPrimary) {
                    if (c.getTimestampCreated() != null && c.getTimestampCreated().after(maxDate)) {
                        maxDate = c.getTimestampCreated();
                    }
                    if (c.getTimestampLastUpdated() != null && c.getTimestampLastUpdated().after(maxDate)) {
                        maxDate = c.getTimestampLastUpdated();
                    }
                }
                UserContactEntity primaryContact = null;
                for (UserContactEntity c : contactListPrimary) {
                    if ((c.getTimestampCreated() != null && c.getTimestampCreated().equals(maxDate))
                            || (c.getTimestampLastUpdated() != null && c.getTimestampLastUpdated().equals(maxDate))) {
                        // This is the effectively primary contact, keep it primary
                        primaryContact = c;
                        break;
                    }
                }
                // Update primary contacts which were created earlier, they are no longer primary
                for (UserContactEntity c : contactListPrimary) {
                    if (c.equals(primaryContact)) {
                        continue;
                    }
                    c.setPrimary(false);
                }
            }
        }
    }

}