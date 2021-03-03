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

package io.getlime.security.powerauth.lib.nextstep.client;

import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.NextStepError;

/**
 * Class representing a Next Step client exception created when calling the Next Step REST API.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepClientException extends Exception {

    private Error error;

    /**
     * Default constructor.
     */
    public NextStepClientException() {
        super();
        this.error = new NextStepError();
    }

    /**
     * Constructor with error.
     * @param error Error.
     */
    public NextStepClientException(Error error) {
        super(error != null ? error.getMessage() : null);
        this.error = error;
    }

    /**
     * Constructor with message and error.
     * @param message Message.
     * @param error Error.
     */
    public NextStepClientException(String message, Error error) {
        super(message);
        this.error = error;
    }

    /**
     * Constructor with message, cause and error.
     * @param message Message.
     * @param cause Cause.
     * @param error Error.
     */
    public NextStepClientException(String message, Throwable cause, Error error) {
        super(message, cause);
        this.error = error;
    }

    /**
     * Constructor with cause and error.
     * @param cause Cause.
     * @param error Error.
     */
    public NextStepClientException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    /**
     * Constructor with all details.
     * @param message Message.
     * @param cause Cause.
     * @param enableSuppression Whether suppression is enabled.
     * @param writableStackTrace Writeable stacktrace.
     * @param error Error.
     */
    public NextStepClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Error error) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
    }

    /**
     * Set the error.
     * @param error Error.
     */
    public void setError(Error error) {
        this.error = error;
    }

    /**
     * Get the error.
     * @return Error.
     */
    public Error getError() {
        return error;
    }

    /**
     * Get the original Next Step error.
     * @return Original Next Step error.
     */
    public Error getNextStepError() {
        Throwable cause = getCause();
        if (!(cause instanceof RestClientException)) {
            return null;
        }
        RestClientException ex = (RestClientException) cause;
        if (ex.getErrorResponse() == null) {
            return null;
        }
        return ex.getErrorResponse().getResponseObject();
    }

}