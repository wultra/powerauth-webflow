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

import io.getlime.security.powerauth.app.tppengine.model.entity.GivenConsentHistory;

import java.util.List;

/**
 * Class representing a response of the consent history for given user.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ConsentHistoryListResponse {

    /**
     * User ID.
     */
    private String userId;

    /**
     * List of history items related to user consents.
     */
    private List<GivenConsentHistory> history;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<GivenConsentHistory> getHistory() {
        return history;
    }

    public void setHistory(List<GivenConsentHistory> history) {
        this.history = history;
    }
}
