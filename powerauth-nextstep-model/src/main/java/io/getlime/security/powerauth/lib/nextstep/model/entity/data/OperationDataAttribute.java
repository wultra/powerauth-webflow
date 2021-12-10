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

/**
 * Abstract operation data field.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public abstract class OperationDataAttribute {

    /**
     * Operation data attribute type.
     */
    public enum Type {
        /**
         * Monetary amount.
         */
        AMOUNT,

        /**
         * Generic account format.
         */
        ACCOUNT_GENERIC,

        /**
         * Account in IBAN format.
         */
        ACCOUNT_IBAN,

        /**
         * Payment date.
         */
        DATE,

        /**
         * Payment reference.
         */
        REFERENCE,

        /**
         * Payment note.
         */
        NOTE,

        /**
         * Text element.
         */
        TEXT
    }

    /**
     * Data type.
     */
    protected Type type;

    /**
     * Get operation data attribute type.
     * @return Attribute type.
     */
    public abstract Type getType();

    /**
     * Generate formatted value for attribute.
     * @return Formatted value.
     */
    public abstract String formattedValue();

}
