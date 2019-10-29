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
package io.getlime.security.powerauth.lib.nextstep.model.request;

/**
 * Request object used for updating mobile token status.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateMobileTokenRequest {

    private String operationId;
    private boolean mobileTokenActive;

    /**
     * Default constructor.
     */
    public UpdateMobileTokenRequest() {
    }

    /**
     * Constructor with mobile token status.
     * @param operationId Operation ID.
     * @param mobileTokenActive Whether mobile token is active.
     */
    public UpdateMobileTokenRequest(String operationId, boolean mobileTokenActive) {
        this.operationId = operationId;
        this.mobileTokenActive = mobileTokenActive;
    }

    /**
     * Get operation ID.
     * @return Operation ID.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set operation ID.
     * @param operationId Operation ID.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get whether mobile token is active.
     * @return Whether mobile token is active.
     */
    public boolean isMobileTokenActive() {
        return mobileTokenActive;
    }

    /**
     * Set whether mobile token is active.
     * @param mobileTokenActive Whether mobile token is active.
     */
    public void setMobileTokenActive(boolean mobileTokenActive) {
        this.mobileTokenActive = mobileTokenActive;
    }
}
