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

import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object used for creating a new operation.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class CreateOperationRequest {

    private String operationName;
    private String operationId;
    private String operationData;
    private String httpSessionId;
    private List<KeyValueParameter> params;
    private OperationFormData formData;

    /**
     * Default constructor.
     */
    public CreateOperationRequest() {
        params = new ArrayList<>();
    }

    /**
     * Get the new operation name.
     * @return Operation name.
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set the new operation name.
     * @param operationName Operation name.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Get the operation ID.
     *
     * @return Operation ID.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set the operation ID.
     *
     * @param operationId operation ID, use null value in case operation ID should be generated.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get operation data.
     * @return Operation data.
     */
    public String getOperationData() {
        return operationData;
    }

    /**
     * Set operation data.
     * @param operationData Operation data.
     */
    public void setOperationData(String operationData) {
        this.operationData = operationData;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    /**
     * Get the list with optional extra parameters.
     * @return Extra parameters.
     */
    public List<KeyValueParameter> getParams() {
        return params;
    }

    /**
     * Get form data (title, message, other visual attributes, ...) of the operation.
     * @return Form data.
     */
    public OperationFormData getFormData() {
        return formData;
    }

    /**
     * Set form data object.
     * @param formData Set form data.
     */
    public void setFormData(OperationFormData formData) {
        this.formData = formData;
    }
}
