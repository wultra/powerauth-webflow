/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public Map<String, String> fromString(String param) throws JsonProcessingException {
        return objectMapper.readValue(param, new TypeReference<Map<String, String>>() {});
    }

    /**
     * Convert parameters from String to Object.
     * @param param Parameters serialized as string.
     * @param clazz Parameter type.
     * @return Object with deserialized parameters.
     */
    public <T> T fromString(String param, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(param, clazz);
    }

    /**
     * Convert parameters from Map to String.
     * @param paramMap Parameters map.
     * @return String with serialized parameters.
     */
    public String fromMap(Map<String, String> paramMap) throws JsonProcessingException {
        return objectMapper.writeValueAsString(paramMap);
    }

    /**
     * Convert parameters from Object to String.
     * @param object Parameters object.
     * @return String with serialized parameters.
     */
    public String fromObject(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

}