/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.model.response;

/**
 * Response to registration of a new WebSocket session.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class WebSocketRegistrationResponse {

    private String webSocketId;
    private boolean registrationSucceeded;

    /**
     * Get Web Socket ID.
     * @return Web Socket ID.
     */
    public String getWebSocketId() {
        return webSocketId;
    }

    /**
     * Set Web Socket ID.
     * @param webSocketId Web Socket ID.
     */
    public void setWebSocketId(String webSocketId) {
        this.webSocketId = webSocketId;
    }

    /**
     * Get whether Web Socket registration succeeded.
     * @return Whether Web Socket registration succeeded.
     */
    public boolean isRegistrationSucceeded() {
        return registrationSucceeded;
    }

    /**
     * Set whether Web Socket registration succeeded.
     * @param registrationSucceeded Whether Web Socket registration succeeded.
     */
    public void setRegistrationSucceeded(boolean registrationSucceeded) {
        this.registrationSucceeded = registrationSucceeded;
    }
}
