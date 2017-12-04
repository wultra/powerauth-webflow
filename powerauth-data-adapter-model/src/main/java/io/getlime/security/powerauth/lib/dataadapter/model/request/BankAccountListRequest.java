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

package io.getlime.security.powerauth.lib.dataadapter.model.request;

/**
 * Request object for obtaining bank accounts by user ID.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class BankAccountListRequest {

    private String userId;
    private String operationName;
    private String operationId;

    /**
     * Default constructor
     */
    public BankAccountListRequest() {
    }

    /**
     * Constructor with user ID as a parameter.
     * @param userId User ID.
     * @param operationName Operation name.
     * @param operationId Operation ID.
     */
    public BankAccountListRequest(String userId, String operationName, String operationId) {
        this.userId = userId;
        this.operationName = operationName;
        this.operationId = operationId;
    }

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID.
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get operation name.
     * @return Operation name.
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set operation name.
     * @param operationName Operation name.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
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
}
