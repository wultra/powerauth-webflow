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
package io.getlime.security.powerauth.app.webauth.repository.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Models a session for communication via Web Sockets.
 *
 * @author Roman Strobl
 */
@Data
@Entity
public class Session {

    private @Id
    @GeneratedValue
    Long id;

    private String sessionId;

    /**
     * Empty constructor.
     */
    public Session() {
    }

    /**
     * Parametrized constructor with sessionId.
     *
     * @param sessionId websocket sessionId
     */
    public Session(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * String representation of the session for logging.
     * @return String representation
     */
    public String toString() {
        return sessionId;
    }

}
