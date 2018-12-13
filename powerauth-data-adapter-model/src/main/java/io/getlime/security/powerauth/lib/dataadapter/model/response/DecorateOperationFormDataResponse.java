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

package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;

/**
 * Response with decorated operation form data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DecorateOperationFormDataResponse {

    private FormData formData;

    /**
     * Default constructor.
     */
    public DecorateOperationFormDataResponse() {
    }

    /**
     * Constructor with operation form data.
     * @param formData Operation form data.
     */
    public DecorateOperationFormDataResponse(FormData formData) {
        this.formData = formData;
    }

    /**
     * Get operation form data.
     * @return Operation form data.
     */
    public FormData getFormData() {
        return formData;
    }

    /**
     * Set operation form data.
     * @param formData Operation form data.
     */
    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}