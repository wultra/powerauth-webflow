/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity which stores configuration of anti-fraud system.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "wf_afs_config")
public class AfsConfigEntity implements Serializable {

    private static final long serialVersionUID = -3077689235187445743L;

    @Id
    @Column(name = "afs_config_id")
    private String afsConfigId;

    @Column(name = "js_snippet")
    private String jsSnippet;

    @Column(name = "parameters")
    private String parameters;

    public AfsConfigEntity() {
    }

    public AfsConfigEntity(String afsConfigId, String jsSnippet, String parameters) {
        this.afsConfigId = afsConfigId;
        this.jsSnippet = jsSnippet;
        this.parameters = parameters;
    }

    public String getAfsConfigId() {
        return afsConfigId;
    }

    public void setAfsConfigId(String afsConfigId) {
        this.afsConfigId = afsConfigId;
    }

    public String getJsSnippet() {
        return jsSnippet;
    }

    public void setJsSnippet(String jsSnippet) {
        this.jsSnippet = jsSnippet;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AfsConfigEntity that = (AfsConfigEntity) o;
        return afsConfigId.equals(that.afsConfigId) &&
                jsSnippet.equals(that.jsSnippet) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(afsConfigId, jsSnippet, parameters);
    }
}
