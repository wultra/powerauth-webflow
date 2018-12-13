/*
 * Copyright 2017 Wultra s.r.o.
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
 * Request for online token signature verification.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationApproveRequest {

    private String id;
    private String data;

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
}
