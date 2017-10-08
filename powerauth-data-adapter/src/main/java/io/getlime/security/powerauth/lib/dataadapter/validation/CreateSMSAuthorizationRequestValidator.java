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
package io.getlime.security.powerauth.lib.dataadapter.validation;

import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateSMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.dataadapter.service.OperationFormDataService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationAmountAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

/**
 * Validator for SMS OTP authorization requests.
 * <p>
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
        return CreateSMSAuthorizationRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CreateSMSAuthorizationRequest authRequest = (CreateSMSAuthorizationRequest) o;
        String userId = authRequest.getUserId();
        String operationName = authRequest.getOperationName();
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userId", "smsAuthorization.userId.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "operationName", "smsAuthorization.operationName.empty");

        // update validation logic based on the real requirements
        if (userId.length() > 30) {
            errors.rejectValue("userId", "smsAuthorization.userId.long");
        }
        if (operationName.length() > 32) {
            errors.rejectValue("operationName", "smsAuthorization.operationName.long");
        }

        OperationAmountAttribute amountAttribute = operationFormDataService.getAmount(authRequest.getOperationFormData());
        BigDecimal amount = amountAttribute.getAmount();
        String currency = amountAttribute.getCurrency();
        String account = operationFormDataService.getAccount(authRequest.getOperationFormData());
        if (amount == null) {
            errors.rejectValue("operationFormData", "smsAuthorization.amount.empty");
        } else if (amount.doubleValue()<=0) {
            errors.rejectValue("operationFormData", "smsAuthorization.amount.invalid");
        }
        if (currency == null || currency.isEmpty()) {
            errors.rejectValue("operationFormData", "smsAuthorization.currency.empty");
        }
        if (account == null || account.isEmpty()) {
            errors.rejectValue("operationFormData", "smsAuthorization.account.empty");
        }

    }
}
