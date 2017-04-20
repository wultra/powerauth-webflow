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

package io.getlime.security.powerauth.lib.webauth.resource.controller;

import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClientErrorException;
import io.getlime.security.powerauth.lib.credentials.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.webauth.resource.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping("/api/secure/user")
public class UserProfileController {

    @Autowired
    private CredentialStoreClient client;

    @RequestMapping("me")
    public @ResponseBody User me(Principal principal) {
        try {
            final Response<UserDetailResponse> userDetailResponse = client.fetchUserDetail(principal.getName());
            UserDetailResponse userDetail = userDetailResponse.getResponseObject();
            User user = new User();
            user.setId(userDetail.getId());
            user.setGivenName(userDetail.getGivenName());
            user.setFamilyName(userDetail.getFamilyName());
            return user;
        } catch (CredentialStoreClientErrorException e) {
            // Return dummy user
            User user = new User();
            user.setId("anonymousUser");
            user.setGivenName(null);
            user.setFamilyName(null);
            return user;
        }
    }

}
