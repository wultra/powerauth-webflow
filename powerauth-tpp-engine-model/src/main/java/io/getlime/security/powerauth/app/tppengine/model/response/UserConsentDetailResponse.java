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

/**
 * Response object representing a given consent to TPP app by given user.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UserConsentDetailResponse {

    /**
     * User ID.
     */
    private String userId;

    /**
     * Information about given consent, or null in case a consent is not given by the user.
     */
    private GivenConsent consent;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public GivenConsent getConsent() {
        return consent;
    }

    public void setConsent(GivenConsent consent) {
        this.consent = consent;
    }
}
