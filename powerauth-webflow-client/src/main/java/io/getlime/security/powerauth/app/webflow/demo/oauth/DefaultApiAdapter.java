/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

package io.getlime.security.powerauth.app.webflow.demo.oauth;

import io.getlime.security.powerauth.app.webflow.demo.model.User;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;

/**
 * Default API Adapter for provided OAuth 2.0 protected service.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class DefaultApiAdapter<T extends DefaultApiBinding> implements ApiAdapter<T> {

    @Override
    public boolean test(T api) {
        api.getProfile();
        return true;
    }

    @Override
    public void setConnectionValues(T api, ConnectionValues values) {
        User user = api.getProfile();
        values.setProviderUserId(user.getId());
        values.setDisplayName(user.getGivenName() + " " + user.getFamilyName());
    }

    @Override
    public UserProfile fetchUserProfile(T api) {
        User user = api.getProfile();
        return new UserProfileBuilder()
                .setUsername(user.getId())
                .setFirstName(user.getGivenName())
                .setLastName(user.getFamilyName())
                .build();
    }

    @Override
    public void updateStatus(T api, String message) {
    }
}
