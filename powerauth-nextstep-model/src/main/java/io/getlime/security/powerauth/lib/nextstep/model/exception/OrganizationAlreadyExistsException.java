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
package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when organization already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OrganizationAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "ORGANIZATION_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OrganizationAlreadyExistsException(String message) {
        super(message);
    }

}
