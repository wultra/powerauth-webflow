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
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.webflow.resource.configuration.WebFlowResourcesServerConfiguration;
import io.getlime.security.powerauth.lib.webflow.resource.model.UserInfoResponse;
import io.getlime.security.powerauth.lib.webflow.resource.model.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private static final String ORGANIZATION_ID = "organization_id";

    private static final String ANONYMOUS_USER = "anonymousUser";

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    public UserProfileController(DataAdapterClient client, AuthorizationServerTokenServices tokenServices, WebFlowResourcesServerConfiguration webFlowResourcesServerConfiguration) {
        this.client = client;
        this.tokenServices = tokenServices;
        this.webFlowResourcesServerConfiguration = webFlowResourcesServerConfiguration;
    }

    /**
     * Returns user profile of authenticated user, or anonymous user in case there is an error fetching user details.
     * This endpoint is specifically designed to return additional context information related to PSD2/SCA process,
     * such as info about if SCA (Strong Customer Authentication) was used or not, or about a language used to finalize
     * the flow.
     *
     * @param authentication Original authentication of the currently logged user.
     * @return User profile.
     */
    @RequestMapping(value = "me", method = RequestMethod.GET)
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

            if (userDetailResponse.getResponseObject().getAccountStatus() != AccountStatus.ACTIVE) {
                // Return dummy user in case user account is not ACTIVE
                return anonymousUser();
            }

            UserDetailResponse userDetail = userDetailResponse.getResponseObject();
            userResponse.getUser().setId(userDetail.getId());
            userResponse.getUser().setGivenName(userDetail.getGivenName());
            userResponse.getUser().setFamilyName(userDetail.getFamilyName());
            userResponse.getConnection().setLanguage(language);
            userResponse.getConnection().setSca(sca);

            // In case Data Adapter translated organization ID, use the translated value,
            // otherwise use the organization ID that is assigned to the access token.
            if (userDetail.getOrganizationId() != null && !userDetail.getOrganizationId().isEmpty()) {
                organizationId = userDetail.getOrganizationId();
            }
            userResponse.getConnection().setOrganizationId(organizationId);
        } catch (DataAdapterClientErrorException e) {
            return anonymousUser();
        }
        // Save service information
        userResponse.getService().setApplicationName(webFlowResourcesServerConfiguration.getApplicationName());
        userResponse.getService().setApplicationDisplayName(webFlowResourcesServerConfiguration.getApplicationDisplayName());
        userResponse.getService().setApplicationEnvironment(webFlowResourcesServerConfiguration.getApplicationEnvironment());
        userResponse.getService().setTimestamp(new Date());

        // Return response
        return userResponse;
    }

    /**
     * Returns user profile of authenticated user, or anonymous user in case there is an error fetching user details.
     * This method returns a minimal format compatible with OpenID Connect specification (basic JWT claims).
     *
     * @param authentication Original authentication of the currently logged user.
     * @return User profile.
     */
    @RequestMapping(value = "me/info", method = { RequestMethod.GET, RequestMethod.POST })
    public @ResponseBody UserInfoResponse userInfo(OAuth2Authentication authentication) {
        // Try to fetch user details from the service
        try {
            final String usedId = authentication.getUserAuthentication().getName();
            // Get additional information stored with the token
            final Map<String, Object> additionalInfo = tokenServices.getAccessToken(authentication).getAdditionalInformation();
            final String organizationId = (String) additionalInfo.get(ORGANIZATION_ID);
            logger.info("Fetching user details for user with ID: {}, organization ID: {}", usedId, organizationId);
            final ObjectResponse<UserDetailResponse> userDetail = client.fetchUserDetail(usedId, organizationId);
            if (userDetail.getResponseObject().getAccountStatus() != AccountStatus.ACTIVE) {
                return new UserInfoResponse(ANONYMOUS_USER, null, null, null);
            }
            final UserDetailResponse user = userDetail.getResponseObject();
            final String id = user.getId();
            final String givenName = user.getGivenName();
            final String familyName = user.getFamilyName();
            logger.info("Found user with ID: {}, given name: {}, family name: {}", usedId, givenName, familyName);
            return new UserInfoResponse(id, id, givenName, familyName);
        } catch (DataAdapterClientErrorException e) {
            throw new UnauthorizedUserException("Unable to fetch user details from data adapter");
        }
    }

    /**
     * Create dummy user for case when user does not exist or user account is not active.
     * @return Dummy user response.
     */
    private UserResponse anonymousUser() {
        UserResponse userResponse = new UserResponse();
        userResponse.getUser().setId(ANONYMOUS_USER);
        userResponse.getUser().setGivenName(null);
        userResponse.getUser().setFamilyName(null);
        userResponse.getConnection().setLanguage("en");
        userResponse.getConnection().setSca(false);
        userResponse.getConnection().setOrganizationId(null);
        return userResponse;
    }

}
