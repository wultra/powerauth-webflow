/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Converter for String lists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ValueListConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert values from String to List.
     * @param values Values serialized as string.
     * @return List with deserialized values.
     */
    public List<String> fromString(String values) throws JsonProcessingException {
        return objectMapper.readValue(values, new TypeReference<List<String>>() {});
    }

    /**
     * Convert parameters from String to Map.
     * @param valueList Parameters map.
     * @return String with serialized values.
     */
    public String fromList(List<String> valueList) throws JsonProcessingException {
        return objectMapper.writeValueAsString(valueList);
    }
}