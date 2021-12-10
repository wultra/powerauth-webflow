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
