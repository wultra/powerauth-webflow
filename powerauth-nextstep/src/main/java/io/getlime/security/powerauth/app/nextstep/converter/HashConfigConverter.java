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
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.HashConfigDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;

import java.util.Map;

/**
 * Converter for hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class HashConfigConverter {

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Convert hashing configuration from entity to detail.
     * @param hashConfig Hashing configuration entity.
     * @return Hashing configuration detail.
     */
    public HashConfigDetail fromEntity(HashConfigEntity hashConfig) throws InvalidConfigurationException {
        final HashConfigDetail hashConfigDetail = new HashConfigDetail();
        hashConfigDetail.setHashConfigName(hashConfig.getName());
        hashConfigDetail.setAlgorithm(hashConfig.getAlgorithm());
        hashConfigDetail.setHashConfigStatus(hashConfig.getStatus());
        hashConfigDetail.setTimestampCreated(hashConfig.getTimestampCreated());
        try {
            Map<String, String> parameters = parameterConverter.fromString(hashConfig.getParameters());
            hashConfigDetail.getParameters().putAll(parameters);
        } catch (JsonProcessingException ex) {
            throw new InvalidConfigurationException(ex);
        }
        return hashConfigDetail;
    }

}