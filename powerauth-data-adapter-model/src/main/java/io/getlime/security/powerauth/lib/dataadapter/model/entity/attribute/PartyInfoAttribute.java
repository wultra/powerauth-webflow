/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.PartyInfo;

/**
 * Class representing party information attribute.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
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