/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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