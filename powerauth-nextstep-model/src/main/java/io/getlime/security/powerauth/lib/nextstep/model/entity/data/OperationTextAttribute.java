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
