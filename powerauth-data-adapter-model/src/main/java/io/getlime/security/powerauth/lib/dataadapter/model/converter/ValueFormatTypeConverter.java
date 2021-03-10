/*
 * Copyright 2017 Wultra s.r.o.
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
        switch (input) {
            case AMOUNT:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.AMOUNT;
            case TEXT:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.TEXT;
            case LOCALIZED_TEXT:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.LOCALIZED_TEXT;
            case DATE:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.DATE;
            case NUMBER:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.NUMBER;
            case ACCOUNT:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType.ACCOUNT;
            default:
                throw new IllegalStateException("Unsupported value format type: "+input);
        }
    }

    /**
     * Converter from Data adapter value format type.
     * @param input Data adapter value format type.
     * @return Next step value format type.
     */
    public ValueFormatType fromValueFormatType(io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType input) {
        switch (input) {
            case AMOUNT:
                return ValueFormatType.AMOUNT;
            case TEXT:
                return ValueFormatType.TEXT;
            case LOCALIZED_TEXT:
                return ValueFormatType.LOCALIZED_TEXT;
            case DATE:
                return ValueFormatType.DATE;
            case NUMBER:
                return ValueFormatType.NUMBER;
            case ACCOUNT:
                return ValueFormatType.ACCOUNT;
            default:
                throw new IllegalStateException("Unsupported value format type: "+input);
        }
    }

}
