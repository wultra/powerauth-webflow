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
package io.getlime.security.powerauth.lib.mtoken.model.entity.attributes;

import io.getlime.security.powerauth.lib.mtoken.model.entity.PartyInfo;

/**
 * Attribute representing a party.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PartyAttribute extends Attribute {

    private PartyInfo partyInfo;

    /**
     * Default constructor.
     */
    public PartyAttribute() {
        super();
        this.setType(Type.PARTY_INFO);
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Heading text.
     * @param partyInfo Party information.
     */
    public PartyAttribute(String id, String label, PartyInfo partyInfo) {
        this();
        this.id = id;
        this.label = label;
        this.partyInfo = partyInfo;
    }

    /**
     * Get party info.
     * @return Party info.
     */
    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    /**
     * Set party info.
     * @param partyInfo Party info.
     */
    public void setPartyInfo(PartyInfo partyInfo) {
        this.partyInfo = partyInfo;
    }

}
