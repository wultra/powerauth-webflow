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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOperationDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * Controller class related to PowerAuth activation management.
 *
 * @author Petr Dvorak
 */
@Controller
public class OperationController {

    /**
     * Create a new operation with given name and data.
     * @param request Create operation request.
     * @return Create operation response.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.POST)
    public @ResponseBody Response<CreateOperationResponse> createOperation(@RequestBody Request<CreateOperationRequest> request) {
        CreateOperationResponse response = new CreateOperationResponse();
        response.setOperationId("40269145-d91f-4579-badd-c57fa1133239");
        response.setResult(AuthResult.CONTINUE);
        response.setResultDescription("Continue authentication by sending user to the login screen.");
        response.setTimestampCreated(new Date());
        response.setTimestampExpires(new DateTime().plusMinutes(5).toDate());

        AuthStep authStep = new AuthStep();
        authStep.setAuthMethod(AuthMethod.USERNAME_PASSWORD_AUTH);
        authStep.setDescription("Username and password login");
        response.getSteps().add(authStep);

        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Update operation with given ID with a previous authentication step result.
     * @param request Update operation request.
     * @return Update operation response.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.PUT)
    public @ResponseBody Response<UpdateOperationResponse> updateOperation(@RequestBody Request<UpdateOperationRequest> request) {

        UpdateOperationRequest requestObject = request.getRequestObject();

        UpdateOperationResponse response = new UpdateOperationResponse();
        response.setOperationId(requestObject.getOperationId());
        response.setUserId(requestObject.getUserId());
        response.setTimestampCreated(new Date());
        response.setTimestampExpires(new DateTime().plusMinutes(5).toDate());

        if (AuthStepResult.CONFIRMED.equals(requestObject.getAuthStepResult())) {
            response.setResult(AuthResult.DONE);
            response.setResultDescription("Authentication was successfully completed.");
        } else if (AuthStepResult.CANCELED.equals(requestObject.getAuthStepResult())) {
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("User cancelled the authentication.");
        } else {
            response.setResult(AuthResult.CONTINUE);
            response.setResultDescription("Continue authentication by sending user to the login screen.");

            AuthStep authStep = new AuthStep();
            authStep.setAuthMethod(AuthMethod.USERNAME_PASSWORD_AUTH);
            authStep.setDescription("Username and password login");
            response.getSteps().add(authStep);
        }
        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Get detail of an operation with given ID.
     * @param request Get operation detail request.
     * @return Get operation detail response.
     */
    @RequestMapping(value = "/operation/detail", method = RequestMethod.POST)
    public @ResponseBody Response<GetOperationDetailResponse> operationDetail(@RequestBody Request<GetOperationDetailRequest> request) {

        GetOperationDetailRequest requestObject = request.getRequestObject();

        GetOperationDetailResponse response = new GetOperationDetailResponse();
        response.setOperationId(requestObject.getOperationId());
        response.setUserId("26");
        response.setOperationData("{\"amount\":100,\"currency\":\"CZK\",\"to\":\"CZ12000012345678901234\"}");
        response.setResult(AuthResult.DONE);
        response.setResultDescription("Authentication was successfully completed");
        response.setTimestampCreated(new Date());
        response.setTimestampExpires(new DateTime().plusMinutes(5).toDate());
        return new Response<>(Response.Status.OK, response);
    }

}
