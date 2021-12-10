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
package io.getlime.security.powerauth.lib.mtoken.model.entity;

import java.util.Date;

/**
 * Model class representing an operation to be authorized.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class Operation {

    private String id;
    private String name;
    private String data;
    private Date operationCreated;
    private Date operationExpires;
    private AllowedSignatureType allowedSignatureType;
    private FormData formData;

    /**
     * Get operation ID.
     * @return Operation ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set operation ID.
     * @param id Operation ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get operation name.
     * @return Operation name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set operation name.
     *
     * @param name Operation name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get unstructured operation data.
     * @return Operation data.
     */
    public String getData() {
        return data;
    }

    /**
     * Set unstructured operation data.
     * @param data Operation data.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get timestamp when operation was created.
     * @return Timestamp when operation was created.
     */
    public Date getOperationCreated() {
        return operationCreated;
    }

    /**
     * Set timestamp when operation was created.
     * @param operationCreated Timestamp when opereration was created.
     */
    public void setOperationCreated(Date operationCreated) {
        this.operationCreated = operationCreated;
    }

    /**
     * Get timestamp when operation expires.
     * @return Timestamp when operation expires.
     */
    public Date getOperationExpires() {
        return operationExpires;
    }

    /**
     * Set timestamp when operation expires.
     * @param operationExpires Timestamp when operation expires.
     */
    public void setOperationExpires(Date operationExpires) {
        this.operationExpires = operationExpires;
    }

    /**
     * Get allowed signature type.
     * @return Allowed signature type.
     */
    public AllowedSignatureType getAllowedSignatureType() {
        return allowedSignatureType;
    }

    /**
     * Set allowed signature type.
     * @param allowedSignatureType Allowed signature type.
     */
    public void setAllowedSignatureType(AllowedSignatureType allowedSignatureType) {
        this.allowedSignatureType = allowedSignatureType;
    }

    /**
     * Get operation form data.
     * @return Operation form data.
     */
    public FormData getFormData() {
        return formData;
    }

    /**
     * Set operation form data.
     * @param formData Operation form data.
     */
    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
