/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.tpp.engine.client;

import io.getlime.security.powerauth.app.tppengine.model.entity.TppEngineError;

/**
 * Exception thrown from the data adapter in case of an error.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppEngineClientException extends Exception {

    private TppEngineError error;

    /**
     * Default constructor.
     */
    public TppEngineClientException() {
    }

    /**
     * Constructor with cause.
     * @param cause Exception cause.
     */
    public TppEngineClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error.
     * @param cause Exception cause.
     * @param error Data adapter error.
     */
    public TppEngineClientException(Throwable cause, TppEngineError error) {
        super(cause);
        this.error = error;
    }

    /**
     * Get data adapter error.
     * @return Data adapter error.
     */
    public TppEngineError getError() {
        return error;
    }
}