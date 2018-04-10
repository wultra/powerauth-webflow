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
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
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
import io.getlime.security.powerauth.lib.webflow.authentication.exception.*;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.OperationSessionEntity;
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.MessageTranslationService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base controller for any authentication method. Controller class is templated using three attributes.
 *
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

    @Autowired
    private OperationSessionService operationSessionService;

    @Autowired
    private HttpServletRequest request;

    /**
     * Get operation detail.
     * @return Operation detail.
     * @throws AuthStepException Thrown when operation could not be retrieved or it is not available.
     */
    protected GetOperationDetailResponse getOperation() throws AuthStepException {
        final UserOperationAuthentication pendingUserAuthentication = authenticationManagementService.getPendingUserAuthentication();
        if (pendingUserAuthentication != null) {
            String operationId = pendingUserAuthentication.getOperationId();
            if (operationId != null) {
                return getOperation(operationId);
            } else {
                throw new OperationNotAvailableException("Operation is not available");
            }
        } else {
            throw new OperationNotAvailableException("Operation is not available");
        }
    }

    /**
     * Get operation detail with given operation ID.
     * @param operationId Operation ID.
     * @return Operation detail.
     * @throws AuthStepException Thrown when operation could not be retrieved or it is not available.
     */
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

    /**
     * Get current authentication method.
     * @return Current authentication method.
     */
    abstract protected AuthMethod getAuthMethodName();

    /**
     * Get pending operations for given user.
     * @param userId User ID.
     * @return List of operations for given user.
     */
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
     * @throws AuthStepException In case authorization fails.
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
     * @throws AuthStepException In case authorization fails.
     */
    protected UpdateOperationResponse authorize(String operationId, String userId, List<KeyValueParameter> params) throws NextStepServiceException, AuthStepException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authorization started, operation ID: {0}, user ID: {1}, authentication method: {2}", new String[] {operationId, userId, getAuthMethodName().toString()});
        // validate operation before requesting update
        GetOperationDetailResponse operation = validateOperationState(operationId);
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.CONFIRMED, null, params);
        // notify Data Adapter in case operation is in DONE state now
        if (response.getResponseObject().getResult()==AuthResult.DONE) {
            try {
                FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
                OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData);
                dataAdapterClient.operationChangedNotification(OperationChange.DONE, userId, operationContext);
            } catch (DataAdapterClientErrorException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while notifying Data Adapter", ex);
            }
        }
        // update operation result in operation to HTTP session mapping
        operationSessionService.updateOperationResult(operationId, response.getResponseObject().getResult());
        filterStepsBasedOnActiveAuthMethods(response.getResponseObject().getSteps(), userId, operationId);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authorization succeeded, operation ID: {0}, user ID: {1}, authentication method: {2}", new String[] {operationId, userId, getAuthMethodName().toString()});
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
     * @throws AuthStepException In case authorization fails.
     */
    protected UpdateOperationResponse failAuthorization(String operationId, String userId, List<KeyValueParameter> params) throws NextStepServiceException, AuthStepException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Fail step started, operation ID: {0}, user ID: {1}, authentication method: {2}", new String[] {operationId, userId, getAuthMethodName().toString()});
        // validate operation before requesting update
        GetOperationDetailResponse operation = validateOperationState(operationId);
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.AUTH_FAILED, null, params);
        // notify Data Adapter in case operation is in FAILED state now
        if (response.getResponseObject().getResult()==AuthResult.FAILED) {
            try {
                FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
                OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData);
                dataAdapterClient.operationChangedNotification(OperationChange.FAILED, userId, operationContext);
            } catch (DataAdapterClientErrorException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while notifying Data Adapter", ex);
            }
        }
        // update operation result in operation to HTTP session mapping
        operationSessionService.updateOperationResult(operationId, response.getResponseObject().getResult());
        filterStepsBasedOnActiveAuthMethods(response.getResponseObject().getSteps(), userId, operationId);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Fail step succeeded, operation ID: {0}, user ID: {1}, authentication method: {2}", new String[] {operationId, userId, getAuthMethodName().toString()});
        return response.getResponseObject();
    }

    /**
     * @param operationId  Operation ID of operation to cancel.
     * @param userId       User ID of user who owns the operation.
     * @param params       Custom parameters.
     * @param cancelReason Reason for cancellation of the operation.
     * @return Response with information about operation update result.
     * @throws NextStepServiceException In case communication fails.
     * @throws AuthStepException In case authorization fails.
     */
    protected UpdateOperationResponse cancelAuthorization(String operationId, String userId, OperationCancelReason cancelReason, List<KeyValueParameter> params) throws NextStepServiceException, AuthStepException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step cancel started, operation ID: {0}, authentication method: {1}", new String[]{operationId, getAuthMethodName().toString()});
        // validate operation before requesting update
        GetOperationDetailResponse operation = validateOperationState(operationId);
        ObjectResponse<UpdateOperationResponse> response = nextStepClient.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.CANCELED, cancelReason.toString(), params);
        // notify Data Adapter in case operation is in FAILED state now
        if (response.getResponseObject().getResult()==AuthResult.FAILED) {
            try {
                FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
                OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData);
                dataAdapterClient.operationChangedNotification(OperationChange.CANCELED, userId, operationContext);
            } catch (DataAdapterClientErrorException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while notifying Data Adapter", ex);
            }
        }
        // update operation result in operation to HTTP session mapping
        operationSessionService.updateOperationResult(operationId, response.getResponseObject().getResult());
        filterStepsBasedOnActiveAuthMethods(response.getResponseObject().getSteps(), userId, operationId);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step cancel succeeded, operation ID: {0}, authentication method: {1}", new String[]{operationId, getAuthMethodName().toString()});
        return response.getResponseObject();
    }

    /**
     * Initiate a new operation with given name, data and parameters.
     *
     * @param operationName Name of the operation to be created.
     * @param operationData Data of the operation.
     * @param formData      Form data used for displaying the operation details.
     * @param httpSessionId HTTP session ID.
     * @param params        Additional parameters of the operation.
     * @param provider      Provider that implements authentication callback.
     * @return Response indicating next step, based on provider response.
     */
    protected R initiateOperationWithName(String operationName, String operationData, OperationFormData formData, String httpSessionId, List<KeyValueParameter> params, AuthResponseProvider provider) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation initiate with name started, operation name: {0}", operationName);
        try {
            ObjectResponse<CreateOperationResponse> response = nextStepClient.createOperation(operationName, operationData, formData, params);
            CreateOperationResponse responseObject = response.getResponseObject();
            String operationId = responseObject.getOperationId();
            // persist mapping of operation to HTTP session
            operationSessionService.persistOperationToSessionMapping(operationId, httpSessionId, responseObject.getResult());
            filterStepsBasedOnActiveAuthMethods(responseObject.getSteps(), null, operationId);
            authenticationManagementService.createAuthenticationWithOperationId(operationId);
            R initResponse = provider.continueAuthentication(operationId, null, responseObject.getSteps());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation initiate succeeded, operation name: {0}", operationName);
            return initResponse;
        } catch (NextStepServiceException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while initiating operation", e);
            return provider.failedAuthentication(null, "error.unknown");
        }
    }

    /**
     * Continue an existing operation.
     *
     * @param operationId ID of operation to be fetched.
     * @param httpSessionId HTTP session ID.
     * @param provider    Provider that implements authentication callback.
     * @return Response indicating next step, based on provider response.
     */
    protected R continueOperationWithId(String operationId, String httpSessionId, AuthResponseProvider provider) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation continue with ID started, operation ID: {0}", operationId);
        try {
            final GetOperationDetailResponse operation = getOperation(operationId);
            // check whether session is already initiated - page refresh could cause double initialization
            // if it is not initiated yet, persist operation to session mapping
            OperationSessionEntity operationSessionEntity = operationSessionService.getOperationToSessionMapping(operationId);
            if (operationSessionEntity == null) {
                // cancel previous active operations
                cancelOperationsInHttpSession(httpSessionId);
                // persist mapping of operation to HTTP session
                operationSessionService.persistOperationToSessionMapping(operationId, httpSessionId, operation.getResult());
            }
            final String userId = operation.getUserId();
            filterStepsBasedOnActiveAuthMethods(operation.getSteps(), userId, operationId);
            if (userId != null) {
                authenticationManagementService.updateAuthenticationWithUserId(userId);
            }
            if (AuthResult.DONE.equals(operation.getResult())) {
                R done = provider.doneAuthentication(userId);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation continue succeeded, result is DONE, operation ID: {0}", operationId);
                return done;
            } else {
                R cont = provider.continueAuthentication(operationId, userId, operation.getSteps());
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation continue succeeded, result is CONTINUE, operation ID: {0}", operationId);
                return cont;
            }
        } catch (AuthStepException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred while updating operation", e);
            return provider.failedAuthentication(null, e.getMessage());
        }
    }

    /**
     * Cancel active operations within HTTP session.
     *
     * @param httpSessionId HTTP session ID.
     */
    private void cancelOperationsInHttpSession(String httpSessionId) {
        // at first cancel operations within same HTTP session in the operation to session mapping
        List<OperationSessionEntity> operationsToCancel = operationSessionService.cancelOperationsInHttpSession(httpSessionId);
        for (OperationSessionEntity operationToCancel: operationsToCancel) {
            try {
                // cancel operations in Next Step
                final ObjectResponse<GetOperationDetailResponse> operation = nextStepClient.getOperationDetail(operationToCancel.getOperationId());
                final GetOperationDetailResponse operationDetail = operation.getResponseObject();
                nextStepClient.updateOperation(operationDetail.getOperationId(), operationDetail.getUserId(), getAuthMethodName(), AuthStepResult.CANCELED, OperationCancelReason.INTERRUPTED_OPERATION.toString(), null);
                // notify Data Adapter about cancellation
                FormData formData = new FormDataConverter().fromOperationFormData(operation.getResponseObject().getFormData());
                OperationContext operationContext = new OperationContext(operationDetail.getOperationId(), operationDetail.getOperationName(), operationDetail.getOperationData(), formData);
                dataAdapterClient.operationChangedNotification(OperationChange.CANCELED, operationDetail.getUserId(), operationContext);
            } catch (NextStepServiceException | DataAdapterClientErrorException e) {
                // errors occurring when canceling previous operations are not critical
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while canceling previous operation", e);
            }
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

    /**
     * Clear current browser session.
     */
    protected void clearCurrentBrowserSession() {
        authenticationManagementService.clearContext();
    }

    /**
     * Authenticate current browser session.
     */
    protected void authenticateCurrentBrowserSession() {
        authenticationManagementService.authenticateCurrentSession();
        try {
            final GetOperationDetailResponse operation = getOperation();
            if (AuthResult.DONE.equals(operation.getResult())) {
                authenticationManagementService.pendingAuthenticationToAuthentication();
            }
        } catch (AuthStepException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred while authenticating browser session", e);
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
    private GetOperationDetailResponse validateOperationState(String operationId) throws NextStepServiceException, AuthStepException {
        final ObjectResponse<GetOperationDetailResponse> operationDetail = nextStepClient.getOperationDetail(operationId);
        final GetOperationDetailResponse operation = operationDetail.getResponseObject();
        validateOperationState(operation);
        return operation;
    }

    /**
     * Validate that operation state is valid in current step.
     * @param operation Operation.
     * @throws AuthStepException Thrown when operation state is invalid.
     */
    private void validateOperationState(GetOperationDetailResponse operation) throws AuthStepException {
        if (operation == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Operation is not available");
            throw new OperationNotAvailableException("Operation is not available");
        }
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Validate operation started, operation ID: {0}", operation.getOperationId());
        if (operation.getResult() == AuthResult.FAILED) {
            List<OperationHistory> operationHistory = operation.getHistory();
            if (operationHistory.size() == 0 || operationHistory.get(operationHistory.size()-1).getRequestAuthStepResult() != AuthStepResult.CANCELED) {
                // allow displaying of canceled operations - operation may be canceled in mobile app and later displayed in web UI
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Operation has already failed, operation ID: {0}", operation.getOperationId());
                throw new OperationAlreadyFailedException("Operation has already failed");
            }
        }
        if (operation.isExpired()) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Operation has timed out, operation ID: {0}", operation.getOperationId());
            throw new OperationTimeoutException("Operation has timed out");
        }
        final AuthMethod currentAuthMethod = getAuthMethodName();
        List<OperationHistory> operationHistoryList = operation.getHistory();
        if (operationHistoryList == null || operationHistoryList.isEmpty()) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Operation is missing its history, operation ID: {0}", operation.getOperationId());
            throw new OperationMissingHistoryException("Operation is missing its history");
        }
        AuthMethod chosenAuthMethod = operation.getChosenAuthMethod();
        if (chosenAuthMethod != null) {
            // check that chosen authentication method matches next steps
            if (!isAuthMethodAvailable(operation, chosenAuthMethod)) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Invalid chosen authentication method, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), chosenAuthMethod.toString()});
                throw new InvalidChosenMethodException("Invalid chosen authentication method: "+chosenAuthMethod);
            }
        }
        if (operation.getResult() == AuthResult.CONTINUE) {
            // verify operation hash
            String clientOperationHash = request.getHeader("X-OPERATION-HASH");
            String currentOperationHash = operationSessionService.generateOperationHash(operation.getOperationId());
            // mobile API clients do not send operation hash - when operation hash is missing, concurrency check is not performed
            if (clientOperationHash != null && !clientOperationHash.equals(currentOperationHash)) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Operation was interrupted, operation ID: {0}", operation.getOperationId());
                throw new OperationInterruptedException("Operation was interrupted");
            }
            // check steps for operations with AuthResult = CONTINUE, DONE and FAILED methods do not have steps
            if (currentAuthMethod != AuthMethod.INIT && currentAuthMethod != AuthMethod.SHOW_OPERATION_DETAIL) {
                // check whether AuthMethod is available in next steps, only done in real authentication methods
                if (!isAuthMethodAvailable(operation)) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Authentication method is not available, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), currentAuthMethod.toString()});
                    throw new AuthMethodNotAvailableException("Authentication method is not available: " + currentAuthMethod);
                }
            }
            // special handling for SHOW_OPERATION_DETAIL - endpoint can be called only when either SMS_KEY or POWERAUTH_TOKEN are present in next steps
            if (currentAuthMethod == AuthMethod.SHOW_OPERATION_DETAIL) {
                if (!isAuthMethodAvailable(operation, AuthMethod.SMS_KEY) && !isAuthMethodAvailable(operation, AuthMethod.POWERAUTH_TOKEN)) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Authentication method is not available, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), currentAuthMethod.toString()});
                    throw new AuthMethodNotAvailableException("Authentication method is not available: " + currentAuthMethod);
                }
            }
        }
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Operation validation succeeded, operation ID: {0}", operation.getOperationId());
    }

    /**
     * Resolves the number of remaining authentication attempts.
     * @param remainingAttemptsDA Number of remaining attempts from Data Adapter.
     * @param remainingAttemptsNS Number of remaining attempts from Next Step.
     * @return Resolved number of remaining attempts. Null value is returned for no limit.
     */
    protected Integer resolveRemainingAttempts(Integer remainingAttemptsDA, Integer remainingAttemptsNS) {
        if (remainingAttemptsDA == null && remainingAttemptsNS == null) {
            // no remaining attempts are set
            return null;
        } else if (remainingAttemptsDA == null) {
            // only NS remaining attempts are set
            return remainingAttemptsNS;
        } else if (remainingAttemptsNS == null) {
            // only DA remaining attempts are set
            return remainingAttemptsDA;
        } else if (remainingAttemptsDA < remainingAttemptsNS) {
            // DA has smaller number of remaining attempts
            return remainingAttemptsDA;
        } else {
            // NS has smaller number of remaining attempts
            return remainingAttemptsNS;
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
