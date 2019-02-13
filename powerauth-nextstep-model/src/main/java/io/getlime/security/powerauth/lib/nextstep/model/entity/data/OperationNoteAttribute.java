package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

import io.getlime.security.powerauth.lib.nextstep.model.converter.OperationTextNormalizer;

/**
 * Note in operation data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationNoteAttribute extends OperationDataAttribute {

    private String note;

    /**
     * Default constructor.
     */
    public OperationNoteAttribute() {
        this.type = Type.NOTE;
    }

    /**
     * Constructor with note.
     * @param note Note.
     */
    public OperationNoteAttribute(String note) {
        this.type = Type.NOTE;
        this.note = note;
    }

    /**
     * Get note.
     * @return Note.
     */
    public String getNote() {
        return note;
    }

    /**
     * Set note.
     * @param note Note.
     */
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (note == null) {
            return "";
        }
        return "N"+new OperationTextNormalizer().normalizeOperationData(note);
    }
}
