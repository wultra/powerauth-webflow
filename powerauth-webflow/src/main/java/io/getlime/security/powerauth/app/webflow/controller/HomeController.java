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
package io.getlime.security.powerauth.app.webflow.controller;

import io.getlime.security.powerauth.app.webflow.configuration.WebFlowServerConfiguration;
import io.getlime.security.powerauth.app.webflow.i18n.I18NService;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple controller, redirects to the main HTML page with JavaScript content.
 *
 * @author Roman Strobl
 */
@Controller
public class HomeController {

    private final WebFlowServerConfiguration webFlowConfig;
    private final AuthenticationManagementService authenticationManagementService;
    private final I18NService i18nService;
    private final OperationSessionService operationSessionService;
    private final NextStepClient nextStepClient;

    /**
     * Initialization of the HomeController with application webflowServicesConfiguration.
     *
     * @param authenticationManagementService Authentication management service.
     * @param webFlowConfig WebFlowServicesConfiguration of the application.
     * @param i18nService I18n service.
     * @param operationSessionService Operation to session mapping service.
     * @param nextStepClient Next step client.
     */
    @Autowired
    public HomeController(AuthenticationManagementService authenticationManagementService, WebFlowServerConfiguration webFlowConfig, I18NService i18nService, OperationSessionService operationSessionService, NextStepClient nextStepClient) {
        this.webFlowConfig = webFlowConfig;
        this.authenticationManagementService = authenticationManagementService;
        this.i18nService = i18nService;
        this.operationSessionService = operationSessionService;
        this.nextStepClient = nextStepClient;
    }

    /**
     * Redirect from home page to /authenticate endpoint.
     *
     * @return Redirect to /authenticate endpoint.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:/authenticate";
    }

    /**
     * Renders the main home page element.
     *
     * @param model Page model.
     * @param request  Reference to current HttpServletRequest.
     * @param response Reference to current HttpServletResponse.
     * @return index page
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.GET)
    public String authenticate(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        if (savedRequest == null) {
            return "redirect:/oauth/error";
        }

        authenticationManagementService.clearContext();

        // fetch operation ID from the saved request, in case there is one present
        final Map<String, String[]> parameterMap = savedRequest.getParameterMap();
        final String[] operationIdList = parameterMap.get("operation_id");
        String operationId = null;
        if (operationIdList != null && operationIdList.length >= 1) {
            operationId = operationIdList[0];
            if (operationIdList.length > 1) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "There are duplicate operation ID instances (" + operationId + ") in redirect URL, first instance will be used");
            }
            // check whether operation exists, if it does not exist or it could not be retrieved, redirect user to the error page
            try {
                nextStepClient.getOperationDetail(operationId);
            } catch (NextStepServiceException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred while retrieving operation with ID: " + operationId, e);
                return "redirect:/oauth/error";
            }

            authenticationManagementService.createAuthenticationWithOperationId(operationId);
        }

        model.put("title", webFlowConfig.getPageTitle());
        model.put("stylesheet", webFlowConfig.getCustomStyleSheetUrl());
        model.put("lang", LocaleContextHolder.getLocale().toString());
        // JSON objects with i18n messages are inserted into the model to provide localization for the frontend
        model.put("i18n_CS", i18nService.generateMessages(new Locale("cs")));
        model.put("i18n_EN", i18nService.generateMessages(Locale.ENGLISH));
        model.put("operationHash", operationSessionService.generateOperationHash(operationId));
        return "index";
    }

    /**
     * Redirects user to previous URL after authentication, or to error URL in case of broken OAuth dance.
     *
     * @param request  Reference to current HttpServletRequest.
     * @param response Reference to current HttpServletResponse.
     * @return Redirect to the /oauth/authorize page
     */
    @RequestMapping(value = "/authenticate/continue", method = RequestMethod.GET)
    public String continueToRedirect(HttpServletRequest request, HttpServletResponse response) {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        String redirectUrl;
        if (savedRequest == null) {
            // Redirect to original page? Currently, use redirect to error...
            // StringBuffer url = request.getRequestURL();
            // String uri = request.getRequestURI();
            // String ctx = request.getContextPath();
            // String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
            return "redirect:/oauth/error";
        } else {
            authenticationManagementService.setLanguage(LocaleContextHolder.getLocale().getLanguage());
            authenticationManagementService.pendingAuthenticationToAuthentication();
            redirectUrl = savedRequest.getRedirectUrl();
        }
        response.setHeader("Location", redirectUrl);
        response.setStatus(HttpServletResponse.SC_FOUND);
        return null;
    }

    /**
     * Handles the cancelling of the authentication flow.
     *
     * @param request  Reference to current HttpServletRequest.
     * @param response Reference to current HttpServletResponse.
     * @return Redirect to the originating page
     */
    @RequestMapping(value = "/authenticate/cancel", method = RequestMethod.GET)
    public String cancelAuthentication(HttpServletRequest request, HttpServletResponse response) {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        if (savedRequest == null) {
            return "redirect:/oauth/error";
        }
        String[] redirectUriParameter = savedRequest.getParameterMap().get("redirect_uri");
        if (redirectUriParameter == null || redirectUriParameter.length != 1) {
            return "redirect:/oauth/error";
        }
        String redirectUri = redirectUriParameter[0];

        String clearContext = request.getParameter("clearContext");
        if (!"false".equals(clearContext)) {
            // Clear security context and invalidate session unless it is suppressed due to a new operation
            authenticationManagementService.clearContext();
        }

        return "redirect:" + redirectUri;
    }

    /**
     * Render the OAuth 2.0 protocol error page.
     *
     * @param model Model.
     * @return Return oauth/error template.
     */
    @RequestMapping(value = "/oauth/error", method = RequestMethod.GET)
    public String oauthError(Map<String, Object> model) {
        model.put("title", webFlowConfig.getPageTitle());
        model.put("stylesheet", webFlowConfig.getCustomStyleSheetUrl());
        return "oauth/error";
    }

}
