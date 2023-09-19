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
package io.getlime.security.powerauth.app.webflow.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.webflow.configuration.WebFlowServerConfiguration;
import io.getlime.security.powerauth.app.webflow.i18n.I18NService;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.AfsConfigRepository;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.AfsConfigEntity;
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationCancellationService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Simple controller, redirects to the main HTML page with JavaScript content.
 *
 * @author Roman Strobl
 */
@Controller
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final WebFlowServerConfiguration webFlowConfig;
    private final AuthenticationManagementService authenticationManagementService;
    private final I18NService i18nService;
    private final OperationSessionService operationSessionService;
    private final NextStepClient nextStepClient;
    private final AfsConfigRepository afsConfigRepository;
    private final HttpSession httpSession;
    private final OperationCancellationService operationCancellationService;
    private final RegisteredClientRepository registeredClientRepository;

    /**
     * Initialization of the HomeController with application configuration.
     * @param authenticationManagementService Authentication management service.
     * @param webFlowConfig WebFlowServicesConfiguration of the application.
     * @param i18nService I18n service.
     * @param operationSessionService Operation to session mapping service.
     * @param nextStepClient Next step client.
     * @param afsConfigRepository Anti-fraud system configuration repository.
     * @param httpSession HTTP session.
     * @param operationCancellationService Service used for canceling operations.
     * @param registeredClientRepository OAuth 2.1 registered client repository.
     */
    @Autowired
    public HomeController(AuthenticationManagementService authenticationManagementService, WebFlowServerConfiguration webFlowConfig, I18NService i18nService, OperationSessionService operationSessionService, NextStepClient nextStepClient, AfsConfigRepository afsConfigRepository, HttpSession httpSession, OperationCancellationService operationCancellationService, RegisteredClientRepository registeredClientRepository) {
        this.webFlowConfig = webFlowConfig;
        this.authenticationManagementService = authenticationManagementService;
        this.i18nService = i18nService;
        this.operationSessionService = operationSessionService;
        this.nextStepClient = nextStepClient;
        this.afsConfigRepository = afsConfigRepository;
        this.httpSession = httpSession;
        this.operationCancellationService = operationCancellationService;
        this.registeredClientRepository = registeredClientRepository;
    }

    /**
     * Redirect from home page to /authenticate endpoint.
     *
     * @return Redirect to /authenticate endpoint.
     */
    @GetMapping("/")
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
    @GetMapping("/authenticate")
    public String authenticate(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
        logger.info("Received /authenticate request");
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        if (savedRequest == null) {
            logger.error("HTTP request not found in HttpSessionRequestCache");
            return "redirect:/oauth2/error";
        }

        authenticationManagementService.clearContext();

        // make sure all state variables used in HTTP session during operation steps are cleared
        cleanHttpSession();

        // fetch operation ID from the saved request, in case there is one present
        final Map<String, String[]> parameterMap = savedRequest.getParameterMap();
        final String[] operationIdList = parameterMap.get("operation_id");
        String operationId = null;
        if (operationIdList != null && operationIdList.length >= 1) {
            operationId = operationIdList[0];
            if (operationIdList.length > 1) {
                logger.info("There are duplicate operation ID instances (" + operationId + ") in redirect URL, first instance will be used");
            }
            // check whether operation exists, if it does not exist or it could not be retrieved, redirect user to the error page
            String organizationId;
            try {
                ObjectResponse<GetOperationDetailResponse> objectResponse = nextStepClient.getOperationDetail(operationId);
                organizationId = objectResponse.getResponseObject().getOrganizationId();

                // AFS is enabled only for non-default operations which are deprecated
                if (webFlowConfig.isAfsEnabled()) {
                    String operationName = objectResponse.getResponseObject().getOperationName();
                    ObjectResponse<GetOperationConfigDetailResponse> objectResponseConfig = nextStepClient.getOperationConfigDetail(operationName);
                    GetOperationConfigDetailResponse config = objectResponseConfig.getResponseObject();
                    if (config.isAfsEnabled()) {
                        if (config.getAfsConfigId() != null) {
                            Optional<AfsConfigEntity> afsConfig = afsConfigRepository.findById(config.getAfsConfigId());
                            if (afsConfig.isPresent()) {
                                String afsJsSnippet = afsConfig.get().getJsSnippetUrl();
                                if (afsJsSnippet != null) {
                                    model.put("afs_js_snippet_url", afsJsSnippet);
                                }
                            } else {
                                logger.error("AFS configuration is not available in Web Flow: {}", config.getAfsConfigId());
                            }
                        } else {
                            logger.error("AFS configuration is invalid for operation name: {}", config.getOperationName());
                        }
                    }
                }

            } catch (NextStepClientException ex) {
                logger.error("Error occurred while retrieving operation with ID: " + operationId, ex);
                return "redirect:/oauth2/error";
            }

            authenticationManagementService.createAuthenticationWithOperationId(operationId, organizationId);
        }

        model.putIfAbsent("afs_js_snippet_url", "");

        model.put("title", webFlowConfig.getPageTitle());
        model.put("consentPanelLimitEnabled", webFlowConfig.isConsentPanelLimitEnabled());
        model.put("consentPanelLimitCharacters", webFlowConfig.getConsentPanelLimitCharacters());
        model.put("stylesheet", webFlowConfig.getCustomStyleSheetUrl());
        // get supported languages
        model.put("languageSetting", i18nService.readLanguageSetting());
        // default to the supported language
        model.put("lang", LocaleContextHolder.getLocale().getLanguage());
        // JSON objects with i18n messages are inserted into the model to provide localization for the frontend
        model.put("i18n_CS", i18nService.generateMessages(new Locale("cs")));
        model.put("i18n_EN", i18nService.generateMessages(Locale.ENGLISH));
        model.put("i18n_UK", i18nService.generateMessages(new Locale("uk")));
        model.put("i18n_RO", i18nService.generateMessages(new Locale("ro")));
        model.put("operationHash", operationSessionService.generateOperationHash(operationId));
        model.put("showAndroidSecurityWarning", webFlowConfig.getShowAndroidSecurityWarning());
        model.put("usernameMaxLength", webFlowConfig.getUsernameMaxLength());
        model.put("passwordMaxLength", webFlowConfig.getPasswordMaxLength());
        model.put("smsOtpMaxLength", webFlowConfig.getSmsOtpMaxLength());
        model.put("approvalCertificateEnabled", webFlowConfig.isApprovalCertificateEnabled());
        model.put("approvalCertificateSigner", webFlowConfig.getApprovalCertificateSigner().toString());
        model.put("icaConfigurationUrl", webFlowConfig.getIcaConfigurationUrl());
        model.put("icaLogLevel", webFlowConfig.getIcaLogLevel());
        model.put("icaExtensionOwner", webFlowConfig.getIcaExtensionOwner());
        model.put("icaExtensionIDChrome", webFlowConfig.getIcaExtensionIDChrome());
        model.put("icaExtensionIDOpera", webFlowConfig.getIcaExtensionIDOpera());
        model.put("icaExtensionIDEdge", webFlowConfig.getIcaExtensionIDEdge());
        model.put("icaExtensionIDFirefox", webFlowConfig.getIcaExtensionIDFirefox());
        model.put("icaExtensionInstallURLFirefox", webFlowConfig.getIcaExtensionInstallURLFirefox());
        logger.info("The /authenticate request succeeded");
        return "index";
    }

    /**
     * Redirects user to previous URL after authentication, or to error URL in case of broken OAuth dance.
     *
     * @param request  Reference to current HttpServletRequest.
     * @param response Reference to current HttpServletResponse.
     * @return Redirect to the /oauth/authorize page
     */
    @GetMapping("/authenticate/continue")
    public String continueToRedirect(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Received /authenticate/continue request");
        final HttpSessionRequestCache cache = new HttpSessionRequestCache();
        final SavedRequest savedRequest = cache.getRequest(request, response);
        String redirectUrl;
        if (savedRequest == null) {
            // Redirect to original page? Currently, use redirect to error...
            // StringBuffer url = request.getRequestURL();
            // String uri = request.getRequestURI();
            // String ctx = request.getContextPath();
            // String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
            logger.error("HTTP request not found in HttpSessionRequestCache");
            return "redirect:/oauth2/error";
        } else {
            authenticationManagementService.setLanguage(LocaleContextHolder.getLocale().getLanguage());
            authenticationManagementService.pendingAuthenticationToAuthentication();
            redirectUrl = savedRequest.getRedirectUrl();
        }
        // Make sure HTTP session is cleaned when authentication is complete
        cleanHttpSession();
        response.setHeader("Location", redirectUrl);
        response.setStatus(HttpServletResponse.SC_FOUND);
        logger.info("The /authenticate/continue request succeeded, redirect URL: {}", redirectUrl);
        return null;
    }

    /**
     * Handles the cancelling of the authentication flow.
     *
     * @param request  Reference to current HttpServletRequest.
     * @param response Reference to current HttpServletResponse.
     * @return Redirect to the originating page
     */
    @GetMapping("/authenticate/cancel")
    public String cancelAuthentication(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Received /authenticate/cancel request");
        final HttpSessionRequestCache cache = new HttpSessionRequestCache();
        final SavedRequest savedRequest = cache.getRequest(request, response);
        if (savedRequest == null) {
            logger.error("HTTP request not found in HttpSessionRequestCache");
            return "redirect:/oauth2/error";
        }
        final String[] redirectUriParameter = savedRequest.getParameterMap().get("redirect_uri");
        if (redirectUriParameter == null) {
            logger.error("Parameter redirect_uri is missing");
            return "redirect:/oauth2/error";
        }
        if (redirectUriParameter.length != 1) {
            logger.error("Multiple redirect_uri request parameters found");
            return "redirect:/oauth2/error";
        }
        final String redirectUri = redirectUriParameter[0];

        // Verify client_id against oauth_client_details database table
        final String[] clientIdParameter = savedRequest.getParameterMap().get("client_id");
        if (clientIdParameter == null) {
            logger.error("Parameter client_id is missing");
            return "redirect:/oauth2/error";
        }
        if (clientIdParameter.length != 1) {
            logger.error("Multiple client_id request parameters found");
            return "redirect:/oauth2/error";
        }

        final String clientId = clientIdParameter[0];

        final RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            logger.error("Registered client not found for client_id: {}", clientId);
            return "redirect:/oauth2/error";
        }
        final Set<String> registeredRedirectUris = registeredClient.getRedirectUris();
        // Verify that redirect URI is registered for provided client ID
        if (!registeredRedirectUris.contains(redirectUri)) {
            logger.error("Redirect URI '{}' is not registered for client_id: {}", redirectUri, clientId);
            return "redirect:/oauth2/error";
        }

        // Verify response type, only 'code' is supported
        final String[] responseTypeParameter = savedRequest.getParameterMap().get("response_type");
        if (responseTypeParameter == null) {
            logger.error("Parameter response_type is missing");
            return "redirect:/oauth2/error";
        }
        if (responseTypeParameter.length != 1) {
            logger.error("Multiple response_type request parameters found");
            return "redirect:/oauth2/error";
        }

        final String responseType = responseTypeParameter[0];
        if (!"code".equals(responseType)) {
            logger.error("Invalid response type: {}", responseType);
            return "redirect:/oauth2/error";
        }

        // Extract optional state parameter from original request
        final String[] stateParameter = savedRequest.getParameterMap().get("state");
        String state = null;
        if (stateParameter == null || stateParameter.length > 1) {
            logger.error("Multiple state request parameters found");
            return "redirect:/oauth2/error";
        } else if (stateParameter.length == 1) {
            state = stateParameter[0];
        }

        // Cancel existing operation in Next Step in case operation is still active
        final UserOperationAuthentication pendingUserAuthentication = authenticationManagementService.getPendingUserAuthentication();
        if (pendingUserAuthentication != null) {
            String operationId = pendingUserAuthentication.getOperationId();
            try {
                operationCancellationService.cancelOperation(operationId, AuthMethod.INIT, OperationCancelReason.UNEXPECTED_ERROR, true);
            } catch (CommunicationFailedException ex) {
                // Exception is already logged
                return "redirect:/oauth2/error";
            }
        }

        final String clearContext = request.getParameter("clearContext");
        if (!"false".equals(clearContext)) {
            // Clear security context and invalidate session unless it is suppressed due to a new operation
            authenticationManagementService.clearContext();
        }

        // Make sure HTTP session is cleaned when authentication is canceled
        cleanHttpSession();

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "access_denied")
                .queryParam("error_description", "User%20canceled%20authentication%20request");
        if (state != null) {
            uriBuilder.queryParam("state", state);
        }

        // Append error, error_description and state based on https://www.oauth.com/oauth2-servers/authorization/the-authorization-response
        final String redirectWithError = uriBuilder
                .build()
                .toUriString();
        logger.info("The /authenticate/cancel request succeeded");
        return "redirect:" + redirectWithError;
    }

    /**
     * Render the OAuth 2.1 protocol error page.
     *
     * @param model Model.
     * @return Return oauth/error template.
     */
    @GetMapping("/oauth2/error")
    public String oauthError(Map<String, Object> model) {
        // Make sure HTTP session is cleaned when error is displayed
        cleanHttpSession();

        model.put("title", webFlowConfig.getPageTitle());
        model.put("stylesheet", webFlowConfig.getCustomStyleSheetUrl());
        return "oauth2/error";
    }

    /**
     * Clean HTTP session variables in case previous operation was interrupted
     * or failed with a fatal error.
     */
    private void cleanHttpSession() {
        synchronized (httpSession.getServletContext()) {
            httpSession.removeAttribute(HttpSessionAttributeNames.OTP_ID);
            httpSession.removeAttribute(HttpSessionAttributeNames.LAST_MESSAGE_TIMESTAMP);
            httpSession.removeAttribute(HttpSessionAttributeNames.INITIAL_MESSAGE_SENT);
            httpSession.removeAttribute(HttpSessionAttributeNames.AUTH_STEP_OPTIONS);
            httpSession.removeAttribute(HttpSessionAttributeNames.CONSENT_SKIPPED);
            httpSession.removeAttribute(HttpSessionAttributeNames.USERNAME);
            httpSession.removeAttribute(HttpSessionAttributeNames.CLIENT_CERTIFICATE);
            httpSession.removeAttribute(HttpSessionAttributeNames.APPROVAL_BY_CERTIFICATE_ENABLED);
            httpSession.removeAttribute(HttpSessionAttributeNames.OPERATION_DATA_EXTERNAL);
        }
    }
}
