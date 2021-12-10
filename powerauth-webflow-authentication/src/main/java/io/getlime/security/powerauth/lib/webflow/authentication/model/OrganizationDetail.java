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
package io.getlime.security.powerauth.lib.webflow.authentication.model;

/**
 * Object used for storing information about organizations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OrganizationDetail {

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
