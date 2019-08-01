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
 * Response object used for querying an organization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetOrganizationDetailResponse {

    private String organizationId;
    private String displayNameKey;
    private boolean isDefault;
    private int orderNumber;

    /**
     * Get organization ID.
     * @return Organization ID.
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set organization ID.
     * @param organizationId Organization ID.
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Get display name key.
     * @return Display name key.
     */
    public String getDisplayNameKey() {
        return displayNameKey;
    }

    /**
     * Set display name key.
     * @param displayNameKey Display name key.
     */
    public void setDisplayNameKey(String displayNameKey) {
        this.displayNameKey = displayNameKey;
    }

    /**
     * Get whether the organization is the default organization.
     * @return Whether the organization is the default organization.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Set whether the organization is the default organization.
     * @param isDefault Whether the organization is the default organization.
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Get the order number.
     * @return Order number.
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * Set the order number.
     * @param orderNumber Order number.
     */
    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}
