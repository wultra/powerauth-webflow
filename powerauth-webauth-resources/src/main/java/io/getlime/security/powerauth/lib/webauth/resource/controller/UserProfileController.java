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

import io.getlime.security.powerauth.lib.webauth.resource.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping("/api/secure/user")
public class UserProfileController {

    @RequestMapping("me")
    public @ResponseBody User me() {
        User user = new User();
        user.setId("1234567890");
        user.setGivenName("John");
        user.setFamilyName("Doe");
        return user;
    }

}
