/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserContactEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserContactDetail;

/**
 * Converter for user contacts.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserContactConverter {

    /**
     * Convert user contact entity to detail.
     * @param contact User contact entity.
     * @return User contact detail.
     */
    public UserContactDetail fromEntity(UserContactEntity contact) {
        UserContactDetail contactDetail = new UserContactDetail();
        contactDetail.setContactName(contact.getName());
        contactDetail.setContactType(contact.getType());
        contactDetail.setContactValue(contact.getValue());
        contactDetail.setPrimary(contact.isPrimary());
        contactDetail.setTimestampCreated(contact.getTimestampCreated());
        contactDetail.setTimestampLastUpdated(contact.getTimestampLastUpdated());
        return contactDetail;
    }

}