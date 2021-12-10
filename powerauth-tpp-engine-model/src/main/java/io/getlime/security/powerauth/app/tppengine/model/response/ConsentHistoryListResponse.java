/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
