package io.getlime.security.powerauth.app.webauth.authentication.method.form;

import io.getlime.security.powerauth.app.webauth.exception.NextStepServiceException;
import io.getlime.security.powerauth.app.webauth.authentication.method.form.model.request.UsernamePasswordAuthenticationRequest;
import io.getlime.security.powerauth.app.webauth.authentication.method.form.model.response.UsernamePasswordAuthenticationResponse;
import io.getlime.security.powerauth.app.webauth.service.AuthenticationManagementService;
import io.getlime.security.powerauth.app.webauth.service.NextStepService;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClientErrorException;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
public class FormLoginController {

    @Autowired
    private CredentialStoreClient credentialStoreClient;

    @Autowired
    private AuthenticationManagementService authenticationManagementService;

    @Autowired
    private NextStepService nextStepService;

    @RequestMapping(value = "/api/authenticate", method = RequestMethod.POST)
    public @ResponseBody UsernamePasswordAuthenticationResponse authenticate(@RequestBody UsernamePasswordAuthenticationRequest request) {
        try {

            final Response<?> authenticateResponse = credentialStoreClient.authenticate(request.getUsername(), request.getPassword());
            AuthenticationResponse responseObject = (AuthenticationResponse) authenticateResponse.getResponseObject();
            String userId = responseObject.getUserId();

            String operationId = authenticationManagementService.updateAuthenticationWithUserId(userId);

            Response<UpdateOperationResponse> responseNS = nextStepService.updateOperation(operationId, userId, AuthMethod.USERNAME_PASSWORD_AUTH, AuthStepResult.CONFIRMED, null);
            UpdateOperationResponse responseObjectNS = responseNS.getResponseObject();
            if (responseObjectNS.getResult().equals(AuthResult.DONE)) {
                authenticationManagementService.authenticateCurrentSession();
                final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                response.setResult(AuthStepResult.CONFIRMED);
                response.setMessage("User was successfully authenticated.");
                return response;
            } else if (responseObjectNS.getResult().equals(AuthResult.FAILED)) {
                final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                response.setResult(AuthStepResult.FAILED);
                response.setMessage("Authentication failed.");
                AuthStep step = new AuthStep();
                return response;
            } else {
                final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                response.setResult(AuthStepResult.CONFIRMED);
                response.setMessage("User was successfully authenticated.");
                response.getNext().addAll(responseObjectNS.getSteps());
                return response;
            }
        } catch (NextStepServiceException e) {
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.FAILED);
            response.setMessage("Unable to determine next authentication method.");
            AuthStep step = new AuthStep();
            step.setAuthMethod(AuthMethod.USERNAME_PASSWORD_AUTH);
            step.setDescription("Send user to username and password auth after failed login.");
            response.getNext().add(step);
            return response;
        } catch (CredentialStoreClientErrorException e) {
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.FAILED);
            response.setMessage(e.getError().getMessage());
            AuthStep step = new AuthStep();
            step.setAuthMethod(AuthMethod.USERNAME_PASSWORD_AUTH);
            step.setDescription("Send user to username and password auth after failed login.");
            response.getNext().add(step);
            return response;
        }

    }

}
