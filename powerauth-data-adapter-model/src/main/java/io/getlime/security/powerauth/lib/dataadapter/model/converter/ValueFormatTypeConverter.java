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
package io.getlime.security.powerauth.lib.dataadapter.model.converter;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;

/**
 * Converter for value format type.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ValueFormatTypeConverter {

    /**
     * Converter from Next step value format type.
     * @param input Next step value format type.
     * @return Data adapter value format type.
     */
    public io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType fromOperationValueFormatType(ValueFormatType input) {
        return switch (input) {
            case AMOUNT -> io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.AMOUNT;
            case TEXT -> io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.TEXT;
            case LOCALIZED_TEXT -> io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.LOCALIZED_TEXT;
            case DATE -> io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.DATE;
            case NUMBER -> io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.NUMBER;
            case ACCOUNT -> io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.ACCOUNT;
        };
    }

    /**
     * Converter from Data adapter value format type.
     * @param input Data adapter value format type.
     * @return Next step value format type.
     */
    public ValueFormatType fromValueFormatType(io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType input) {
        return switch (input) {
            case AMOUNT -> ValueFormatType.AMOUNT;
            case TEXT -> ValueFormatType.TEXT;
            case LOCALIZED_TEXT -> ValueFormatType.LOCALIZED_TEXT;
            case DATE -> ValueFormatType.DATE;
            case NUMBER -> ValueFormatType.NUMBER;
            case ACCOUNT -> ValueFormatType.ACCOUNT;
        };
    }

}
