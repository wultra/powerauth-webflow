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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;

/**
 * Response for getting the PowerAuth operation mapping.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetPAOperationMappingResponse {

    private String templateName;
    private String operationName;
    private String operationData;
    private FormData formData;

    /**
     * Default constructor.
     */
    public GetPAOperationMappingResponse() {
    }

    /**
     * Constructor with all parameters.
     * @param templateName PowerAuth operation template name.
     * @param operationName PowerAuth operation name.
     * @param operationData PowerAuth operation data.
     * @param formData PowerAuth operation form data.
     */
    public GetPAOperationMappingResponse(String templateName, String operationName, String operationData, FormData formData) {
        this.templateName = templateName;
        this.operationName = operationName;
        this.operationData = operationData;
        this.formData = formData;
    }

    /**
     * Get PowerAuth operation template name.
     * @return PowerAuth operation template name.
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Set PowerAuth operation template name.
     * @param templateName PowerAuth operation template name.
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Get PowerAuth operation name.
     * @return PowerAuth operation name.
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set PowerAuth operation name.
     * @param operationName PowerAuth operation name.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Get PowerAuth operation data.
     * @return PowerAuth Operation data.
     */
    public String getOperationData() {
        return operationData;
    }

    /**
     * Set PowerAuth operation data.
     * @param operationData PowerAuth operation data.
     */
    public void setOperationData(String operationData) {
        this.operationData = operationData;
    }

    /**
     * Get PowerAuth operation form data.
     * @return PowerAuth operation form data.
     */
    public FormData getFormData() {
        return formData;
    }

    /**
     * Set PowerAuth operation form data.
     * @param formData PowerAuth operation form data.
     */
    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}