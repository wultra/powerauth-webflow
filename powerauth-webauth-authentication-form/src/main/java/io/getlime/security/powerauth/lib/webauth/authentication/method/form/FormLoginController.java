package io.getlime.security.powerauth.lib.webauth.authentication.method.form;

import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.method.form.model.request.UsernamePasswordAuthenticationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.method.form.model.response.UsernamePasswordAuthenticationResponse;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClientErrorException;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
public class FormLoginController extends AuthMethodController<UsernamePasswordAuthenticationRequest, UsernamePasswordAuthenticationResponse, AuthStepException> {

    @Autowired
    private CredentialStoreClient credentialStoreClient;

    @Override
    protected String authenticate(UsernamePasswordAuthenticationRequest request) throws AuthStepException {
        try {
            final Response<AuthenticationResponse> authenticateResponse = credentialStoreClient.authenticate(request.getUsername(), request.getPassword());
            AuthenticationResponse responseObject = authenticateResponse.getResponseObject();
            return responseObject.getUserId();
        } catch (CredentialStoreClientErrorException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    @RequestMapping(value = "/api/authenticate", method = RequestMethod.POST)
    public @ResponseBody UsernamePasswordAuthenticationResponse authenticateHandler(@RequestBody UsernamePasswordAuthenticationRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public UsernamePasswordAuthenticationResponse doneAuthentication() {
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("User was successfully authenticated.");
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse failedAuthentication() {
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.FAILED);
                    response.setMessage("Authentication failed.");
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse continueAuthentication(String operationId, List<AuthStep> steps) {
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("User was successfully authenticated.");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.FAILED);
            response.setMessage(e.getMessage());
            return response;
        }

    }

}
