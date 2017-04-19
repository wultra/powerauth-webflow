package io.getlime.security.powerauth.app.webauth.authentication.service;

import io.getlime.security.powerauth.app.webauth.authentication.security.UserOperationAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Service
public class AuthenticationManagementService {

    private static final String PENDING_AUTH_OBJECT = "PENDING_AUTH_OBJECT";

    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

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

    private UserOperationAuthentication getPendingUserAuthentication() {
        HttpServletRequest request = currentRequest();
        HttpSession session = request.getSession();
        return (UserOperationAuthentication) session.getAttribute(PENDING_AUTH_OBJECT);
    }

    public void createAuthenticationWithOperationId(String operationId) {
        UserOperationAuthentication auth = new UserOperationAuthentication();
        auth.setOperationId(operationId);
        auth.setAuthenticated(false);
        setPendingUserAuthentication(auth);
    }

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

    public void authenticateCurrentSession() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        auth.setAuthenticated(true);
        setPendingUserAuthentication(auth);
    }

    public void pendingAuthenticationToAuthentication() {
        UserOperationAuthentication auth = getPendingUserAuthentication();
        if (auth.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

}
