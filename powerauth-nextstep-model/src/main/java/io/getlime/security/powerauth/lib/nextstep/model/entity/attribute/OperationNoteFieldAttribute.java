package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing an operation form field attribute for the operation note.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationNoteFieldAttribute extends OperationFormFieldAttributeFormatted {

    private String note;

    public OperationNoteFieldAttribute() {
        this.type = Type.NOTE;
        this.valueFormatType = ValueFormatType.TEXT;
    }

    public OperationNoteFieldAttribute(ValueFormatType valueFormatType) {
        this.type = Type.NOTE;
        this.valueFormatType = valueFormatType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
