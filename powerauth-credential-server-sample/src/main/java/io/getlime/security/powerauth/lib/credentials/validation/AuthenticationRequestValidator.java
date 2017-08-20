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
package io.getlime.security.powerauth.lib.credentials.validation;

import io.getlime.security.powerauth.lib.credentials.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.credentials.model.request.AuthenticationRequest;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This class is used to define validations for input fields in authentication requests.
 *
 * Additional validation logic can be added if applicable.
 *
 * @author Roman Strobl
 */
public class AuthenticationRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthenticationRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AuthenticationRequest authRequest = (AuthenticationRequest) o;
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "login.username.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "login.password.empty");
        // update validation logic based on the real Credential server requirements
        if (username.length() > 30) {
            errors.rejectValue("username", "login.username.long");
        }
        if (password.length() > 30) {
            errors.rejectValue("password", "login.password.long");
        }
        AuthenticationType authType = authRequest.getType();
        if (authType != AuthenticationType.BASIC) {
            errors.rejectValue("type", "login.type.unsupported");
        }
    }
}
