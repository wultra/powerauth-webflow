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
package io.getlime.security.powerauth.app.nextstep.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import org.springframework.stereotype.Service;

/**
 * Next Step client factory for testing.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class NextStepClientFactory {

    private final ObjectMapper objectMapper;

    /**
     * Next Step client factory constructor.
     * @param objectMapper Object mapper.
     */
    public NextStepClientFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Create Next Step client.
     * @param baseUrl Base URL.
     * @return Next Step client.
     * @throws NextStepClientException Thrown in case Next Step client could not be initialized.
     */
    public NextStepClient createNextStepClient(String baseUrl) throws NextStepClientException {
        RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setBaseUrl(baseUrl);
        restClientConfiguration.setObjectMapper(objectMapper);
        return new NextStepClient(restClientConfiguration);
    }
}
