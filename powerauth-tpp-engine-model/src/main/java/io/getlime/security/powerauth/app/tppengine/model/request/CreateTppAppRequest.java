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

package io.getlime.security.powerauth.app.tppengine.model.request;

/**
 * @author Petr Dvorak, petr@wultra.com
 */
public class CreateTppAppRequest {

    /**
     * TPP app name.
     */
    private String appName;

    /**
     * TPP app description.
     */
    private String appDescription;

    /**
     * App Type.
     */
    private String appType;

    /**
     * TPP app OAuth 2.0 redirect URIs.
     */
    private String[] redirectUris;

    /**
     * TPP app OAuth 2.0 scopes.
     */
    private String[] scopes;

    /**
     * TPP name (company name).
     */
    private String tppName;

    /**
     * TPP license number.
     */
    private String tppLicense;

    /**
     * TPP website.
     */
    private String tppWebsite;

    /**
     * TPP address.
     */
    private String tppAddress;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
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

    public String getTppWebsite() {
        return tppWebsite;
    }

    public void setTppWebsite(String tppWebsite) {
        this.tppWebsite = tppWebsite;
    }

    public String getTppAddress() {
        return tppAddress;
    }

    public void setTppAddress(String tppAddress) {
        this.tppAddress = tppAddress;
    }

    public String[] getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(String[] redirectUris) {
        this.redirectUris = redirectUris;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }
}
