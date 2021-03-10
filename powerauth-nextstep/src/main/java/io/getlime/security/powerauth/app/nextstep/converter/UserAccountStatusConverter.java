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

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;

/**
 * Converter for user account status.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAccountStatusConverter {

    /**
     * Convert user identity status to account status.
     * @param userIdentityStatus User identity status.
     * @return Account status in Data Adapter.
     */
    public AccountStatus toAccountStatus(UserIdentityStatus userIdentityStatus) {
        if (userIdentityStatus == null) {
            return null;
        }

        switch (userIdentityStatus) {
            case ACTIVE:
                return AccountStatus.ACTIVE;

            case BLOCKED:
            case REMOVED:
                return AccountStatus.NOT_ACTIVE;

            default:
                return null;
        }
    }

    /**
     * Convert user identity status to user account status.
     * @param userIdentityStatus User identity status.
     * @return User account status in Next Step.
     */
    public UserAccountStatus toUserAccountStatus(UserIdentityStatus userIdentityStatus) {
        if (userIdentityStatus == null) {
            return null;
        }

        switch (userIdentityStatus) {
            case ACTIVE:
                return UserAccountStatus.ACTIVE;

            case BLOCKED:
            case REMOVED:
                return UserAccountStatus.NOT_ACTIVE;

            default:
                return null;
        }
    }

    /**
     * Convert user account status in Next Step to account status in Data Adapter.
     * @param accountStatus User account status.
     * @return Account status in Data Adapter.
     */
    public AccountStatus fromUserAccountStatus(UserAccountStatus accountStatus) {
        if (accountStatus == null) {
            return null;
        }

        switch (accountStatus) {
            case ACTIVE:
                return AccountStatus.ACTIVE;

            case NOT_ACTIVE:
                return AccountStatus.NOT_ACTIVE;

            default:
                return null;
        }
    }

    /**
     * Convert account status in Data Adapter to user account status in Next Step.
     * @param userAccountStatus User account status in Next Step.
     * @return User account status in Data Adapter.
     */
    public UserAccountStatus fromAccountStatus(AccountStatus userAccountStatus) {
        if (userAccountStatus == null) {
            return null;
        }

        switch (userAccountStatus) {
            case ACTIVE:
                return UserAccountStatus.ACTIVE;

            case NOT_ACTIVE:
                return UserAccountStatus.NOT_ACTIVE;

            default:
                return null;
        }
    }

}
