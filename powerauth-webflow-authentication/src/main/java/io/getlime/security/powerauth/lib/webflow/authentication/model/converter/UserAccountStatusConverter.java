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
package io.getlime.security.powerauth.lib.webflow.authentication.model.converter;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;

/**
 * Converter for user account status.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAccountStatusConverter {

    /**
     * Convert of user account status.
     * @param userAccountStatus User account status in Next Step.
     * @return User account status in Data Adapter.
     */
    public AccountStatus fromUserAccountStatus(UserAccountStatus userAccountStatus) {
        if (userAccountStatus == null) {
            return null;
        }

        switch (userAccountStatus) {
            case ACTIVE:
                return AccountStatus.ACTIVE;

            case NOT_ACTIVE:
                return AccountStatus.NOT_ACTIVE;

            default:
                return null;
        }
    }

    public UserAccountStatus fromAccountStatus(AccountStatus accountStatus) {
        if (accountStatus == null) {
            return null;
        }

        switch (accountStatus) {
            case ACTIVE:
                return UserAccountStatus.ACTIVE;

            case NOT_ACTIVE:
                return UserAccountStatus.NOT_ACTIVE;

            default:
                return null;
        }
    }
}
