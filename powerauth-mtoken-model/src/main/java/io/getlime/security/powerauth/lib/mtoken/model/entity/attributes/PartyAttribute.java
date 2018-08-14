/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.mtoken.model.entity.attributes;

import io.getlime.security.powerauth.lib.mtoken.model.entity.PartyInfo;

/**
 * Attribute representing a party.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
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
