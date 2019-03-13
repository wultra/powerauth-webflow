/*
 * Copyright 2017 Wultra s.r.o.
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
import io.getlime.security.powerauth.lib.webflow.resource.configuration.WebFlowResourcesServerConfiguration;
import io.getlime.security.powerauth.lib.webflow.resource.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Map;

/**
 * Controller for providing information about user profiles.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping("/api/secure/profile")
public class UserProfileController {

    private final DataAdapterClient client;
    private final AuthorizationServerTokenServices tokenServices;
    private final WebFlowResourcesServerConfiguration webFlowResourcesServerConfiguration;

    private static final String LANGUAGE = "language";
    private static final String SCA = "sca";
    private static final String ORGANIZATION_ID = "organizationId";

    @Autowired
    public UserProfileController(DataAdapterClient client, AuthorizationServerTokenServices tokenServices, WebFlowResourcesServerConfiguration webFlowResourcesServerConfiguration) {
        this.client = client;
        this.tokenServices = tokenServices;
        this.webFlowResourcesServerConfiguration = webFlowResourcesServerConfiguration;
    }

    /**
     * Returns user profile of authenticated user, or anonymous user in case there is an error fetching user details.
     *
     * @param authentication Original authentication of the currently logged user.
     * @return User profile.
     */
    @RequestMapping("me")
    public @ResponseBody UserResponse me(OAuth2Authentication authentication) {
        UserResponse userResponse = new UserResponse();

        // Try to fetch user details from the service
        try {
            // Get additional information stored with the token
            Map<String, Object> additionalInfo = tokenServices.getAccessToken(authentication).getAdditionalInformation();
            String language = (String) additionalInfo.get(LANGUAGE);
            Boolean sca = (Boolean) additionalInfo.get(SCA);
            String organizationId = (String) additionalInfo.get(ORGANIZATION_ID);

            final ObjectResponse<UserDetailResponse> userDetailResponse = client.fetchUserDetail(authentication.getUserAuthentication().getName(), organizationId);

            UserDetailResponse userDetail = userDetailResponse.getResponseObject();
            userResponse.getUser().setId(userDetail.getId());
            userResponse.getUser().setGivenName(userDetail.getGivenName());
            userResponse.getUser().setFamilyName(userDetail.getFamilyName());
            userResponse.getConnection().setLanguage(language);
            userResponse.getConnection().setSca(sca);
            userResponse.getConnection().setOrganizationId(organizationId);
        } catch (DataAdapterClientErrorException e) {
            // Return dummy user
            userResponse.getUser().setId("anonymousUser");
            userResponse.getUser().setGivenName(null);
            userResponse.getUser().setFamilyName(null);
            userResponse.getConnection().setLanguage("en");
            userResponse.getConnection().setSca(false);
            userResponse.getConnection().setOrganizationId(null);
        }
        // Save service information
        userResponse.getService().setApplicationName(webFlowResourcesServerConfiguration.getApplicationName());
        userResponse.getService().setApplicationDisplayName(webFlowResourcesServerConfiguration.getApplicationDisplayName());
        userResponse.getService().setApplicationEnvironment(webFlowResourcesServerConfiguration.getApplicationEnvironment());
        userResponse.getService().setTimestamp(new Date());

        // Return response
        return userResponse;
    }

}
