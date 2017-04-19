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
 * Class representing the base response object.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class Response<T> {

    /**
     * Class with constants representing a response status.
     */
    public class Status {
        /**
         * OK response.
         */
        public static final String OK = "OK";

        /**
         * ERROR response.
         */
        public static final String ERROR = "ERROR";
    }

    private String status;
    private T responseObject;

    /**
     * Default constructor.
     */
    public Response() {
    }

    /**
     * Response constructor with status and response object of a provided type.
     * @param status Response status, use value from Request.Status class.
     * @param responseObject Response object.
     */
    public Response(String status, T responseObject) {
        this.status = status;
        this.responseObject = responseObject;
    }

    /**
     * Get response status.
     * @return Response status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set response status, use value from Request.Status class.
     * @param status Response status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get the response object.
     * @return Response object.
     */
    public T getResponseObject() {
        return responseObject;
    }

    /**
     * Set the response object.
     * @param responseObject Response object.
     */
    public void setResponseObject(T responseObject) {
        this.responseObject = responseObject;
    }
}
