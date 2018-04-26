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

package io.getlime.security.powerauth.lib.webflow.authentication.method.form.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.request.UsernamePasswordAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response.UsernamePasswordAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for username / password authentication step.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/form")
public class FormLoginController extends AuthMethodController<UsernamePasswordAuthenticationRequest, UsernamePasswordAuthenticationResponse, AuthStepException> {

    private final DataAdapterClient dataAdapterClient;

    /**
     * Controller constructor.
     * @param dataAdapterClient Data adapter client.
     */
    @Autowired
    public FormLoginController(DataAdapterClient dataAdapterClient) {
        this.dataAdapterClient = dataAdapterClient;
    }

    /**
     * Authenticate using username / password authentication.
     * @param request Authentication request.
     * @return Authenticated user ID.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @Override
    protected String authenticate(UsernamePasswordAuthenticationRequest request) throws AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authentication started, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData);
            final ObjectResponse<AuthenticationResponse> authenticateResponse = dataAdapterClient.authenticateUser(request.getUsername(), request.getPassword(), operationContext);
            AuthenticationResponse responseObject = authenticateResponse.getResponseObject();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authentication succeeded, operation ID: {0}, user ID: {1}, authentication method: {2}", new String[] {operation.getOperationId(), responseObject.getUserId(), getAuthMethodName().toString()});
            return responseObject.getUserId();
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
                throw new AuthStepException(e2.getError().getMessage(), e2);
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
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CONFIRMED, authentication method: {0}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CONFIRMED, operation ID: {0}, authentication method: {1}", new String[]{operationId, getAuthMethodName().toString()});
                    return response;
                }
            });
        } catch (AuthStepException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error occurred while authenticating user: {0}", e.getMessage());
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
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
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CANCELED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), getAuthMethodName().toString()});
            return response;
        } catch (NextStepServiceException e) {
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
            return response;
        }
    }
}
