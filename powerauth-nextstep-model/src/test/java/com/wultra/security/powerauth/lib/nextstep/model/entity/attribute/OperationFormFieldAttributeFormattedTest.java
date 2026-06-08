/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2026 Wultra s.r.o.
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
package com.wultra.security.powerauth.lib.nextstep.model.entity.attribute;

import com.wultra.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OperationFormFieldAttributeFormatted}.
 *
 * @author Michal Rozehnal, michal.rozehnal@wultra.com
 */
class OperationFormFieldAttributeFormattedTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void testSerializationAndDeserialization() {
        // given
        final var formattedValues = Map.of("cs", "formatted-cs", "en", "formatted-en");
        final var attribute = new OperationKeyValueFieldAttribute(
                "id1",
                "Label",
                "Value",
                ValueFormatType.TEXT,
                formattedValues
        );

        // when
        final var json = objectMapper.writeValueAsString(attribute);

        final var attributeDeserialized = objectMapper.readValue(json, OperationFormFieldAttributeFormatted.class);

        // then
        assertEquals(formattedValues, attributeDeserialized.getFormattedValues());
    }

    @Test
    void testDeserializationOfNullFormattedValues() {
        // given
        final String json = """
                {
                    "type": "KEY_VALUE",
                    "id": "id1",
                    "label": "Label",
                    "value": "Value",
                    "valueFormatType": "TEXT",
                    "formattedValues": null
                }
                """;

        // when
        final var deserializedAttribute = objectMapper.readValue(json, OperationFormFieldAttributeFormatted.class);

        // then
        assertNotNull(deserializedAttribute.getFormattedValues());
        assertTrue(deserializedAttribute.getFormattedValues().isEmpty());
    }


    @Test
    void testDeserializationOfNonEmptyFormattedValues() {
        // given
        final var formattedValues = Map.of("cs", "formatted-cs", "en", "formatted-en");
        final String json = """
                {
                    "type": "KEY_VALUE",
                    "id": "id1",
                    "label": "Label",
                    "value": "Value",
                    "valueFormatType": "TEXT",
                    "formattedValues": {
                        "cs": "formatted-cs",
                        "en": "formatted-en"
                    }
                }
                """;

        // when
        final var deserializedAttribute = objectMapper.readValue(json, OperationFormFieldAttributeFormatted.class);

        // then
        assertEquals(formattedValues, deserializedAttribute.getFormattedValues());
    }

}

