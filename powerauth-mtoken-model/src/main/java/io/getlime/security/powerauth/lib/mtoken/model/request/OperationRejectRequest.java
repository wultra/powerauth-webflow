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
package io.getlime.security.powerauth.lib.mtoken.model.request;

/**
 * Request to cancel an operation.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationRejectRequest {

    private String id;
    private String reason;

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
     * Get reason why operation was rejected.
     * @return Reason why operation was rejected.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set reason why operation was rejected.
     * @param reason Reason why operation was rejected.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
