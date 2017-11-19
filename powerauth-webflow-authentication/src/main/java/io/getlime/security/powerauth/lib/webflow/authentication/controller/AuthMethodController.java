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
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.MessageTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private AuthMethodQueryService authMethodQueryService;

    @Autowired
    private DataAdapterClient dataAdapterClient;

    @Autowired
    private MessageTranslationService messageTranslationService;

    protected GetOperationDetailResponse getOperation() throws AuthStepException {
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

    protected GetOperationDetailResponse getOperation(String operationId) throws AuthStepException {
        try {
            final ObjectResponse<GetOperationDetailResponse> operationDetail = nextStepClient.getOperationDetail(operationId);
            final GetOperationDetailResponse operation = operationDetail.getResponseObject();
            validateOperationState(operation);
            filterStepsBasedOnActiveAuthMethods(operation.getSteps(), operation.getUserId(), operationId);
            // translate formData messages
            messageTranslationService.translateFormData(operation.getFormData());
            return operation;
        } catch (NextStepServiceException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Next Step server", e);
            return null;
        }
    }

    abstract protected AuthMethod getAuthMethodName();

    protected List<GetOperationDetailResponse> getOperationListForUser(String userId) {
        try {
            final ObjectResponse<List<GetOperationDetailResponse>> operations = nextStepClient.getPendingOperations(userId, getAuthMethodName());
            final List<GetOperationDetailResponse> responseObject = operations.getResponseObject();
            for (GetOperationDetailResponse response: responseObject) {
                // translate formData messages
                messageTranslationService.translateFormData(response.getFormData());
            }
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
    protected UpdateOperationResponse authorize(String operationId, String userId) throws NextStepServiceException, AuthStepException {
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
    protected UpdateOperationResponse authorize(String operationId, String userId, List<KeyValueParameter> params) throws NextStepServiceException, AuthStepException {
        // validate operation before requesting update
        validateOperationState(operationId);
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.CONFIRMED, null, params);
        // notify Data Adapter in case operation is in DONE state now
        if (response.getResponseObject().getResult()==AuthResult.DONE) {
            try {
                dataAdapterClient.operationChangedNotification(OperationChange.DONE, userId, operationId);
            } catch (DataAdapterClientErrorException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while notifying Data Adapter", ex);
            }
        }
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
    protected UpdateOperationResponse failAuthorization(String operationId, String userId, List<KeyValueParameter> params) throws NextStepServiceException, AuthStepException {
        // validate operation before requesting update
        validateOperationState(operationId);
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.AUTH_FAILED, null, params);
        // notify Data Adapter in case operation is in FAILED state now
        if (response.getResponseObject().getResult()==AuthResult.FAILED) {
            try {
                dataAdapterClient.operationChangedNotification(OperationChange.FAILED, userId, operationId);
            } catch (DataAdapterClientErrorException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while notifying Data Adapter", ex);
            }
        }
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
    protected UpdateOperationResponse cancelAuthorization(String operationId, String userId, OperationCancelReason cancelReason, List<KeyValueParameter> params) throws NextStepServiceException, AuthStepException {
        // validate operation before requesting update
        validateOperationState(operationId);
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.CANCELED, cancelReason.toString(), params);
        // notify Data Adapter in case operation is in FAILED state now
        if (response.getResponseObject().getResult()==AuthResult.FAILED) {
            try {
                dataAdapterClient.operationChangedNotification(OperationChange.CANCELED, userId, operationId);
            } catch (DataAdapterClientErrorException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while notifying Data Adapter", ex);
            }
        }
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while initiating operation", e);
            return provider.failedAuthentication(null, "error.unknown");
        }
    }

    /**
     * Continue an operation.
     *
     * @param operationId ID of operation to be fetched.
     * @param provider    Provider that implements authentication callback.
     * @return Response indicating next step, based on provider response.
     */
    protected R continueOperationWithId(String operationId, AuthResponseProvider provider) {
        try {
            final GetOperationDetailResponse operation = getOperation(operationId);
            final String userId = operation.getUserId();
            filterStepsBasedOnActiveAuthMethods(operation.getSteps(), userId, operationId);
            if (userId != null) {
                authenticationManagementService.updateAuthenticationWithUserId(userId);
            }
            if (AuthResult.DONE.equals(operation.getResult())) {
                return provider.doneAuthentication(userId);
            } else {
                return provider.continueAuthentication(operationId, userId, operation.getSteps());
            }
        } catch (AuthStepException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while updating operation", e);
            return provider.failedAuthentication(null, e.getMessage());
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
                GetOperationDetailResponse operation = getOperation();
                // user was not authenticated - fail authorization
                authenticationManagementService.clearContext();
                responseObject = failAuthorization(operation.getOperationId(), null, null);
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while building authorization response", e);
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    protected void clearCurrentBrowserSession() {
        authenticationManagementService.clearContext();
    }

    protected void authenticateCurrentBrowserSession() {
        authenticationManagementService.authenticateCurrentSession();
        try {
            final GetOperationDetailResponse operation = getOperation();
            if (AuthResult.DONE.equals(operation.getResult())) {
                authenticationManagementService.pendingAuthenticationToAuthentication();
            }
        } catch (AuthStepException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while authenticating browser session", e);
        }
    }

    /**
     * Returns whether current authentication method is available in operation steps.
     * @param operation Operation.
     * @return Whether authentication method is available.
     */
    protected boolean isAuthMethodAvailable(GetOperationDetailResponse operation) {
        final AuthMethod currentAuthMethod = getAuthMethodName();
        return isAuthMethodAvailable(operation, currentAuthMethod);
    }

    /**
     * Returns whether authentication method is available in operation steps.
     * @param operation Operation.
     * @param authMethod Authentication method.
     * @return Whether authentication method is available.
     */
    private boolean isAuthMethodAvailable(GetOperationDetailResponse operation, AuthMethod authMethod) {
        for (AuthStep step: operation.getSteps()) {
            if (step.getAuthMethod() == authMethod) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve operation and validate it.
     * @param operationId Operation ID.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails.
     * @throws AuthStepException Thrown when operation state is invalid.
     */
    private void validateOperationState(String operationId) throws NextStepServiceException, AuthStepException {
        final ObjectResponse<GetOperationDetailResponse> operationDetail = nextStepClient.getOperationDetail(operationId);
        final GetOperationDetailResponse operation = operationDetail.getResponseObject();
        validateOperationState(operation);
    }

    /**
     * Validate that operation state is valid in current step.
     * @param operation Operation.
     * @throws AuthStepException Thrown when operation state is invalid.
     */
    private void validateOperationState(GetOperationDetailResponse operation) throws AuthStepException {
        if (operation == null) {
            throw new AuthStepException("operation.notAvailable", new NullPointerException());
        }
        final AuthMethod currentAuthMethod = getAuthMethodName();
        List<OperationHistory> operationHistoryList = operation.getHistory();
        if (operationHistoryList == null || operationHistoryList.isEmpty()) {
            throw new AuthStepException("operation.missingHistory", new IllegalStateException());
        }
        AuthMethod chosenAuthMethod = operation.getChosenAuthMethod();
        if (chosenAuthMethod != null) {
            // check that chosen authentication method matches next steps
            if (!isAuthMethodAvailable(operation, chosenAuthMethod)) {
                throw new AuthStepException("operation.invalidChosenMethod", new IllegalStateException());
            }
        }
        if (operation.getResult() == AuthResult.CONTINUE) {
            // check steps for operations with AuthResult = CONTINUE, DONE and FAILED methods do not have steps
            if (currentAuthMethod != AuthMethod.INIT && currentAuthMethod != AuthMethod.SHOW_OPERATION_DETAIL) {
                // check whether AuthMethod is available in next steps, only done in real authentication methods
                if (!isAuthMethodAvailable(operation)) {
                    throw new AuthStepException("operation.methodNotAvailable", new IllegalStateException());
                }
            }
            // special handling for SHOW_OPERATION_DETAIL - endpoint can be called only when either SMS_KEY or POWERAUTH_TOKEN are present in next steps
            if (currentAuthMethod == AuthMethod.SHOW_OPERATION_DETAIL) {
                if (!isAuthMethodAvailable(operation, AuthMethod.SMS_KEY) && !isAuthMethodAvailable(operation, AuthMethod.POWERAUTH_TOKEN)) {
                    throw new AuthStepException("operation.methodNotAvailable", new IllegalStateException());
                }
            }
        }
    }

    /**
     * Filter the list of steps based on current availability of authentication methods.
     * @param authSteps List of authentication steps.
     * @param userId User ID, use null for unknown user ID.
     * @param operationId Operation ID.
     */
    private void filterStepsBasedOnActiveAuthMethods(List<AuthStep> authSteps, String userId, String operationId) {
        Set<AuthStep> authStepsToRemove = new HashSet<>();
        for (AuthStep authStep: authSteps) {
            if (!authMethodQueryService.isAuthMethodEnabled(authStep.getAuthMethod(), userId, operationId)) {
                authStepsToRemove.add(authStep);
            }
        }
        authSteps.removeAll(authStepsToRemove);
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
