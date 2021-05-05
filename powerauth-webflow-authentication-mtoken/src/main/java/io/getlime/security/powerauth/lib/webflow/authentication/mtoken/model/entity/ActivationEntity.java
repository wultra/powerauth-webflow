/*
 * Copyright 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity;

/**
 * Activation entity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ActivationEntity {

    private String activationId;
    private String activationName;
    private String timestampLastUsed;

    /**
     * Get activation ID.
     * @return Activation ID.
     */
    public String getActivationId() {
        return activationId;
    }

    /**
     * Set activation ID.
     * @param activationId Activation ID.
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /**
     * Get activation name.
     * @return Activation name.
     */
    public String getActivationName() {
        return activationName;
    }

    /**
     * Set activation name.
     * @param activationName Activation name.
     */
    public void setActivationName(String activationName) {
        this.activationName = activationName;
    }

    /**
     * Get last used timestamp.
     * @return Last used timestamp.
     */
    public String getTimestampLastUsed() {
        return timestampLastUsed;
    }

    /**
     * Set last used timestamp.
     * @param timestampLastUsed Last used timestamp.
     */
    public void setTimestampLastUsed(String timestampLastUsed) {
        this.timestampLastUsed = timestampLastUsed;
    }
}
