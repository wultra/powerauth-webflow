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
package io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

/**
 * Model for an authentication response for SCA login.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class LoginScaAuthResponse extends AuthStepResponse {

    private boolean mobileTokenEnabled;

    /**
     * Get whether mobile token is enabled for this step.
     * @return Whether mobile token is enabled for this step.
     */
    public boolean isMobileTokenEnabled() {
        return mobileTokenEnabled;
    }

    /**
     * Set whether mobile token is enabled for this step.
     * @param mobileTokenEnabled Whether mobile token is enabled for this step.
     */
    public void setMobileTokenEnabled(boolean mobileTokenEnabled) {
        this.mobileTokenEnabled = mobileTokenEnabled;
    }

}
