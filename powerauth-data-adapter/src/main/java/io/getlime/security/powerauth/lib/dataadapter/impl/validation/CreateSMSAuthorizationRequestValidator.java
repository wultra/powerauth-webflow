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
package io.getlime.security.powerauth.lib.dataadapter.impl.validation;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.security.powerauth.lib.dataadapter.impl.service.OperationFormDataService;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateSMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationAmountFieldAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

/**
 * Validator for SMS OTP authorization requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Component
public class CreateSMSAuthorizationRequestValidator implements Validator {

    private OperationFormDataService operationFormDataService;

    @Autowired
    public CreateSMSAuthorizationRequestValidator(OperationFormDataService operationFormDataService) {
        this.operationFormDataService = operationFormDataService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ObjectRequest.class.isAssignableFrom(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(Object o, Errors errors) {
        ObjectRequest<CreateSMSAuthorizationRequest> requestObject = (ObjectRequest<CreateSMSAuthorizationRequest>) o;
        CreateSMSAuthorizationRequest authRequest = requestObject.getRequestObject();

        // update validation logic based on the real Data Adapter requirements
        String userId = authRequest.getUserId();
        String operationName = authRequest.getOperationName();

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.userId", "smsAuthorization.userId.empty");
        if (userId != null && userId.length() > 30) {
            errors.rejectValue("requestObject.userId", "smsAuthorization.userId.long");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.operationName", "smsAuthorization.operationName.empty");
        if (operationName != null && operationName.length() > 32) {
            errors.rejectValue("requestObject.operationName", "smsAuthorization.operationName.long");
        }

        OperationAmountFieldAttribute amountAttribute = operationFormDataService.getAmount(authRequest.getOperationFormData());
        BigDecimal amount = amountAttribute.getAmount();
        String currency = amountAttribute.getCurrency();
        String account = operationFormDataService.getAccount(authRequest.getOperationFormData());

        if (amount == null) {
            errors.rejectValue("requestObject.operationFormData", "smsAuthorization.amount.empty");
        } else if (amount.doubleValue()<=0) {
            errors.rejectValue("requestObject.operationFormData", "smsAuthorization.amount.invalid");
        }

        if (currency == null || currency.isEmpty()) {
            errors.rejectValue("requestObject.operationFormData", "smsAuthorization.currency.empty");
        }

        if (account == null || account.isEmpty()) {
            errors.rejectValue("requestObject.operationFormData", "smsAuthorization.account.empty");
        }

    }
}
