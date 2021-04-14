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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserAliasEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAliasDetail;

import java.util.Map;

/**
 * Converter for user aliases.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAliasConverter {

    private final ExtrasConverter extrasConverter = new ExtrasConverter();

    /**
     * Convert user alias entity to detail.
     * @param alias User alias entity.
     * @return User alias detail.
     */
    public UserAliasDetail fromEntity(UserAliasEntity alias) throws JsonProcessingException {
        final UserAliasDetail aliasDetail = new UserAliasDetail();
        aliasDetail.setAliasName(alias.getName());
        aliasDetail.setAliasValue(alias.getValue());
        aliasDetail.setUserAliasStatus(alias.getStatus());
        aliasDetail.setTimestampCreated(alias.getTimestampCreated());
        aliasDetail.setTimestampLastUpdated(alias.getTimestampLastUpdated());
        if (alias.getExtras() != null) {
            final Map<String, Object> extras = extrasConverter.fromString(alias.getExtras());
            aliasDetail.getExtras().putAll(extras);
        }
        return aliasDetail;
    }

}