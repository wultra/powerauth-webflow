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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Organization entity which adds organization context to operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_organization")
public class OrganizationEntity implements Serializable {

    private static final long serialVersionUID = -3682348562614758414L;

    @Id
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Column(name = "display_name_key")
    private String displayNameKey;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "order_number", nullable = false)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationEntity that = (OrganizationEntity) o;
        return Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(displayNameKey, that.displayNameKey) &&
                Objects.equals(isDefault, that.isDefault) &&
                Objects.equals(orderNumber, that.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, displayNameKey, isDefault, orderNumber);
    }
}
