/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webauth.authentication.method.init;

import io.getlime.security.powerauth.app.webauth.exception.NextStepServiceException;
import io.getlime.security.powerauth.app.webauth.authentication.method.init.model.response.InitOperationResponse;
import io.getlime.security.powerauth.app.webauth.authentication.method.init.model.request.InitOperationRequest;
import io.getlime.security.powerauth.app.webauth.service.AuthenticationManagementService;
import io.getlime.security.powerauth.app.webauth.service.NextStepService;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageController class handles responses to messages sent from the UI clients on different destinations.
 *
 * @author Roman Strobl
 */
@Controller
public class ApiController {

    /**
     * Next step service provides access to the Next Step server.
     */
    private final NextStepService nextStepService;

    /**
     * Service that manages authentication state.
     */
    private AuthenticationManagementService authenticationService;

    /**
     * Autowired dependencies.
     * @param nextStepService next step service
     */
    @Autowired
    public ApiController(NextStepService nextStepService, AuthenticationManagementService authenticationService) {
        this.nextStepService = nextStepService;
        this.authenticationService = authenticationService;
    }

    /**
     * Handles registration messages arriving from the clients.
     * @param initOperationRequest registration request message.
     * @return New registration response.
     * @throws Exception thrown in case of invalid messages.
     */
    @RequestMapping(value = "/api/init", method = RequestMethod.POST)
    public @ResponseBody InitOperationResponse register(@RequestBody InitOperationRequest initOperationRequest) throws Exception {
        String operationName = null;
        String operationData = null;
        List<KeyValueParameter> params = new ArrayList<>();
        if (initOperationRequest.getOperationId() == null) {
            operationName = "login";
            operationData = null;
        } else {
            //TODO: Fetch operation info from NS server
            //TODO: Check that operation can be completed by current user
            //TODO: Add code for updating existing operation, instead of creating one
        }

        try {
            Response<CreateOperationResponse> response = nextStepService.createOperation(operationName, operationData, params);
            CreateOperationResponse responseObject = (CreateOperationResponse) response.getResponseObject();
            String operationId = responseObject.getOperationId();

            authenticationService.createAuthenticationWithOperationId(operationId);

            InitOperationResponse registrationResponse = new InitOperationResponse();
            registrationResponse.setResult(AuthStepResult.CONFIRMED);
            registrationResponse.setOperationId(operationId);
            registrationResponse.getNext().addAll(responseObject.getSteps());
            return registrationResponse;

        } catch (NextStepServiceException e) {

            InitOperationResponse registrationResponse = new InitOperationResponse();
            registrationResponse.setResult(AuthStepResult.FAILED);
            registrationResponse.setOperationId(null);
            return registrationResponse;

        }

    }

}
