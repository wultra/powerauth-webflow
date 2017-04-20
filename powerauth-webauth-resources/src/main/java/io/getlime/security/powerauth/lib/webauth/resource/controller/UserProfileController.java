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
