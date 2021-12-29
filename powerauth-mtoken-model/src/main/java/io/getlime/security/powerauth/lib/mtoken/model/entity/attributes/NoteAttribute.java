/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.mtoken.model.entity.attributes;

/**
 * Attribute representing a key-value item, where key and value are displayed
 * below each other, with value that can extend over multiple lines.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class NoteAttribute extends Attribute {

    private String note;

    /**
     * Default constructor.
     */
    public NoteAttribute() {
        super();
        this.setType(Type.NOTE);
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Attribute label.
     * @param note Note.
     */
    public NoteAttribute(String id, String label, String note) {
        this();
        this.id = id;
        this.label = label;
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
}
