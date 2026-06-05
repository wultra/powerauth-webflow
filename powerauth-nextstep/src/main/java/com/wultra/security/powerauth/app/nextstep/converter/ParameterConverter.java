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
package com.wultra.security.powerauth.app.nextstep.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Converter for parameter maps.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ParameterConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert parameters from String to Map.
     * @param param Parameters serialized as string.
     * @return Map with deserialized parameters.
     */
    public Map<String, String> fromString(String param) throws JacksonException {
        return objectMapper.readValue(param, new TypeReference<>() {});
    }

    /**
     * Convert parameters from String to Object.
     * @param param Parameters serialized as string.
     * @param clazz Parameter type.
     * @return Object with deserialized parameters.
     */
    public <T> T fromString(String param, Class<T> clazz) throws JacksonException {
        return objectMapper.readValue(param, clazz);
    }

    /**
     * Convert parameters from Map to String.
     * @param paramMap Parameters map.
     * @return String with serialized parameters.
     */
    public String fromMap(Map<String, String> paramMap) throws JacksonException {
        return objectMapper.writeValueAsString(paramMap);
    }

    /**
     * Convert parameters from Object to String.
     * @param object Parameters object.
     * @return String with serialized parameters.
     */
    public String fromObject(Object object) throws JacksonException {
        return objectMapper.writeValueAsString(object);
    }

}