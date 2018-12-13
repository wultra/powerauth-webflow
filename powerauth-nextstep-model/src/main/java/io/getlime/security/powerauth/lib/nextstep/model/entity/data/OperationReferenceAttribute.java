package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

import io.getlime.security.powerauth.lib.nextstep.model.converter.OperationTextNormalizer;

/**
 * Reference in operation data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationReferenceAttribute extends OperationDataAttribute {

    private String reference;

    /**
     * Default constructor.
     */
    public OperationReferenceAttribute() {
        this.type = Type.REFERENCE;
    }

    /**
     * Constructor with reference.
     * @param reference Reference.
     */
    public OperationReferenceAttribute(String reference) {
        this.type = Type.REFERENCE;
        this.reference = reference;
    }

    /**
     * Get reference.
     * @return Reference.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Set reference.
     * @param reference Reference.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (reference == null) {
            return "";
        }
        return "R"+new OperationTextNormalizer().normalizeOperationData(reference);
    }
}
