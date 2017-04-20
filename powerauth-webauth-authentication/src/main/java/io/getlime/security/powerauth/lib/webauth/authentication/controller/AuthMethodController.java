package io.getlime.security.powerauth.lib.webauth.authentication.controller;

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
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Component
public class AuthMethodController<T extends AuthStepRequest, R extends AuthStepResponse, E extends AuthStepException> {

    @Autowired
    private AuthenticationManagementService authenticationManagementService;

    @Autowired
    private NextStepClient nextStepService;

    protected String authenticate(T request) throws E {
        return null;
    }

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


    protected R buildAuthorizationResponse(T request, AuthResponseProvider provider) throws AuthStepException {
        try {
            String userId = authenticate(request);
            String operationId = authenticationManagementService.updateAuthenticationWithUserId(userId);
            Response<UpdateOperationResponse> response = nextStepService.updateOperation(operationId, userId, AuthMethod.USERNAME_PASSWORD_AUTH, AuthStepResult.CONFIRMED, null);
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

    public abstract class AuthResponseProvider {
        public abstract R doneAuthentication();
        public abstract R failedAuthentication();
        public abstract R continueAuthentication(String operationId, List<AuthStep> steps);
    }


}
