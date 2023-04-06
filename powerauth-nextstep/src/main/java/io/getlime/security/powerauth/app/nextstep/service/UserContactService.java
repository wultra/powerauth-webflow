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

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.UserContactConverter;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserContactEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String AUDIT_TYPE_USER_IDENTITY = "USER_IDENTITY";

    private final UserIdentityRepository userIdentityRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final Audit audit;

    private final UserContactConverter userContactConverter = new UserContactConverter();

    /**
     * User contact service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public UserContactService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, Audit audit) {
        this.serviceCatalogue = serviceCatalogue;
        this.userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();
        this.audit = audit;
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
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
        logger.debug("User contact was created, user ID: {}, contact name: {}", user.getUserId(), contact.getName());
        audit.info("User contact was created", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("contactName", contact.getName())
                .build());
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
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
        logger.debug("User contact was updated, user ID: {}, contact name: {}", user.getUserId(), contact.getName());
        audit.info("User contact was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("contactName", contact.getName())
                .build());
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Set<UserContactEntity> contacts = user.getContacts();
        Optional<UserContactEntity> contactOptional = contacts.stream().filter(c -> c.getName().equals(request.getContactName()) && c.getType().equals(request.getContactType())).findFirst();
        if (!contactOptional.isPresent()) {
            throw new UserContactNotFoundException("No user contact found: " + request.getContactName() + ", user ID: " + user.getUserId() + ", type: " + request.getContactType());
        }
        UserContactEntity contact = contactOptional.get();
        user.getContacts().remove(contact);
        user = userIdentityRepository.save(user);
        logger.debug("User contact was deleted, user ID: {}, contact name: {}", user.getUserId(), contact.getName());
        audit.info("User contact was deleted", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("contactName", contact.getName())
                .build());
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