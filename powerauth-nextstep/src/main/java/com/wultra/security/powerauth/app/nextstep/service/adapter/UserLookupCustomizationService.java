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
package com.wultra.security.powerauth.app.nextstep.service.adapter;

import com.wultra.security.powerauth.app.nextstep.converter.OperationConverter;
import com.wultra.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import com.wultra.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import com.wultra.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import com.wultra.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import com.wultra.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import com.wultra.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import com.wultra.security.powerauth.lib.nextstep.model.response.GetUserDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service handles customization of user identity lookup.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserLookupCustomizationService {

    private final Logger logger = LoggerFactory.getLogger(UserLookupCustomizationService.class);

    private final DataAdapterClient dataAdapterClient;
    private final OperationConverter operationConverter;

    /**
     * User identity customization service constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param operationConverter Operation converter.
     */
    @Autowired
    public UserLookupCustomizationService(DataAdapterClient dataAdapterClient, OperationConverter operationConverter) {
        this.dataAdapterClient = dataAdapterClient;
        this.operationConverter = operationConverter;
    }

    /**
     * Lookup a user identity using Data Adapter.
     * @param username Username.
     * @param organizationId Organization ID.
     * @param operation Operation entity.
     * @return User detail response.
     */
    public GetUserDetailResponse lookupUser(String username, String organizationId, OperationEntity operation) {
        try {
            final OperationContext operationContext = operationConverter.toOperationContext(operation);
            // TODO - add support for user lookup using client certificate
            final UserDetailResponse response = dataAdapterClient.lookupUser(username, organizationId, null, operationContext).getResponseObject();
            final GetUserDetailResponse userDetail = new GetUserDetailResponse();
            userDetail.setUserId(response.getId());
            if (response.getAccountStatus() == AccountStatus.ACTIVE) {
                userDetail.setUserIdentityStatus(UserIdentityStatus.ACTIVE);
            } else {
                userDetail.setUserIdentityStatus(UserIdentityStatus.BLOCKED);
            }
            return userDetail;
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
            return null;
        }
    }
}