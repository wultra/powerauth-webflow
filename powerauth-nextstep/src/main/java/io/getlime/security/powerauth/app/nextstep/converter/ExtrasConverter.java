/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

import java.util.Map;

/**
 * Converter for extras maps.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ExtrasConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert extras from String to Map.
     * @param extras Extras serialized as string.
     * @return Map with deserialized extras.
     */
    public Map<String, Object> fromString(String extras) throws JsonProcessingException {
        return objectMapper.readValue(extras, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Convert extras from String to Map.
     * @param extrasMap Extras map.
     * @return String with serialized extras.
     */
    public String fromMap(Map<String, Object> extrasMap) throws JsonProcessingException {
        return objectMapper.writeValueAsString(extrasMap);
    }
}