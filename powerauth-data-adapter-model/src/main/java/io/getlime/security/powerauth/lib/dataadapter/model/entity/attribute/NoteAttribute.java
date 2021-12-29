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

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType;

import java.util.Map;

/**
 * Class representing an operation form field attribute for the operation note.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class NoteAttribute extends AttributeFormatted {

    private String note;

    /**
     * Default constructor.
     */
    public NoteAttribute() {
        this.type = Type.NOTE;
        this.valueFormatType = ValueFormatType.TEXT;
    }

    /**
     * Constructor with value format type.
     * @param valueFormatType Value format type.
     */
    public NoteAttribute(ValueFormatType valueFormatType) {
        this.type = Type.NOTE;
        this.valueFormatType = valueFormatType;
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Label.
     * @param note Note.
     * @param valueFormatType Value format type.
     * @param formattedValues Formatted values.
     */
    public NoteAttribute(String id, String label, String note, ValueFormatType valueFormatType, Map<String, String> formattedValues) {
        this.type = Type.NOTE;
        this.id = id;
        this.label = label;
        this.note = note;
        this.valueFormatType = valueFormatType;
        if (formattedValues != null) {
            addFormattedValues(formattedValues);
        }
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
}
