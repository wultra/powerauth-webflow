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
package io.getlime.security.powerauth.app.webauth.controller;

import io.getlime.security.powerauth.lib.webauth.authentication.service.AuthenticationManagementService;
import io.getlime.security.powerauth.app.webauth.configuration.WebAuthServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Simple controller, redirects to the main HTML page with JavaScript content.
 *
 * @author Roman Strobl
 */
@Controller
public class HomeController {

    private WebAuthServerConfiguration webAuthConfig;
    private AuthenticationManagementService authenticationManagementService;

    /**
     * Initialization of the HomeController with application WebAuthServicesConfiguration.
     * @param webAuthConfig WebAuthServicesConfiguration of the application
     */
    @Autowired
    public HomeController(AuthenticationManagementService authenticationManagementService, WebAuthServerConfiguration webAuthConfig) {
        this.webAuthConfig = webAuthConfig;
        this.authenticationManagementService = authenticationManagementService;
    }

    @RequestMapping("/")
    public String index() {
        return "redirect:/authenticate";
    }

    /**
     * Renders the index.jsp template file
     * @param model Model is used to store a link to the stylesheet which can be externalized
     * @return index page
     * @throws Exception thrown when page is not found
     */
    @RequestMapping("/authenticate")
    public String authenticate(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        if (savedRequest == null) {
            return "redirect:/oauth/error";
        }

        authenticationManagementService.clearContext();
        model.put("title", webAuthConfig.getPageTitle());
        model.put("stylesheet", webAuthConfig.getStylesheetUrl());
        return "index";
    }

    /**
     * Redirects user to previous URL after authentication, or to error URL in case of broken OAuth dance.
     * @param request Reference to current HttpServletRequest.
     * @param response Reference to current HttpServletResponse.
     */
    @RequestMapping("/continue")
    public String continueToRedirect(HttpServletRequest request, HttpServletResponse response) {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        String redirectUrl;
        if (savedRequest == null) {
            StringBuffer url = request.getRequestURL();
            String uri = request.getRequestURI();
            String ctx = request.getContextPath();
            String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
            return "redirect:/oauth/error";
        } else {
            authenticationManagementService.pendingAuthenticationToAuthentication();
            redirectUrl = savedRequest.getRedirectUrl();
        }
        response.setHeader("Location", redirectUrl);
        response.setStatus(HttpServletResponse.SC_FOUND);
        return null;
    }

}
