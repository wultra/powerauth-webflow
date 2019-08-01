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

package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Class that is responsible for maintaining state of the pending authentication in session, and
 * for transferring the pending session to security context in the right moment, after authentication
 * is complete.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class AuthenticationManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagementService.class);

    /**
     * Get current HTTP request.
     * @return Current HTTP request.
     */
    private HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        return attributes.getRequest();
    }

    /**
     * Set pending user authentication object.
     * @param auth User authentication object.
     */
    private void setPendingUserAuthentication(UserOperationAuthentication auth) {
        HttpServletRequest request = currentRequest();
        HttpSession session = request.getSession();
        synchronized (session.getServletContext()) {
            session.setAttribute(HttpSessionAttributeNames.PENDING_AUTH_OBJECT, auth);
        }
        logger.info("PENDING_AUTH_OBJECT was added into HTTP session");
    }

    /**
     * Get the current pending authentication object.
     *
     * @return Pending authentication.
     */
    public UserOperationAuthentication getPendingUserAuthentication() {
        HttpServletRequest request = currentRequest();
        HttpSession session = request.getSession();
        return (UserOperationAuthentication) session.getAttribute(HttpSessionAttributeNames.PENDING_AUTH_OBJECT);
    }

    /**
     * Clear the security context.
     */
    public void clearContext() {
        SecurityContextHolder.clearContext();
        logger.info("Security context was cleared");
        HttpServletRequest request = currentRequest();
        HttpSession session = request.getSession();
        synchronized (session.getServletContext()) {
            session.removeAttribute(HttpSessionAttributeNames.PENDING_AUTH_OBJECT);
        }
        logger.info("PENDING_AUTH_OBJECT was removed from HTTP session");

    }

    /**
     * Create a new authentication object with assigned operation ID.
     *
     * @param operationId Operation ID.
     * @param organizationId Organization ID.
     */
    public void createAuthenticationWithOperationId(String operationId, String organizationId) {
        logger.info("Authentication object created for operation ID: {}", operationId);
        UserOperationAuthentication auth = new UserOperationAuthentication();
        auth.setOperationId(operationId);
        auth.setAuthenticated(false);
        auth.setOrganizationId(organizationId);
        setPendingUserAuthentication(auth);
    }

    /**
     * Update the current operation with provided user ID and organization ID. This step assigns authenticated
     * user to given operation.
     *
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @return Operation ID.
     */
    public String updateAuthenticationWithUserDetails(String userId, String organizationId) {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        if (auth.getUserId() != null && !userId.equals(auth.getUserId())) {
            logger.error("Failed updateAuthenticationWithUserDetails due to missing or invalid user ID");
            return null;
        }
        if (auth.getOperationId() == null) {
            logger.error("Failed updateAuthenticationWithUserDetails due to missing operation ID");
            return null;
        }
        auth.setUserId(userId);
        auth.setOrganizationId(organizationId);
        setPendingUserAuthentication(auth);
        return auth.getOperationId();
    }

    /**
     * Return whether pending session is authenticated.
     * @return Whether pending session is authenticated.
     */
    public boolean isPendingSessionAuthenticated() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getUserId() != null;
    }

    /**
     * Mark the current pending authentication authenticated.
     */
    public void upgradeToStrongClientAuthentication() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        auth.setStrongAuthentication(true);
        setPendingUserAuthentication(auth);
    }

    /**
     * Set language to the authentication object.
     * @param language Language to be set.
     */
    public void setLanguage(String language) {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        auth.setLanguage(language);
        setPendingUserAuthentication(auth);
    }

    /**
     * Mark the current pending authentication autenticated.
     */
    public void authenticateCurrentSession() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        auth.setAuthenticated(true);
        setPendingUserAuthentication(auth);
    }

    /**
     * Convert the pending activation to an actual Spring Security authentication
     * stored in the security context.
     */
    public void pendingAuthenticationToAuthentication() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        if (auth.isAuthenticated()) {
            logger.info("Security context was set to authenticated");
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

}
