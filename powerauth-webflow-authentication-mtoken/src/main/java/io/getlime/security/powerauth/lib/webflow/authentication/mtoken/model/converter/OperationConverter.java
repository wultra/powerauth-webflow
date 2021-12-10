/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.lib.mtoken.model.entity.AllowedSignatureType;
import io.getlime.security.powerauth.lib.mtoken.model.entity.Operation;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Converter for the Operation objects used for mobile API.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationConverter {

    private static final Logger logger = LoggerFactory.getLogger(OperationConverter.class);

    private final FormDataConverter formDataConverter = new FormDataConverter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert operation detail to Operation.
     * @param input Operation detail.
     * @param mobileTokenMode Mobile token mode.
     * @return Operation.
     */
    public Operation fromOperationDetailResponse(GetOperationDetailResponse input, String mobileTokenMode) {
        if (input == null || mobileTokenMode == null) { // we also do not want to have null signature type
            return null;
        }
        Operation result = new Operation();
        result.setId(input.getOperationId());
        result.setName(input.getOperationName());
        result.setData(input.getOperationData());
        result.setOperationCreated(input.getTimestampCreated());
        result.setOperationExpires(input.getTimestampExpires());
        result.setAllowedSignatureType(fromMobileTokenMode(mobileTokenMode));
        result.setFormData(formDataConverter.fromOperationFormData(input.getFormData()));
        return result;
    }

    /**
     * Convert mobile token mode JSON string to allowed signature type class.
     * @param mobileTokenMode Mobile token mode JSON string.
     * @return Allowed signature type class.
     */
    public AllowedSignatureType fromMobileTokenMode(String mobileTokenMode) {
        AllowedSignatureType allowedSignatureType;
        try {
            allowedSignatureType = objectMapper.readValue(mobileTokenMode, AllowedSignatureType.class);
        } catch (IOException e) {
            logger.error("Error while deserializing mobile token mode", e);
            return null;
        }
        return allowedSignatureType;
    }
}
