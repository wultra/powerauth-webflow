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
package io.getlime.security.powerauth.lib.nextstep.model.base;

/**
 * Class representing the base request object.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class Request<T> {

    private T requestObject;

    /**
     * Default constructor.
     */
    public Request() {
    }

    /**
     * Constructor with the request object.
     * @param requestObject Request object of a provided type.
     */
    public Request(T requestObject) {
        this.requestObject = requestObject;
    }

    /**
     * Get request object.
     * @return Request object.
     */
    public T getRequestObject() {
        return requestObject;
    }

    /**
     * Set request object.
     * @param requestObject Request object.
     */
    public void setRequestObject(T requestObject) {
        this.requestObject = requestObject;
    }
}
