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
package io.getlime.security.powerauth.app.nextstep.service.adapter;

import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final OperationConverter operationConverter = new OperationConverter();

    /**
     * User identity customization service constructor.
     * @param dataAdapterClient Data Adapter client.
     */
    public UserLookupCustomizationService(DataAdapterClient dataAdapterClient) {
        this.dataAdapterClient = dataAdapterClient;
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