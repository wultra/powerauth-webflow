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
package io.getlime.security.powerauth.lib.bankadapter.validation;

import com.fasterxml.jackson.databind.JsonNode;
import io.getlime.security.powerauth.lib.bankadapter.model.request.CreateSMSAuthorizationRequest;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for SMS OTP authorization requests.
 * <p>
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class CreateSMSAuthorizationRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateSMSAuthorizationRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CreateSMSAuthorizationRequest authRequest = (CreateSMSAuthorizationRequest) o;
        String userId = authRequest.getUserId();
        String operationName = authRequest.getOperationName();
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userId", "sms_authorization.user_id.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "operationName", "sms_authorization.operation_name.empty");

        // update validation logic based on the real requirements
        if (userId.length() > 30) {
            errors.rejectValue("userId", "sms_authorization.user_id.long");
        }
        if (operationName.length() > 32) {
            errors.rejectValue("operationName", "sms_authorization.operation_name.long");
        }

        JsonNode amount = authRequest.getOperationData().get("amount");
        JsonNode currency = authRequest.getOperationData().get("currency");
        JsonNode account = authRequest.getOperationData().get("account");
        if (amount == null) {
            errors.rejectValue("operationData", "sms_authorization.amount.empty");
        } else if (!amount.isNumber()) {
            errors.rejectValue("operationData", "sms_authorization.amount.invalid");
        }
        if (currency == null || currency.textValue().isEmpty()) {
            errors.rejectValue("operationData", "sms_authorization.currency.empty");
        }
        if (account == null || account.textValue().isEmpty()) {
            errors.rejectValue("operationData", "sms_authorization.account.empty");
        }

    }
}
