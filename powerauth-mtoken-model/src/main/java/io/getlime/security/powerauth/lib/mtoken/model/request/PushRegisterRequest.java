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
package io.getlime.security.powerauth.lib.mtoken.model.request;

/**
 * Request to register for sending push messages.
 * @author Petr Dvorak, petr@wultra.com
 */
public class PushRegisterRequest {

    private String platform;
    private String token;

    /**
     * Get mobile platform - "ios" / "android".
     * @return Mobile platform.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Set mobile platform - "ios" / "android".
     * @param platform Mobile platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Get push registration token.
     * @return Push registration token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Set push registration token.
     * @param token Push registration token.
     */
    public void setToken(String token) {
        this.token = token;
    }
}
