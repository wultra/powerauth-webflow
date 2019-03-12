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

package io.getlime.security.powerauth.lib.webflow.authentication.method.form.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationsResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.converter.OrganizationConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.OrganizationDetailRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.OrganizationListRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.UsernamePasswordAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.OrganizationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.OrganizationListResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.UsernamePasswordAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthenticationResult;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller for username / password authentication step.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/form")
public class FormLoginController extends AuthMethodController<UsernamePasswordAuthenticationRequest, UsernamePasswordAuthenticationResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(FormLoginController.class);

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;

    private final OrganizationConverter organizationConverter = new OrganizationConverter();

    /**
     * Controller constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param nextStepClient Next Step client.
     */
    @Autowired
    public FormLoginController(DataAdapterClient dataAdapterClient, NextStepClient nextStepClient) {
        this.dataAdapterClient = dataAdapterClient;
        this.nextStepClient = nextStepClient;
    }

    /**
     * Authenticate using username / password authentication.
     * @param request Authentication request.
     * @return Authentication result with user ID and organization ID.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @Override
    protected AuthenticationResult authenticate(UsernamePasswordAuthenticationRequest request) throws AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        checkOperationExpiration(operation);
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData);
            final ObjectResponse<AuthenticationResponse> authenticateResponse = dataAdapterClient.authenticateUser(request.getUsername(), request.getPassword(), request.getOrganizationId(), operationContext);
            AuthenticationResponse responseObject = authenticateResponse.getResponseObject();
            logger.info("Step authentication succeeded, operation ID: {}, user ID: {}, authentication method: {}", operation.getOperationId(), responseObject.getUserId(), getAuthMethodName().toString());
            return new AuthenticationResult(responseObject.getUserId(), responseObject.getOrganizationId());
        } catch (DataAdapterClientErrorException e) {
            Integer remainingAttemptsNS;
            try {
                // User was not authenticated by Data Adapter - fail authorization to count the number of failures and make it possible
                // to switch to an alternate authentication method in case it is available.
                // Fix #72: Do not include incomplete login attempts when counting number of failed authentication requests
                if ("login.authenticationFailed".equals(e.getError().getMessage())) {
                    UpdateOperationResponse response = failAuthorization(operation.getOperationId(), null, null);
                    if (response.getResult() == AuthResult.FAILED) {
                        // FAILED result instead of CONTINUE means the authentication method is failed
                        throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded");
                    }
                }
                GetOperationDetailResponse updatedOperation = getOperation();
                remainingAttemptsNS = updatedOperation.getRemainingAttempts();
            } catch (NextStepServiceException e2) {
                logger.error("Error occurred in Next Step server", e);
                throw new AuthStepException(e2.getError().getMessage(), e2, "error.communication");
            }
            AuthStepException authEx = new AuthStepException(e.getError().getMessage(), e);
            Integer remainingAttemptsDA = e.getError().getRemainingAttempts();
            Integer remainingAttempts = resolveRemainingAttempts(remainingAttemptsDA, remainingAttemptsNS);
            authEx.setRemainingAttempts(remainingAttempts);
            throw authEx;
        }
    }

    /**
     * Get current authentication method name.
     * @return Current authentication method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.USERNAME_PASSWORD_AUTH;
    }

    /**
     * Handle the user authentication based on username and password.
     *
     * @param request Authentication request using username and password.
     * @return Authentication response.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody UsernamePasswordAuthenticationResponse authenticateHandler(@RequestBody UsernamePasswordAuthenticationRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public UsernamePasswordAuthenticationResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    logger.info("Step result: CONFIRMED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
                    return response;
                }
            });
        } catch (AuthStepException e) {
            logger.warn("Error occurred while authenticating user: {}", e.getMessage());
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            if (e.getMessageId() != null) {
                // prefer localized message over regular message string
                response.setMessage(e.getMessageId());
            } else {
                response.setMessage(e.getMessage());
            }
            response.setRemainingAttempts(e.getRemainingAttempts());
            return response;
        }

    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody UsernamePasswordAuthenticationResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (NextStepServiceException e) {
            logger.error("Error occurred in Next Step server", e);
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Get organization detail.
     * @param request Get organization detail request.
     * @return Get organization detail response.
     * @throws AuthStepException Thrown when request is invalid or communication with Next Step fails.
     */
    @RequestMapping(value = "/organization", method = RequestMethod.POST)
    public @ResponseBody OrganizationDetailResponse getOrganizationDetail(@RequestBody OrganizationDetailRequest request) throws AuthStepException {
        if (request == null || request.getOrganizationId() == null) {
            throw new AuthStepException("Invalid request in getOrganization", "error.invalidRequest");
        }
        String organizationId = request.getOrganizationId();
        logger.info("Get organization detail started, organization ID: {}", organizationId);
        final OrganizationDetailResponse response;
        try {
            ObjectResponse<GetOrganizationResponse> nsObjectResponse = nextStepClient.getOrganization(organizationId);
            GetOrganizationResponse nsResponse = nsObjectResponse.getResponseObject();
            response = organizationConverter.fromNSOrganization(nsResponse);
        } catch (NextStepServiceException e) {
            throw new CommunicationFailedException("Organization is not available");
        }
        logger.info("Get organization detail succeeded, organization ID: {}", organizationId);
        return response;
    }

    /**
     * Get organizations.
     * @param request Get organizations request.
     * @return Get organizations response.
     * @throws AuthStepException Thrown when request is invalid or communication with Next Step fails.
     */
    @RequestMapping(value = "/organization/list", method = RequestMethod.POST)
    public @ResponseBody OrganizationListResponse getOrganizationList(@RequestBody OrganizationListRequest request) throws AuthStepException {
        if (request == null) {
            throw new AuthStepException("Invalid request in getOrganizations", "error.invalidRequest");
        }
        logger.info("Get organization list started");
        final OrganizationListResponse response = new OrganizationListResponse();
        try {
            ObjectResponse<GetOrganizationsResponse> nsObjectResponse = nextStepClient.getOrganizations();
            List<GetOrganizationResponse> nsResponseList = nsObjectResponse.getResponseObject().getOrganizations();
            for (GetOrganizationResponse nsResponse: nsResponseList) {
                OrganizationDetailResponse organization = organizationConverter.fromNSOrganization(nsResponse);
                response.addOrganization(organization);
            }
        } catch (NextStepServiceException e) {
            throw new CommunicationFailedException("Organization is not available");
        }
        logger.info("Get organization list succeeded");
        return response;
    }
}
