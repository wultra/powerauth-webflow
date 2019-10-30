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
