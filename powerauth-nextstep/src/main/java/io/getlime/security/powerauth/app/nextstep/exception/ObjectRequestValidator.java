/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.exception;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Custom validator for the generic {@link ObjectRequest} type.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class ObjectRequestValidator implements Validator {

    private final Validator validator;

    @Autowired
    public ObjectRequestValidator(@Qualifier("localValidator") Validator validator) {
        this.validator = validator;
    }

    /**
     * Return whether validator can validate given class.
     *
     * @param clazz Validated class.
     * @return Whether validator can validate given class.
     */
    public boolean supports(@NonNull Class<?> clazz) {
        return ObjectRequest.class.isAssignableFrom(clazz);
    }

    /**
     * Validate object and add validation errors.
     *
     * @param o Validated object.
     * @param errors Errors object.
     */
    public void validate(@Nullable Object o, @NonNull Errors errors) {
        ObjectRequest<?> objectRequest = (ObjectRequest<?>) o;
        if (objectRequest == null) {
            errors.reject("Object request is null");
            return;
        }
        Object requestObject = objectRequest.getRequestObject();
        if (requestObject == null) {
            errors.reject("Request object is null");
            return;
        }
        validator.validate(requestObject, errors);
    }
}
