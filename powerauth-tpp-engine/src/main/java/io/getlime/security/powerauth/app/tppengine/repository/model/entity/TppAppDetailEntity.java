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

package io.getlime.security.powerauth.app.tppengine.repository.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity representing a TPP application details. This entity is connected to TPP via tpp_id on one end,
 * and to OAuth 2.0 client details via client_id on the other hand. As a result, this table does not contain
 * association with the OAuth 2.0 credentials. It only stores connection between TPP and the credentials and
 * also some basic metadata info related to the app.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "tpp_app_detail")
public class TppAppDetailEntity implements Serializable {

    @EmbeddedId
    private TppAppDetailKey primaryKey;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "app_info")
    private String appInfo;

    @Column(name = "app_type")
    private String appType;

    @ManyToOne
    @JoinColumn(name = "tpp_id", referencedColumnName = "tpp_id", insertable = false, updatable = false)
    private TppEntity tpp;

    public TppAppDetailEntity() {
    }

    public TppAppDetailEntity(String clientId, Long tppId) {
        this.primaryKey = new TppAppDetailKey(clientId, tppId);
    }

    public TppAppDetailKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(TppAppDetailKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(String appInfo) {
        this.appInfo = appInfo;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public TppEntity getTpp() {
        return tpp;
    }

    public void setTpp(TppEntity tpp) {
        this.tpp = tpp;
    }

    @Embeddable
    public static class TppAppDetailKey implements Serializable {

        @Column(name = "app_client_id")
        private String appClientId;

        @Column(name = "tpp_id")
        private Long tppId;

        public TppAppDetailKey() {
        }

        public TppAppDetailKey(String appClientId, Long tppId) {
            this.appClientId = appClientId;
            this.tppId = tppId;
        }

        public String getAppClientId() {
            return appClientId;
        }

        public void setAppClientId(String appClientId) {
            this.appClientId = appClientId;
        }

        public Long getTppId() {
            return tppId;
        }

        public void setTppId(Long tppId) {
            this.tppId = tppId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TppAppDetailKey)) return false;
            TppAppDetailKey that = (TppAppDetailKey) o;
            return Objects.equals(appClientId, that.appClientId) && Objects.equals(tppId, that.tppId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appClientId, tppId);
        }
    }

}
