/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webauth.model.entity.registration;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;

/**
 * Models a registration request received from the client.
 *
 * @author Roman Strobl
 */
public class RegistrationRequest extends WebSocketJsonMessage {

    private boolean performUITest;

    /**
     * Empty constructor.
     */
    public RegistrationRequest() {
    }

    /**
     * Constructor with parameters for convenience.
     * @param performUITest whether to perform UI test
     */
    public RegistrationRequest(boolean performUITest) {
        this.action = WebAuthAction.REGISTER;
        this.performUITest = performUITest;
    }

    /**
     * Whether to perform the UI test.
     *
     * @return whether UI test should be performed
     */
    public boolean getPerformUITest() {
        return performUITest;
    }

    /**
     * String representation of the object for logging.
     * @return String representation
     */
    public String toString() {
        return "RegistrationRequest (performUITest=" + performUITest + ")";
    }

}
