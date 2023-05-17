/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -3077689235187445743L;

    @Id
    @Column(name = "config_id")
    private String afsConfigId;

    @Column(name = "js_snippet_url")
    private String jsSnippetUrl;

    @Column(name = "parameters")
    private String parameters;

    /**
     * Default constructor.
     */
    public AfsConfigEntity() {
    }

    /**
     * Entity constructor.
     * @param afsConfigId AFS configuration ID.
     * @param jsSnippetUrl JavaScript snipped for integration of AFS into Web Flow.
     * @param parameters Parameters which should be sent together with the AFS request.
     */
    public AfsConfigEntity(String afsConfigId, String jsSnippetUrl, String parameters) {
        this.afsConfigId = afsConfigId;
        this.jsSnippetUrl = jsSnippetUrl;
        this.parameters = parameters;
    }

    public String getAfsConfigId() {
        return afsConfigId;
    }

    public void setAfsConfigId(String afsConfigId) {
        this.afsConfigId = afsConfigId;
    }

    public String getJsSnippetUrl() {
        return jsSnippetUrl;
    }

    public void setJsSnippetUrl(String jsSnippetUrl) {
        this.jsSnippetUrl = jsSnippetUrl;
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
                jsSnippetUrl.equals(that.jsSnippetUrl) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(afsConfigId, jsSnippetUrl, parameters);
    }
}
