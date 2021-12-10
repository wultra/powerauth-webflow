/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
