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
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.dataadapter.model.request.AuthenticationRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Defines validations for input fields in authentication requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Component
public class AuthenticationRequestValidator implements Validator {

    /**
     * Return whether validator can validate given class.
     * @param clazz Validated class.
     * @return Whether validator can validate given class.
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return ObjectRequest.class.isAssignableFrom(clazz);
    }

    /**
     * Validate object and add validation errors.
     * @param o Validated object.
     * @param errors Errors object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void validate(Object o, Errors errors) {
        ObjectRequest<AuthenticationRequest> requestObject = (ObjectRequest<AuthenticationRequest>) o;
        AuthenticationRequest authRequest = requestObject.getRequestObject();

        // update validation logic based on the real Data Adapter requirements
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.username", "login.username.empty");
        if (username!=null && username.length() > 30) {
            errors.rejectValue("requestObject.username", "login.username.long");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "requestObject.password", "login.password.empty");
        if (password!=null && password.length() > 30) {
            errors.rejectValue("requestObject.password", "login.password.long");
        }

        AuthenticationType authType = authRequest.getType();
        if (authType != AuthenticationType.BASIC) {
            errors.rejectValue("requestObject.type", "login.type.unsupported");
        }
    }
}
