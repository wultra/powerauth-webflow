/*
 * Copyright 2019 Wultra s.r.o.
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
 * Request for creating the OAuth 2.0 consent form.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateConsentFormRequest {

    /**
     * User ID for this request.
     */
    private String userId;

    /**
     * Operation context which provides context for creating the consent form.
     */
    private OperationContext operationContext;

    /**
     * Language used for generating the consent text.
     */
    private String lang;

    /**
     * Default constructor.
     */
    public CreateConsentFormRequest() {
    }

    /**
     * Constructor with user ID, language and operation context.
     * @param userId User ID.
     * @param lang Language used for generating contest text.
     * @param operationContext Operation context which provides context for creating the consent form.
     */
    public CreateConsentFormRequest(String userId, String lang, OperationContext operationContext) {
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
     * Get operation context which provides context for creating the consent form.
     * @return Operation context which provides context for creating the consent form.
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context which provides context for creating the consent form.
     * @param operationContext Operation context which provides context for creating the consent form.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    /**
     * Get language used for generating contest text.
     * @return Language used for generating contest text.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Set language used for generating contest text.
     * @param lang Language used for generating contest text.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }
}
