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

package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChangeEntity;

/**
 * Request object for notifying data adapter about operation change.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class FormDataChangeNotificationRequest {

    private String userId;
    private String operationId;
    private FormDataChangeEntity formDataChange;

    /**
     * Default constructor
     */
    public FormDataChangeNotificationRequest() {
    }

    /**
     * Constructor with user ID, operation ID and formData change.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @param formDataChange FormData change.
     */
    public FormDataChangeNotificationRequest(String userId, String operationId, FormDataChangeEntity formDataChange) {
        this.userId = userId;
        this.operationId = operationId;
        this.formDataChange = formDataChange;
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
     * Get operation ID.
     * @return Operation ID.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set operation ID.
     * @param operationId Operation ID.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get formData change.
     * @return FormData change.
     */
    public FormDataChangeEntity getFormDataChange() {
        return formDataChange;
    }

    /**
     * Set formData change.
     * @param formDataChange Change of formData.
     */
    public void setFormDataChange(FormDataChangeEntity formDataChange) {
        this.formDataChange = formDataChange;
    }
}
