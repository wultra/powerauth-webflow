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
package io.getlime.security.powerauth.lib.dataadapter.impl.service;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationAmountFieldAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormFieldAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationKeyValueFieldAttribute;
import org.springframework.stereotype.Service;

/**
 * Service which extracts form data from an operation based on required input for SMS text.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class OperationFormDataService {

    private static final String FIELD_ACCOUNT_ID = "operation.account";

    /**
     * Extract amount from operation form data.
     * @param formData Operation form data.
     * @return Operation amount attribute.
     */
    public OperationAmountFieldAttribute getAmount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid");
        }
        return formData.getAmount();
    }

    /**
     * Extract account from operation form data.
     * @param formData Operation form data.
     * @return Operation to account value.
     */
    public String getAccount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid");
        }
        OperationFormFieldAttribute accountAttr = formData.getAttributeById(FIELD_ACCOUNT_ID);
        if (accountAttr == null) {
            return null;
        }
        if (!(accountAttr instanceof OperationKeyValueFieldAttribute)) {
            throw new IllegalStateException("Invalid account in formData");
        }
        return ((OperationKeyValueFieldAttribute)accountAttr).getValue();
    }

}
