/*
 * Copyright 2018 Wultra s.r.o.
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
        this.formattedValues.putAll(formattedValues);
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
