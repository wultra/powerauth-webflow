/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.mtoken.model.entity;

import java.util.Date;

/**
 * Model class representing an operation to be authorized.
 *
 * @author Petr Dvorak, petr@lime-company.eu
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
