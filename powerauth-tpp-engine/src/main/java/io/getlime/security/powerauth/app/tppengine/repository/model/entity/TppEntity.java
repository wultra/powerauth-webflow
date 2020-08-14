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

    public List<TppAppDetailEntity> getApplications() {
        return applications;
    }

    public void setApplications(List<TppAppDetailEntity> applications) {
        this.applications = applications;
    }
}
