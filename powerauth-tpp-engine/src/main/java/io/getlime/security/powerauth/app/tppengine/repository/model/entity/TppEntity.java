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
import java.util.List;

/**
 * Database entity representing a TPP details.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "tpp_detail")
public class TppEntity implements Serializable {

    private static final long serialVersionUID = -7089801604663605351L;

    @Id
    @SequenceGenerator(name = "tpp_detail", sequenceName = "tpp_detail_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "tpp_detail")
    @Column(name = "tpp_id", nullable = false)
    private Long tppId;

    @Column(name = "tpp_name")
    private String tppName;

    @Column(name = "tpp_license")
    private String tppLicense;

    @Column(name = "tpp_info")
    private String tppInfo;

    @Column(name = "tpp_address")
    private String tppAddress;

    @Column(name = "tpp_website")
    private String tppWebsite;

    @Column(name = "tpp_phone")
    private String tppPhone;

    @Column(name = "tpp_email")
    private String tppEmail;

    @Lob
    @Column(name = "tpp_logo")
    private byte[] tppLogo;

    @Column(name = "tpp_blocked")
    private boolean blocked;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "tpp")
    @OrderBy("app_name")
    private List<TppAppDetailEntity> applications;

    public TppEntity() {
    }

    public Long getTppId() {
        return tppId;
    }

    public void setTppId(Long tppId) {
        this.tppId = tppId;
    }

    public String getTppName() {
        return tppName;
    }

    public void setTppName(String tppName) {
        this.tppName = tppName;
    }

    public String getTppLicense() {
        return tppLicense;
    }

    public void setTppLicense(String tppLicense) {
        this.tppLicense = tppLicense;
    }

    public String getTppInfo() {
        return tppInfo;
    }

    public void setTppInfo(String tppInfo) {
        this.tppInfo = tppInfo;
    }

    public String getTppAddress() {
        return tppAddress;
    }

    public void setTppAddress(String tppAddress) {
        this.tppAddress = tppAddress;
    }

    public String getTppWebsite() {
        return tppWebsite;
    }

    public void setTppWebsite(String tppWebsite) {
        this.tppWebsite = tppWebsite;
    }

    public String getTppPhone() {
        return tppPhone;
    }

    public void setTppPhone(String tppPhone) {
        this.tppPhone = tppPhone;
    }

    public String getTppEmail() {
        return tppEmail;
    }

    public void setTppEmail(String tppEmail) {
        this.tppEmail = tppEmail;
    }

    public byte[] getTppLogo() {
        return tppLogo;
    }

    public void setTppLogo(byte[] tppLogo) {
        this.tppLogo = tppLogo;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public List<TppAppDetailEntity> getApplications() {
        return applications;
    }

    public void setApplications(List<TppAppDetailEntity> applications) {
        this.applications = applications;
    }
}
