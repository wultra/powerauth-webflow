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

package io.getlime.security.powerauth.app.tppengine.model.response;

import io.getlime.security.powerauth.app.tppengine.model.entity.GivenConsent;

import java.util.List;

/**
 * Class representing currently given consents by a user.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ConsentListResponse {

    /**
     * User ID.
     */
    private String userId;

    /**
     * List of consents currently given by the user.
     */
    private List<GivenConsent> consents;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<GivenConsent> getConsents() {
        return consents;
    }

    public void setConsents(List<GivenConsent> consents) {
        this.consents = consents;
    }
}
