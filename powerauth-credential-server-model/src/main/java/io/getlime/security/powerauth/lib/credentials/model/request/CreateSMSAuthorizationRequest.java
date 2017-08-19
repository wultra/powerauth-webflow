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
package io.getlime.security.powerauth.lib.credentials.model.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Request for creating SMS OTP authorization message.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class CreateSMSAuthorizationRequest {

    /**
     * User ID for this authorization request.
     */
    private String userId;

    /**
     * Name of operation.
     */
    private String operationName;

    /**
     * Operation data in JSON format.
     */
    private JsonNode operationData;

    /**
     * Language used in the SMS OTP messages.
     */
    private String lang;

    public CreateSMSAuthorizationRequest() {
    }

    public CreateSMSAuthorizationRequest(String userId, String operationName, JsonNode operationData, String lang) {
        this.userId = userId;
        this.operationName = operationName;
        this.operationData = operationData;
        this.lang = lang;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public JsonNode getOperationData() {
        return operationData;
    }

    public void setOperationData(ObjectNode operationData) {
        this.operationData = operationData;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
