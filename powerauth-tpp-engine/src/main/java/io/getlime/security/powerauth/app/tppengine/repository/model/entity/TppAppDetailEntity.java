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

package io.getlime.security.powerauth.app.tppengine.repository.model.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity representing a TPP application details. This entity is connected to TPP via tpp_id on one end,
 * and to OAuth 2.1 client details via client_id on the other hand. As a result, this table does not contain
 * association with the OAuth 2.1 credentials. It only stores connection between TPP and the credentials and
 * also some basic metadata info related to the app.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "tpp_app_detail")
public class TppAppDetailEntity implements Serializable {

    private static final long serialVersionUID = 4100688209055833070L;

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

        private static final long serialVersionUID = -527239721500406289L;

        @Column(name = "app_client_id", nullable = false)
        private String appClientId;

        @Column(name = "tpp_id", nullable = false)
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
