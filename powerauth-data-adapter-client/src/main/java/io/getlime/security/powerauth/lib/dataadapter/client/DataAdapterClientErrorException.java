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

package io.getlime.security.powerauth.lib.dataadapter.client;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.DataAdapterError;

/**
 * Exception thrown from the data adapter in case of an error.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class DataAdapterClientErrorException extends Exception {

    private DataAdapterError error;

    public DataAdapterClientErrorException() {
    }

    public DataAdapterClientErrorException(Throwable cause) {
        super(cause);
    }

    public DataAdapterClientErrorException(Throwable cause, DataAdapterError error) {
        super(cause);
        this.error = error;
    }

    public DataAdapterError getError() {
        return error;
    }
}
