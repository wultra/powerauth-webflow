package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

import io.getlime.security.powerauth.lib.nextstep.model.converter.OperationTextNormalizer;

/**
 * Text in operation data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationTextAttribute extends OperationDataAttribute {

    private String text;

    /**
     * Default constructor.
     */
    public OperationTextAttribute() {
        this.type = Type.TEXT;
    }

    /**
     * Constructor with text.
     * @param text Text.
     */
    public OperationTextAttribute(String text) {
        this.type = Type.TEXT;
        this.text = text;
    }

    /**
     * Get text.
     * @return Text.
     */
    public String getText() {
        return text;
    }

    /**
     * Set text.
     * @param text Text.
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (text == null) {
            return "";
        }
        return "T"+new OperationTextNormalizer().normalizeOperationData(text);
    }
}
