package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import io.getlime.security.powerauth.lib.nextstep.model.entity.PartyInfo;

/**
 * Class representing an operation form field attribute for party information.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationPartyInfoFieldAttribute extends OperationFormFieldAttribute {

    private PartyInfo partyInfo;

    /**
     * Default constructor.
     */
    public OperationPartyInfoFieldAttribute() {
        this.type = Type.PARTY_INFO;
    }

    /**
     * Constructor with party information.
     * @param id Attribute ID.
     * @param partyInfo Party information.
     */
    public OperationPartyInfoFieldAttribute(String id, PartyInfo partyInfo) {
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
