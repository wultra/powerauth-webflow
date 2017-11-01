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

package io.getlime.security.powerauth.lib.webflow.resource.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.webflow.resource.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;

/**
 * Controller for providing information about user profiles.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping("/api/secure/profile")
public class UserProfileController {

    private final DataAdapterClient client;
    private final AuthorizationServerTokenServices tokenServices;

    private static final String LANGUAGE = "language";
    private static final String SCA = "sca";

    @Autowired
    public UserProfileController(DataAdapterClient client, AuthorizationServerTokenServices tokenServices) {
        this.client = client;
        this.tokenServices = tokenServices;
    }

    /**
     * Returns user profile of authenticated user, or anonymous user in case there is an error fetching user details.
     *
     * @param principal Currently logged user.
     * @return User profile.
     */
    @RequestMapping("me")
    public @ResponseBody UserResponse me(Principal principal, OAuth2Authentication authentication) {
        try {
            final ObjectResponse<UserDetailResponse> userDetailResponse = client.fetchUserDetail(principal.getName());

            Map<String, Object> additionalInfo = tokenServices.getAccessToken(authentication).getAdditionalInformation();

            UserDetailResponse userDetail = userDetailResponse.getResponseObject();
            UserResponse user = new UserResponse();
            user.getUser().setId(userDetail.getId());
            user.getUser().setGivenName(userDetail.getGivenName());
            user.getUser().setFamilyName(userDetail.getFamilyName());
            user.getConnection().setLanguage((String) additionalInfo.get(LANGUAGE));
            user.getConnection().setSca((Boolean) additionalInfo.get(SCA));
            return user;
        } catch (DataAdapterClientErrorException e) {
            // Return dummy user
            UserResponse user = new UserResponse();
            user.getUser().setId("anonymousUser");
            user.getUser().setGivenName(null);
            user.getUser().setFamilyName(null);
            user.getConnection().setLanguage("CZ");
            user.getConnection().setSca(false);
            return user;
        }
    }

}
