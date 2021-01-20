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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Extras for application context for OAuth 2.0 consent screen.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ApplicationExtras {

    private final List<String> requestedScopes;
    private String applicationOwner;

    /**
     * Default constructor.
     */
    public ApplicationExtras() {
        requestedScopes = new ArrayList<>();
    }

    /**
     * Constructor with all details.
     * @param requestedScopes Original requested scopes for OAuth 2.0 consent screen.
     * @param applicationOwner Entity who created the application requesting consent.
     */
    public ApplicationExtras(List<String> requestedScopes, String applicationOwner) {
        this.requestedScopes = requestedScopes;
        this.applicationOwner = applicationOwner;
    }

    /**
     * Get original requested scopes for OAuth 2.0 consent screen.
     * @return Original requested scopes for OAuth 2.0 consent screen.
     */
    public List<String> getRequestedScopes() {
        return requestedScopes;
    }

    /**
     * Get entity who created the application requesting consent.
     * @return Entity who created the application requesting consent.
     */
    public String getApplicationOwner() {
        return applicationOwner;
    }

    /**
     * Set entity who created the application requesting consent.
     * @param applicationOwner Entity who created the application requesting consent.
     */
    public void setApplicationOwner(String applicationOwner) {
        this.applicationOwner = applicationOwner;
    }
}
