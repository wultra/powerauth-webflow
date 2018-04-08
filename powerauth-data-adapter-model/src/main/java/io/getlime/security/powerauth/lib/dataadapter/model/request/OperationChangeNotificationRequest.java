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

import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;

/**
 * Request object for notifying data adapter about operation change.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationChangeNotificationRequest {

    private String userId;
    private OperationContext operationContext;
    private OperationChange operationChange;

    /**
     * Default constructor
     */
    public OperationChangeNotificationRequest() {
    }

    /**
     * Constructor with user ID, operation ID and form data change.
     * @param userId User ID.
     * @param operationChange Operation change request object.
     * @param operationContext Operation context.
     */
    public OperationChangeNotificationRequest(String userId, OperationChange operationChange, OperationContext operationContext) {
        this.userId = userId;
        this.operationChange = operationChange;
        this.operationContext = operationContext;
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
     * Get operation context.
     * @return Operation context
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context.
     * @param operationContext Operation context.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    /**
     * Get operation change.
     * @return Operation change.
     */
    public OperationChange getOperationChange() {
        return operationChange;
    }

    /**
     * Set operation change.
     * @param operationChange Operation change.
     */
    public void setOperationChange(OperationChange operationChange) {
        this.operationChange = operationChange;
    }
}
