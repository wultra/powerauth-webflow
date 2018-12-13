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
package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;

/**
 * Request for creating SMS OTP authorization message.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateSMSAuthorizationRequest {


    /**
     * User ID for this authorization request.
     */
    private String userId;

    /**
     * Operation context.
     */
    private OperationContext operationContext;

    /**
     * Language used in the SMS OTP messages.
     */
    private String lang;

    /**
     * Default constructor.
     */
    public CreateSMSAuthorizationRequest() {
    }

    /**
     * Constructor with user ID, language and operation context.
     * @param userId User ID.
     * @param lang SMS language.
     * @param operationContext Operation context.
     */
    public CreateSMSAuthorizationRequest(String userId, String lang, OperationContext operationContext) {
        this.userId = userId;
        this.lang = lang;
        this.operationContext = operationContext;
    }

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID.
     * @param userId user ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get operation context.
     * @return Operation context.
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context.
     * @param operationContext Operation context.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    /**
     * Get SMS language.
     * @return SMS language.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Set SMS language.
     * @param lang SMS language.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }
}
