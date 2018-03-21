/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.mtoken.model.entity.AllowedSignatureType;
import io.getlime.security.powerauth.lib.mtoken.model.entity.Operation;

/**
 * Converter for the Operation objects used for mobile API.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationConverter {

    private FormDataConverter formDataConverter = new FormDataConverter();

    public Operation fromOperationDetailResponse(GetOperationDetailResponse input, AllowedSignatureType allowedSignatureType) {
        Operation result = new Operation();
        result.setId(input.getOperationId());
        result.setData(input.getOperationData());
        result.setOperationCreated(input.getTimestampCreated());
        result.setOperationExpires(input.getTimestampExpires());
        result.setAllowedSignatureType(allowedSignatureType);
        result.setFormData(formDataConverter.fromOperationFormData(input.getFormData()));
        return result;
    }

}
