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

import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This service handles user identity lookup.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserIdentityLookupService {

    private final UserIdentityRepository userIdentityRepository;

    @Autowired
    public UserIdentityLookupService(UserIdentityRepository userIdentityRepository) {
        this.userIdentityRepository = userIdentityRepository;
    }

    /**
     * Find a user identity. The method is not transactional and should be used for utility purposes only.
     * @param userId User ID.
     * @return User identity entity.
     * @throws UserNotFoundException Thrown when user identity entity is not found.
     */
    public UserIdentityEntity findUser(String userId) throws UserNotFoundException {
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User identity not found: " + userId);
        }
        UserIdentityEntity user = userOptional.get();
        if (user.getStatus() == UserIdentityStatus.REMOVED) {
            throw new UserNotFoundException("User identity is REMOVED: " + userId);
        }
        return user;
    }

}