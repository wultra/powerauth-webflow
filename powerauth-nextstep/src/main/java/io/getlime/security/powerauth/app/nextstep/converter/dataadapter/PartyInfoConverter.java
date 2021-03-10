/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter.dataadapter;

import io.getlime.security.powerauth.lib.nextstep.model.entity.PartyInfo;

/**
 * Converter for party information.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PartyInfoConverter {

    /**
     * Converter from Next step PartyInfo.
     * @param input Next step PartyInfo.
     * @return Data adapter PartyInfo.
     */
    public io.getlime.security.powerauth.lib.dataadapter.model.entity.PartyInfo fromOperationPartyInfo(PartyInfo input) {
        if (input == null) {
            return null;
        }
        return new io.getlime.security.powerauth.lib.dataadapter.model.entity.PartyInfo(input.getLogoUrl(), input.getName(), input.getDescription(), input.getWebsiteUrl());
    }

    /**
     * Converter from Data adapter PartyInfo.
     * @param input Data adapter PartyInfo.
     * @return Next step PartyInfo.
     */
    public PartyInfo fromPartyInfo(io.getlime.security.powerauth.lib.dataadapter.model.entity.PartyInfo input) {
        if (input == null) {
            return null;
        }
        return new PartyInfo(input.getLogoUrl(), input.getName(), input.getDescription(), input.getWebsiteUrl());
    }
}
