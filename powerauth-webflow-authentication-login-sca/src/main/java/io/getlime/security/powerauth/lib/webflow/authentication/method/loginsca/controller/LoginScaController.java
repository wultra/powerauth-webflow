/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationListResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.request.LoginScaAuthRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.request.LoginScaInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.response.LoginScaAuthResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.response.LoginScaInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.model.OrganizationDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.OrganizationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.UserAccountStatusConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * Controller for initialization of SCA login.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/login-sca")
public class LoginScaController extends AuthMethodController<LoginScaAuthRequest, LoginScaAuthResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(LoginScaController.class);

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final AuthenticationManagementService authenticationManagementService;
    private final HttpSession httpSession;

    private final OrganizationConverter organizationConverter = new OrganizationConverter();
    private final UserAccountStatusConverter statusConverter = new UserAccountStatusConverter();

    /**
     * Controller constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param nextStepClient Next Step client.
     * @param authMethodQueryService Service for querying authentication methods.
     * @param authenticationManagementService Authentication management service.
     * @param httpSession HTTP session.
     */
    @Autowired
    public LoginScaController(DataAdapterClient dataAdapterClient, NextStepClient nextStepClient, AuthMethodQueryService authMethodQueryService, AuthenticationManagementService authenticationManagementService, HttpSession httpSession) {
        this.dataAdapterClient = dataAdapterClient;
        this.nextStepClient = nextStepClient;
        this.authMethodQueryService = authMethodQueryService;
        this.authenticationManagementService = authenticationManagementService;
        this.httpSession = httpSession;
    }

    /**
     * Initialize SCA login for given username.
     * @param request Initialization request.
     * @return SCA login initialization response.
     * @throws AuthStepException In case SCA login initialization fails.
     * @throws NextStepServiceException In case communication with Next Step service fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public LoginScaAuthResponse authenticateScaLogin(@RequestBody LoginScaAuthRequest request) throws AuthStepException, NextStepServiceException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        checkOperationExpiration(operation);
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData, applicationContext);
            String userId = operation.getUserId();
            String organizationId = request.getOrganizationId();
            AccountStatus accountStatus = statusConverter.fromUserAccountStatus(operation.getAccountStatus());
            boolean userIdAlreadyAvailable;
            if (userId == null) {
                // First time invocation, user ID is not available yet
                userIdAlreadyAvailable = false;
                String username = request.getUsername();
                ObjectResponse<UserDetailResponse> objectResponse = dataAdapterClient.lookupUser(username, organizationId, operationContext);
                updateUsernameInHttpSession(username);
                UserDetailResponse userDetailResponse = objectResponse.getResponseObject();
                userId = userDetailResponse.getId();
                accountStatus = userDetailResponse.getAccountStatus();
            } else {
                // User ID is already set, this can happen when the user refreshes the page or another authentication method set the user ID
                userIdAlreadyAvailable = true;
            }
            LoginScaAuthResponse response = new LoginScaAuthResponse();
            nextStepClient.updateChosenAuthMethod(operation.getOperationId(), AuthMethod.LOGIN_SCA);
            if (!userIdAlreadyAvailable && userId != null) {
                // User ID lookup succeeded, update user ID in operation so that Push Server can deliver the personal push message
                authenticationManagementService.updateAuthenticationWithUserDetails(userId, organizationId);
                authenticationManagementService.upgradeToStrongCustomerAuthentication();
                nextStepClient.updateOperationUser(operation.getOperationId(), userId, organizationId, statusConverter.fromAccountStatus(accountStatus));
            }
            if (userId == null || accountStatus != AccountStatus.ACTIVE) {
                // User ID is not available or user account is not ACTIVE, mock SMS and password fallback to avoid fishing for active accounts
                response.setResult(AuthStepResult.CONFIRMED);
                response.setMobileTokenEnabled(false);
                logger.debug("Step authentication succeeded with fake SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                return response;
            } else {
                // Find out whether mobile token is enabled
                boolean mobileTokenEnabled = false;
                try {
                    if (authMethodQueryService.isMobileTokenAvailable(userId, operation.getOperationId())) {
                        nextStepClient.updateMobileToken(operation.getOperationId(), true);
                        mobileTokenEnabled = true;
                    }
                } catch (NextStepServiceException e) {
                    logger.error(e.getMessage(), e);
                }
                response.setMobileTokenEnabled(mobileTokenEnabled);
                if (mobileTokenEnabled) {
                    response.setResult(AuthStepResult.CONFIRMED);
                    logger.debug("Step authentication succeeded with mobile token, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                    return response;
                } else {
                    response.setResult(AuthStepResult.CONFIRMED);
                    logger.debug("Step authentication succeeded with SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                    return response;
                }
            }
        } catch (DataAdapterClientErrorException e) {
            // Send error to client
            LoginScaAuthResponse response = new LoginScaAuthResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setRemainingAttempts(e.getError().getRemainingAttempts());
            response.setMessage(e.getError().getMessage());
            return response;
        }
    }

    /**
     * Prepare login form data.
     * @param request Prepare login form data request.
     * @return Prepare login form response.
     * @throws AuthStepException Thrown when request is invalid or communication with Next Step fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public LoginScaInitResponse initScaLogin(@RequestBody LoginScaInitRequest request) throws AuthStepException {
        if (request == null) {
            throw new AuthStepException("Invalid request in prepareLoginForm", "error.invalidRequest");
        }
        final GetOperationDetailResponse operation = getOperation();
        logger.info("Step init started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        final LoginScaInitResponse response = new LoginScaInitResponse();
        if (operation.getUserId() != null && operation.getOrganizationId() != null) {
            // Username form can be skipped
            response.setUserAlreadyKnown(true);
            // Find out whether mobile token is enabled
            boolean mobileTokenEnabled = false;
            try {
                if (authMethodQueryService.isMobileTokenAvailable(operation.getUserId(), operation.getOperationId())) {
                    mobileTokenEnabled = true;
                }
            } catch (NextStepServiceException e) {
                logger.error(e.getMessage(), e);
            }
            response.setMobileTokenEnabled(mobileTokenEnabled);
            logger.info("Step init skipped, user and organization is already known, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        }
        try {
            ObjectResponse<GetOrganizationListResponse> nsObjectResponse = nextStepClient.getOrganizationList();
            List<GetOrganizationDetailResponse> nsResponseList = nsObjectResponse.getResponseObject().getOrganizations();
            for (GetOrganizationDetailResponse nsResponse: nsResponseList) {
                OrganizationDetail organization = organizationConverter.fromNSOrganization(nsResponse);
                response.addOrganization(organization);
            }
        } catch (NextStepServiceException e) {
            throw new CommunicationFailedException("Organization is not available");
        }
        logger.info("Step authentication succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        return response;
    }

    /**
     * Get current authentication method name.
     * @return Current authentication method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.LOGIN_SCA;
    }

    /**
     * Set username in HTTP session.
     * @param username Username supplied by user.
     */
    private void updateUsernameInHttpSession(String username) {
        synchronized (httpSession.getServletContext()) {
            if (username == null || !username.matches("^[0-9a-zA-Z_]+$")) {
                logger.warn("Invalid username: {}", username);
                return;
            }
            httpSession.setAttribute(HttpSessionAttributeNames.USERNAME, username);
        }
    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public AuthStepResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (NextStepServiceException e) {
            logger.error("Error occurred in Next Step server", e);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

}
