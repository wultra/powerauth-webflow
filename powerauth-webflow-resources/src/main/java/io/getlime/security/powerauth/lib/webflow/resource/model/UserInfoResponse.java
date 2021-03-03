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

package io.getlime.security.powerauth.lib.webflow.resource.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Minimal OIDC UserInfo response, as defined in OpenID Connect specification.
 * See https://openid.net/specs/openid-connect-core-1_0.html#UserInfo for details.
 *
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

    public UserInfoResponse() {
    }

    /**
     * Constructor with the minimal attribute set.
     * @param id Identifier of the subject, mapped to id.
     * @param sub Identifier of the subject, mapped to sub.
     * @param givenName Given name of the subject.
     * @param familyName Family name of the subject.
     */
    public UserInfoResponse(String id, String sub, String givenName, String familyName) {
        this.id = id;
        this.sub = sub;
        this.givenName = givenName;
        this.familyName = familyName;
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
}
