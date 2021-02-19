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
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.HashingConfigDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;

import java.util.Map;

/**
 * Converter for hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class HashingConfigConverter {

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Convert hashing configuration from entity to detail.
     * @param hashConfig Hashing configuration entity.
     * @return Hashing configuration detail.
     */
    public HashingConfigDetail fromEntity(HashConfigEntity hashConfig) throws InvalidRequestException {
        HashingConfigDetail hashingConfigDetail = new HashingConfigDetail();
        hashingConfigDetail.setHashConfigName(hashConfig.getName());
        hashingConfigDetail.setAlgorithm(hashConfig.getAlgorithm());
        hashingConfigDetail.setHashConfigStatus(hashConfig.getStatus());
        hashingConfigDetail.setTimestampCreated(hashConfig.getTimestampCreated());
        try {
            Map<String, String> parameters = parameterConverter.fromString(hashConfig.getParameters());
            hashingConfigDetail.getParameters().putAll(parameters);
        } catch (JsonProcessingException ex) {
            throw new InvalidRequestException(ex);
        }
        return hashingConfigDetail;
    }

}