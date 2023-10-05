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

package io.getlime.security.powerauth.lib.webflow.resource.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal OIDC UserInfo response, as defined in OpenID Connect specification.
 * See <a href="https://openid.net/specs/openid-connect-core-1_0.html#UserInfo">OpenID Connect User Info</a> for details.
 * <p>
 * TODO: implement support for generic parameters in extras
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UserInfoResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    @JsonProperty("extras")
    private final Map<String, Object> extras = new LinkedHashMap<>();

    public UserInfoResponse() {
    }

    /**
     * Constructor with the minimal attribute set.
     * @param id Identifier of the subject, mapped to id.
     * @param sub Identifier of the subject, mapped to sub.
     * @param givenName Given name of the subject.
     * @param familyName Family name of the subject.
     * @param extras Extra attributes related to user identity.
     */
    public UserInfoResponse(String id, String sub, String givenName, String familyName, Map<String, Object> extras) {
        this.id = id;
        this.sub = sub;
        this.givenName = givenName;
        this.familyName = familyName;
        if (extras != null) {
            this.extras.putAll(extras);
        }
    }

    /**
     * Get identifier of the subject.
     * @return Identifier of the subject.
     */
    public String getId() {
        return id;
    }

    /**
     * Set identifier of the subject.
     * @param id Identifier of the subject.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get identifier of the subject.
     * @return Identifier of the subject.
     */
    public String getSub() {
        return sub;
    }

    /**
     * Set identifier of the subject.
     * @param sub Identifier of the subject.
     */
    public void setSub(String sub) {
        this.sub = sub;
    }

    /**
     * Get subject given name.
     * @return Subject given name.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Set subject given name.
     * @param givenName Subject given name.
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * Get subject family name.
     * @return Subject family name.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Set subject family name.
     * @param familyName Subject family name.
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /**
     * Extra attributes related to user identity.
     * @return Get extra attributes related to user identity.
     */
    public Map<String, Object> getExtras() {
        return extras;
    }

}
