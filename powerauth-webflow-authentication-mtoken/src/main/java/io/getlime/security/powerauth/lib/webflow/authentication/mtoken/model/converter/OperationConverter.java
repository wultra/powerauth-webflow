/*
 * Copyright 2017 Wultra s.r.o.
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
