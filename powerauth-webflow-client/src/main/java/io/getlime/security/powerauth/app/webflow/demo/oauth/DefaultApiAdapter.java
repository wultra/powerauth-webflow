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

package io.getlime.security.powerauth.app.webflow.demo.oauth;

import io.getlime.security.powerauth.app.webflow.demo.model.UserResponse;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;

/**
 * Default API Adapter for provided OAuth 2.0 protected service.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class DefaultApiAdapter<T extends DefaultApiBinding> implements ApiAdapter<T> {

    @Override
    public boolean test(T api) {
        api.getProfile();
        return true;
    }

    @Override
    public void setConnectionValues(T api, ConnectionValues values) {
        UserResponse userResponse = api.getProfile();
        if (userResponse != null) {
            values.setProviderUserId(userResponse.getUser().getId());
            values.setDisplayName(userResponse.getUser().getId());
        }
    }

    @Override
    public UserProfile fetchUserProfile(T api) {
        UserResponse userResponse = api.getProfile();
        if (userResponse == null) {
            return null;
        }
        return new UserProfileBuilder()
                .setUsername(userResponse.getUser().getId())
                .setFirstName(userResponse.getUser().getGivenName())
                .setLastName(userResponse.getUser().getFamilyName())
                .build();
    }

    @Override
    public void updateStatus(T api, String message) {
    }
}
