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

package io.getlime.security.powerauth.lib.webauth.authentication.controller;

import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.base.AuthStepRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.service.AuthenticationManagementService;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Base controller for any authentication method. Controller class is templated using three attributes.
 *
 * <ul>
 *     <li>T - extension of AuthStepRequest.</li>
 *     <li>R - extension of AuthStepResponse.</li>
 *     <li>E - extension of AuthStepException.</li>
 * </ul>
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Component
public class AuthMethodController<T extends AuthStepRequest, R extends AuthStepResponse, E extends AuthStepException> {

    @Autowired
    private AuthenticationManagementService authenticationManagementService;

    @Autowired

    private NextStepClient nextStepService;

    protected GetOperationDetailResponse getOperation() {
        try {
            String operationId = authenticationManagementService.getPendingUserAuthentication().getOperationId();
            final Response<GetOperationDetailResponse> operationDetail = nextStepService.getOperationDetail(operationId);
            final GetOperationDetailResponse responseObject = operationDetail.getResponseObject();
            return responseObject;
        } catch (NextStepServiceException e) {
            return null;
        }
    }

    protected AuthMethod getAuthMethodName() {
        return AuthMethod.USERNAME_PASSWORD_AUTH;
    }

    /**
     * Method to authenticate user with provided request object.
     * @param request Request with authentication object information.
     * @return String with user ID.
     * @throws E In case error occurs during authentication.
     */
    protected String authenticate(T request) throws E {
        return null;
    }

    /**
     * Initiate a new operation with given name, data and parameters.
     * @param operationName Name of the operation to be created.
     * @param operationData Data of the operation.
     * @param params Additional parameters of the operation.
     * @param provider Provider that implements authentication callback.
     * @return Response indicating next step, based on provider response.
     */
    protected R initiateOperationWithName(String operationName, String operationData, List<KeyValueParameter> params, AuthResponseProvider provider) {
        try {
            Response<CreateOperationResponse> response = nextStepService.createOperation(operationName, operationData, params);
            CreateOperationResponse responseObject = response.getResponseObject();
            String operationId = responseObject.getOperationId();
            authenticationManagementService.createAuthenticationWithOperationId(operationId);
            return provider.continueAuthentication(operationId, responseObject.getSteps());
        } catch (NextStepServiceException e) {
            return provider.failedAuthentication();
        }
    }


    /**
     * Build next authentication step information for given operation.
     * @param request Request containing information about the current authentication.
     * @param provider Provider with authentication callback implementation.
     * @return Response indicating next step, based on provider response.
     * @throws AuthStepException In case authentication fails.
     */
    protected R buildAuthorizationResponse(T request, AuthResponseProvider provider) throws AuthStepException {
        try {
            String userId = authenticate(request);
            String operationId = authenticationManagementService.updateAuthenticationWithUserId(userId);
            Response<UpdateOperationResponse> response = nextStepService.updateOperation(operationId, userId, getAuthMethodName(), AuthStepResult.CONFIRMED, null);
            UpdateOperationResponse responseObject = response.getResponseObject();
            switch (responseObject.getResult()) {
                case DONE: {
                    authenticationManagementService.authenticateCurrentSession();
                    return provider.doneAuthentication();
                }
                case FAILED: {
                    authenticationManagementService.clearContext();
                    return provider.failedAuthentication();
                }
                case CONTINUE: {
                    return provider.continueAuthentication(operationId, responseObject.getSteps());
                }
                default: {
                    authenticationManagementService.clearContext();
                    return provider.failedAuthentication();
                }
            }
        } catch (NextStepServiceException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    /**
     * Class providing callbacks for operation authentication outcomes.
     */
    public abstract class AuthResponseProvider {

        /**
         * Called in case user successfully authenticated and no other authentication is needed.
         * @return Information about successful authentication, confirmation step.
         */
        public abstract R doneAuthentication();

        /**
         * Called in case authentication fails and no other steps can be performed.
         * @return Information about authentication failure, error step.
         */
        public abstract R failedAuthentication();

        /**
         * Called in case authentication should continue with next step(s).
         * @param operationId Operation ID of the current operation.
         * @param steps List of next steps to be performed.
         * @return Information about next steps for given operation.
         */
        public abstract R continueAuthentication(String operationId, List<AuthStep> steps);
    }


}
