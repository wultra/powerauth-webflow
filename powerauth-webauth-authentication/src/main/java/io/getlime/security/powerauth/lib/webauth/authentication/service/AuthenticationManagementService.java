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

package io.getlime.security.powerauth.lib.webauth.authentication.service;

import io.getlime.security.powerauth.lib.webauth.authentication.security.UserOperationAuthentication;
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
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Service
public class AuthenticationManagementService {

    private static final String PENDING_AUTH_OBJECT = "PENDING_AUTH_OBJECT";

    private HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        return attributes.getRequest();
    }

    private void setPendingUserAuthentication(UserOperationAuthentication auth) {
        HttpServletRequest request = currentRequest();
        HttpSession session = request.getSession();
        session.setAttribute(PENDING_AUTH_OBJECT, auth);
    }

    /**
     * Get the current pending authentication object.
     * @return Pending authentication.
     */
    public UserOperationAuthentication getPendingUserAuthentication() {
        HttpServletRequest request = currentRequest();
        HttpSession session = request.getSession();
        return (UserOperationAuthentication) session.getAttribute(PENDING_AUTH_OBJECT);
    }

    /**
     * Clear the security context.
     */
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Create a new authentication object with assigned operation ID.
     * @param operationId Operation ID.
     */
    public void createAuthenticationWithOperationId(String operationId) {
        UserOperationAuthentication auth = new UserOperationAuthentication();
        auth.setOperationId(operationId);
        auth.setAuthenticated(false);
        setPendingUserAuthentication(auth);
    }

    /**
     * Update the current operation with provided user ID. This step assigns authenticated
     * user to given operation.
     * @param userId User ID.
     * @return Operation ID.
     */
    public String updateAuthenticationWithUserId(String userId) {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        if (auth.getUserId() != null && userId != auth.getUserId()) {
            return null;
        }
        if (auth.getOperationId() == null) {
            return null;
        }
        auth.setUserId(userId);
        setPendingUserAuthentication(auth);
        return auth.getOperationId();
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
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

}
