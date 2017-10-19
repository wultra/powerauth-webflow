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

package io.getlime.security.powerauth.lib.webflow.authentication.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodAvailabilityService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Base controller for any authentication method. Controller class is templated using three attributes.
 * <p>
 * <ul>
 * <li>T - extension of AuthStepRequest.</li>
 * <li>R - extension of AuthStepResponse.</li>
 * <li>E - extension of AuthStepException.</li>
 * </ul>
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Component
public abstract class AuthMethodController<T extends AuthStepRequest, R extends AuthStepResponse, E extends AuthStepException> {

    @Autowired
    private AuthenticationManagementService authenticationManagementService;

    @Autowired
    private NextStepClient nextStepClient;

    @Autowired
    private AuthMethodAvailabilityService authMethodAvailabilityService;

    protected GetOperationDetailResponse getOperation() {
        final UserOperationAuthentication pendingUserAuthentication = authenticationManagementService.getPendingUserAuthentication();
        if (pendingUserAuthentication != null) {
            String operationId = pendingUserAuthentication.getOperationId();
            if (operationId != null) {
                return getOperation(operationId);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected GetOperationDetailResponse getOperation(String operationId) {
        try {
            final ObjectResponse<GetOperationDetailResponse> operationDetail = nextStepClient.getOperationDetail(operationId);
            filterStepsBasedOnActiveAuthMethods(operationDetail.getResponseObject().getSteps(), operationDetail.getResponseObject().getUserId(), operationId);
            final GetOperationDetailResponse responseObject = operationDetail.getResponseObject();
            return responseObject;
        } catch (NextStepServiceException e) {
            return null;
        }
    }

    abstract protected AuthMethod getAuthMethodName();

    protected boolean isAuthMethodAvailable(String userId, String operationId) {
        return authMethodAvailabilityService.isAuthMethodEnabledForUser(getAuthMethodName(), userId, operationId);
    }

    protected List<GetOperationDetailResponse> getOperationListForUser(String userId) {
        try {
            final ObjectResponse<List<GetOperationDetailResponse>> operations = nextStepClient.getPendingOperations(userId, getAuthMethodName());
            return operations.getResponseObject();
        } catch (NextStepServiceException e) {
            return null;
        }
    }

    /**
     * Method to authenticate user with provided request object.
     *
     * @param request Request with authentication object information.
     * @return String with user ID.
     * @throws E In case error occurs during authentication.
     */
    protected String authenticate(T request) throws E {
        return null;
    }

    /**
     * Authorize operation with provided ID with user with given user ID.
     *
     * @param operationId Operation ID of operation to be authorized.
     * @param userId      User ID of user who should authorize operation.
     * @return Response with information about operation update result.
     * @throws NextStepServiceException In case communication fails.
     */
    protected UpdateOperationResponse authorize(String operationId, String userId) throws NextStepServiceException {
        return authorize(operationId, userId, null);
    }

    /**
     * Authorize operation with provided ID with user with given user ID.
     *
     * @param operationId Operation ID of operation to be authorized.
     * @param userId      User ID of user who should authorize operation.
     * @param params      Custom parameters.
     * @return Response with information about operation update result.
     * @throws NextStepServiceException In case communication fails.
     */
    protected UpdateOperationResponse authorize(String operationId, String userId, List<KeyValueParameter> params) throws NextStepServiceException {
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.CONFIRMED, null, params);
        filterStepsBasedOnActiveAuthMethods(response.getResponseObject().getSteps(), userId, operationId);
        return response.getResponseObject();
    }

    /**
     * Fail the operation with provided operation ID with user with given user ID.
     *
     * @param operationId Operation ID of operation to fail.
     * @param userId      User ID of user who owns the operation.
     * @param params      Custom parameters.
     * @return Response with information about operation update result.
     * @throws NextStepServiceException In case communication fails.
     */
    protected UpdateOperationResponse failAuthorization(String operationId, String userId, List<KeyValueParameter> params) throws NextStepServiceException {
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.AUTH_FAILED, null, params);
        filterStepsBasedOnActiveAuthMethods(response.getResponseObject().getSteps(), userId, operationId);
        return response.getResponseObject();
    }

    /**
     * @param operationId  Operation ID of operation to cancel.
     * @param userId       User ID of user who owns the operation.
     * @param params       Custom parameters.
     * @param cancelReason Reason for cancellation of the operation.
     * @return Response with information about operation update result.
     * @throws NextStepServiceException In case communication fails.
     */
    protected UpdateOperationResponse cancelAuthorization(String operationId, String userId, OperationCancelReason cancelReason, List<KeyValueParameter> params) throws NextStepServiceException {
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.CANCELED, cancelReason.toString(), params);
        filterStepsBasedOnActiveAuthMethods(response.getResponseObject().getSteps(), userId, operationId);
        return response.getResponseObject();
    }

    /**
     * Initiate a new operation with given name, data and parameters.
     *
     * @param operationName Name of the operation to be created.
     * @param operationData Data of the operation.
     * @param params        Additional parameters of the operation.
     * @param provider      Provider that implements authentication callback.
     * @return Response indicating next step, based on provider response.
     */
    protected R initiateOperationWithName(String operationName, String operationData, List<KeyValueParameter> params, AuthResponseProvider provider) {
        try {
            ObjectResponse<CreateOperationResponse> response = nextStepClient.createOperation(operationName, operationData, params);
            CreateOperationResponse responseObject = response.getResponseObject();
            String operationId = responseObject.getOperationId();
            filterStepsBasedOnActiveAuthMethods(responseObject.getSteps(), null, operationId);
            authenticationManagementService.createAuthenticationWithOperationId(operationId);
            return provider.continueAuthentication(operationId, null, responseObject.getSteps());
        } catch (NextStepServiceException e) {
            return provider.failedAuthentication(null, "error.unknown");
        }
    }

    /**
     * Initiate a new operation with given name, data and parameters.
     *
     * @param operationId ID of operation to be fetched.
     * @param provider    Provider that implements authentication callback.
     * @return Response indicating next step, based on provider response.
     */
    protected R continueOperationWithId(String operationId, AuthResponseProvider provider) {
        try {
            final ObjectResponse<GetOperationDetailResponse> operationDetail = nextStepClient.getOperationDetail(operationId);
            if (operationDetail != null) {
                GetOperationDetailResponse responseObject = operationDetail.getResponseObject();
                if (responseObject != null) {
                    final String userId = responseObject.getUserId();
                    filterStepsBasedOnActiveAuthMethods(responseObject.getSteps(), userId, operationId);
                    if (userId != null) {
                        authenticationManagementService.updateAuthenticationWithUserId(userId);
                    }
                    if (AuthResult.DONE.equals(responseObject.getResult())) {
                        return provider.doneAuthentication(userId);
                    } else {
                        return provider.continueAuthentication(operationId, userId, responseObject.getSteps());
                    }
                } else {
                    return provider.failedAuthentication(null, "error.unknown");
                }
            } else {
                return provider.failedAuthentication(null, "error.unknown");
            }
        } catch (NextStepServiceException e) {
            return provider.failedAuthentication(null, "error.unknown");
        }
    }


    /**
     * Build next authentication step information for given operation.
     *
     * @param request  Request containing information about the current authentication.
     * @param provider Provider with authentication callback implementation.
     * @return Response indicating next step, based on provider response.
     * @throws AuthStepException In case authentication fails.
     */
    protected R buildAuthorizationResponse(T request, AuthResponseProvider provider) throws AuthStepException {
        try {
            String userId = authenticate(request);
            UpdateOperationResponse responseObject;
            if (userId == null) {
                // user was not authenticated - fail authorization
                authenticationManagementService.clearContext();
                responseObject = failAuthorization(getOperation().getOperationId(), null, null);
            } else {
                // user was authenticated - complete authorization
                String operationId = authenticationManagementService.updateAuthenticationWithUserId(userId);

                // response could not be derived - call authorize() method to update current operation
                responseObject = authorize(operationId, userId, null);
            }
            // TODO: Allow passing custom parameters
            switch (responseObject.getResult()) {
                case DONE: {
                    return provider.doneAuthentication(userId);
                }
                case FAILED: {
                    return provider.failedAuthentication(userId, responseObject.getResultDescription());
                }
                case CONTINUE: {
                    return provider.continueAuthentication(responseObject.getOperationId(), userId, responseObject.getSteps());
                }
                default: {
                    return provider.failedAuthentication(userId, "error.unknown");
                }
            }
        } catch (NextStepServiceException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    protected void clearCurrentBrowserSession() {
        authenticationManagementService.clearContext();
    }

    protected void authenticateCurrentBrowserSession() {
        authenticationManagementService.authenticateCurrentSession();
        final GetOperationDetailResponse operation = getOperation();
        if (AuthResult.DONE.equals(operation.getResult())) {
            authenticationManagementService.pendingAuthenticationToAuthentication();
        }
    }


    private void filterStepsBasedOnActiveAuthMethods(List<AuthStep> authSteps, String userId, String operationId) {
        for (AuthStep authStep: authSteps) {
            if (!authMethodAvailabilityService.isAuthMethodEnabledForUser(authStep.getAuthMethod(), userId, operationId)) {
                authSteps.remove(authStep);
            }
        }
    }

    /**
     * Class providing callbacks for operation authentication outcomes.
     */
    public abstract class AuthResponseProvider {

        /**
         * Called in case user successfully authenticated and no other authentication is needed.
         *
         * @param userId User ID.
         * @return Information about successful authentication, confirmation step.
         */
        public abstract R doneAuthentication(String userId);

        /**
         * Called in case authentication fails and no other steps can be performed.
         *
         * @param userId       User ID.
         * @param failedReason Reason for the failure.
         * @return Information about authentication failure, error step.
         */
        public abstract R failedAuthentication(String userId, String failedReason);

        /**
         * Called in case authentication should continue with next step(s).
         *
         * @param operationId Operation ID of the current operation.
         * @param userId      User ID.
         * @param steps       List of next steps to be performed.
         * @return Information about next steps for given operation.
         */
        public abstract R continueAuthentication(String operationId, String userId, List<AuthStep> steps);
    }


}
