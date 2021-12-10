/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.converter;

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
