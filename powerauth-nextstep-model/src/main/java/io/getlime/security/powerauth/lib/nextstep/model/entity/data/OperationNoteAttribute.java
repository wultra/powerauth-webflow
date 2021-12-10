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
