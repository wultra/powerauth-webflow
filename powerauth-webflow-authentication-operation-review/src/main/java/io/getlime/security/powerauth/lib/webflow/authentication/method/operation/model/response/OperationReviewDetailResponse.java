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

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * Response with operation detail.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationReviewDetailResponse {

    private String data;
    private OperationFormData formData;
    private AuthMethod chosenAuthMethod;

    /**
     * Get data.
     * @return Data.
     */
    public String getData() {
        return data;
    }

    /**
     * Set data.
     * @param data Data.
     */
    public void setData(String data) {
        this.data = data;
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

    /**
     * Get chosen authentication method.
     * @return Chosen authentication method.
     */
    public AuthMethod getChosenAuthMethod() {
        return chosenAuthMethod;
    }

    /**
     * Set chosen authentication method.
     * @param chosenAuthMethod Chosen authentication method.
     */
    public void setChosenAuthMethod(AuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
    }
}
