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

import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;

/**
 * Request object for notifying data adapter about operation form data change.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FormDataChangeNotificationRequest {

    private String userId;
    private OperationContext operationContext;
    private FormDataChange formDataChange;

    /**
     * Default constructor.
     */
    public FormDataChangeNotificationRequest() {
    }

    /**
     * Constructor with user ID, operation ID and form data change.
     * @param userId User ID.
     * @param formDataChange Form data change.
     * @param operationContext Operation context.
     */
    public FormDataChangeNotificationRequest(String userId, FormDataChange formDataChange, OperationContext operationContext) {
        this.userId = userId;
        this.formDataChange = formDataChange;
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
     * @param userId User ID.
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
     * Get formData change.
     * @return FormData change.
     */
    public FormDataChange getFormDataChange() {
        return formDataChange;
    }

    /**
     * Set formData change.
     * @param formDataChange Change of form data.
     */
    public void setFormDataChange(FormDataChange formDataChange) {
        this.formDataChange = formDataChange;
    }
}
