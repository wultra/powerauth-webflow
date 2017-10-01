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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;

/**
 * Request object used for updating form data of an operation.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class UpdateFormDataRequest {

    private String operationId;
    private OperationFormData formData;

    /**
     * Default constructor.
     */
    public UpdateFormDataRequest() {
    }

    /**
     * Constructor with operation ID and form data.
     * @param operationId Operation ID.
     * @param formData Form data.
     */
    public UpdateFormDataRequest(String operationId, OperationFormData formData) {
        this.operationId = operationId;
        this.formData = formData;
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
     * Get form data.
     * @return Form data.
     */
    public OperationFormData getFormData() {
        return formData;
    }

    /**
     * Set form data.
     * @param formData Form data.
     */
    public void setFormData(OperationFormData formData) {
        this.formData = formData;
    }
}
