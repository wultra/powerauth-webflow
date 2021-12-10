/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
