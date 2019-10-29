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
package io.getlime.security.powerauth.lib.nextstep.model.response;

/**
 * Response object used obtaining mobile token configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetMobileTokenConfigResponse {

    private boolean mobileTokenEnabled;

    /**
     * Get whether mobile token is enabled.
     * @return Whether mobile token is enabled.
     */
    public boolean isMobileTokenEnabled() {
        return mobileTokenEnabled;
    }

    /**
     * Set whether mobile token is enabled.
     * @param mobileTokenEnabled Whether mobile token is enabled.
     */
    public void setMobileTokenEnabled(boolean mobileTokenEnabled) {
        this.mobileTokenEnabled = mobileTokenEnabled;
    }
}
