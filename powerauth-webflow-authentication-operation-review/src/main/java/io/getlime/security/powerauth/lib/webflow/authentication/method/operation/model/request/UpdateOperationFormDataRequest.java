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

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Request to update operation form data.
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateOperationFormDataRequest extends AuthStepRequest {

    private OperationFormData formData;

    /**
     * Default constructor.
     */
    public UpdateOperationFormDataRequest() {
    }

    /**
     * Constructor with form data.
     * @param formData Form data.
     */
    public UpdateOperationFormDataRequest(OperationFormData formData) {
        this.formData = formData;
    }

    /**
     * Get the form data.
     * @return Form data.
     */
    public OperationFormData getFormData() {
        return formData;
    }

    /**
     * Set the form data.
     * @param formData form data.
     */
    public void setFormData(OperationFormData formData) {
        this.formData = formData;
    }
}
