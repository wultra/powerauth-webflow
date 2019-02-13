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
package io.getlime.security.powerauth.lib.nextstep.model.response;

/**
 * Response object used for getting the operation configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetOperationConfigResponse {

    private String operationName;
    private String templateVersion;
    private Integer templateId;
    private String mobileTokenMode;

    /**
     * Get operation name.
     * @return Operation name.
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set operation name.
     * @param operationName Operation name.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Get template version.
     * @return Template version.
     */
    public String getTemplateVersion() {
        return templateVersion;
    }

    /**
     * Set template version.
     * @param templateVersion Template version.
     */
    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    /**
     * Get template ID.
     * @return Template ID.
     */
    public Integer getTemplateId() {
        return templateId;
    }

    /**
     * Set template ID.
     * @param templateId Template ID.
     */
    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    /**
     * Get mobile token mode - JSON configuration of mobile token signatures.
     * @return Mobile token mode.
     */
    public String getMobileTokenMode() {
        return mobileTokenMode;
    }

    /**
     * Set mobile token mode - JSON configuration of mobile token signatures.
     * @param mobileTokenMode Mobile token mode.
     */
    public void setMobileTokenMode(String mobileTokenMode) {
        this.mobileTokenMode = mobileTokenMode;
    }
}
