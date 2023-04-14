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

package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        UserOperationAuthentication auth = new UserOperationAuthentication();
        auth.setOperationId(operationId);
        auth.setAuthenticated(false);
        auth.setOrganizationId(organizationId);
        setPendingUserAuthentication(auth);
        logger.info("Authentication object was created for operation ID: {}", operationId);
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
        logger.info("Authentication object was updated in HTTP session with user details");
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
     * Upgrade authentication to SCA.
     */
    public void upgradeToStrongCustomerAuthentication() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        auth.setStrongAuthentication(true);
        setPendingUserAuthentication(auth);
        logger.info("Authentication object was updated in HTTP session with upgrade to SCA");
    }

    /**
     * Set language to the authentication object.
     * @param language Language to be set.
     */
    public void setLanguage(String language) {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        auth.setLanguage(language);
        setPendingUserAuthentication(auth);
        logger.info("Authentication object was updated in HTTP session with language");
    }

    /**
     * Mark the current pending authentication autenticated.
     */
    public void authenticateCurrentSession() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        auth.setAuthenticated(true);
        setPendingUserAuthentication(auth);
        logger.info("Authentication object was updated in HTTP session with authenticated state");
    }

    /**
     * Convert the pending activation to an actual Spring Security authentication
     * stored in the security context.
     */
    public void pendingAuthenticationToAuthentication() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        if (auth.isAuthenticated()) {
            final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(auth);
            SecurityContextHolder.setContext(securityContext);
            logger.info("UserOperationAuthentication(userId={}) set to the security context.", auth.getUserId());
        }
    }

}
