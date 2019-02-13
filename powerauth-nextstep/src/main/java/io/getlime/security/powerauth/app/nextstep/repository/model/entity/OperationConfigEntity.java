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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity which stores configuration of operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_operation_config")
public class OperationConfigEntity implements Serializable {

    private static final long serialVersionUID = 4855111531493246740L;

    @Id
    @Column(name = "operation_name")
    private String operationName;

    @Column(name = "template_version")
    private String templateVersion;

    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "mobile_token_mode")
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
     * Get mobile token mode configuration.
     * @return Mobile token mode configuration.
     */
    public String getMobileTokenMode() {
        return mobileTokenMode;
    }

    /**
     * Set mobile token mode configuration.
     * @param mobileTokenMode Mobile token mode configuration.
     */
    public void setMobileTokenMode(String mobileTokenMode) {
        this.mobileTokenMode = mobileTokenMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationConfigEntity that = (OperationConfigEntity) o;
        return Objects.equals(operationName, that.operationName) &&
                Objects.equals(templateVersion, that.templateVersion) &&
                Objects.equals(templateId, that.templateId) &&
                Objects.equals(mobileTokenMode, that.mobileTokenMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationName, templateVersion, templateId, mobileTokenMode);
    }
}
