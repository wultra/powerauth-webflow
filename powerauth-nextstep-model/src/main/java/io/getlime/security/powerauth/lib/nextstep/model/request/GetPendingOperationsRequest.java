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
package io.getlime.security.powerauth.lib.nextstep.model.request;

/**
 * Request object used for querying of pending operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetPendingOperationsRequest {

    private String userId;
    private boolean mobileTokenOnly;

    /**
     * Get the user id.
     *
     * @return user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user id.
     *
     * @param userId user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get whether pending operation list should be filtered for only next step with mobile token support.
     * @return Whether pending operation list should be filtered for only next step with mobile token support.
     */
    public boolean isMobileTokenOnly() {
        return mobileTokenOnly;
    }

    /**
     * Set whether pending operation list should be filtered for only next step with mobile token support.
     * @param mobileTokenOnly Whether pending operation list should be filtered for only next step with mobile token support.
     */
    public void setMobileTokenOnly(boolean mobileTokenOnly) {
        this.mobileTokenOnly = mobileTokenOnly;
    }
}
