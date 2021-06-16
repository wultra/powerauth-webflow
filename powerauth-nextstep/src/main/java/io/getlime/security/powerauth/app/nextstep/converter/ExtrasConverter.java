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