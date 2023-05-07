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
     * TPP app OAuth 2.1 redirect URIs.
     */
    private String[] redirectUris;

    /**
     * TPP app OAuth 2.1 scopes.
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
