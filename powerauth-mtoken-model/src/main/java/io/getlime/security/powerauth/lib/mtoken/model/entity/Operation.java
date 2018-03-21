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
    private String data;
    private Date operationCreated;
    private Date operationExpires;
    private AllowedSignatureType allowedSignatureType;
    private FormData formData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getOperationCreated() {
        return operationCreated;
    }

    public void setOperationCreated(Date operationCreated) {
        this.operationCreated = operationCreated;
    }

    public Date getOperationExpires() {
        return operationExpires;
    }

    public void setOperationExpires(Date operationExpires) {
        this.operationExpires = operationExpires;
    }

    public AllowedSignatureType getAllowedSignatureType() {
        return allowedSignatureType;
    }

    public void setAllowedSignatureType(AllowedSignatureType allowedSignatureType) {
        this.allowedSignatureType = allowedSignatureType;
    }

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
