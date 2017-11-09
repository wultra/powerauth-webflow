package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing an operation form field attribute for the operation note.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationNoteFieldAttribute extends OperationFormFieldAttribute {

    private String note;

    public OperationNoteFieldAttribute() {
        this.type = Type.NOTE;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
