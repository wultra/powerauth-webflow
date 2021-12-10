/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.PartyInfo;

/**
 * Class representing party information attribute.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PartyInfoAttribute extends Attribute {

    private PartyInfo partyInfo;

    /**
     * Default constructor.
     */
    public PartyInfoAttribute() {
        this.type = Type.PARTY_INFO;
    }

    /**
     * Constructor with party information.
     * @param id Attribute ID.
     * @param partyInfo Party information.
     */
    public PartyInfoAttribute(String id, PartyInfo partyInfo) {
        this.type = Type.PARTY_INFO;
        this.id = id;
        this.partyInfo = partyInfo;
    }

    /**
     * Get party information.
     * @return Party information.
     */
    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    /**
     * Set party information.
     * @param partyInfo Party information.
     */
    public void setPartyInfo(PartyInfo partyInfo) {
        this.partyInfo = partyInfo;
    }
}