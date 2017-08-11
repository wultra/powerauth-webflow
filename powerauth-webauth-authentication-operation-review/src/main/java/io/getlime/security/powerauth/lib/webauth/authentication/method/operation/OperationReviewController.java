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

package io.getlime.security.powerauth.lib.webauth.authentication.method.operation;

import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.method.operation.model.request.OperationReviewRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.method.operation.model.response.OperationReviewDetailResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.method.operation.model.response.OperationReviewResponse;
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
@RequestMapping(value = "/api/auth/operation")
public class OperationReviewController extends AuthMethodController<OperationReviewRequest, OperationReviewResponse, AuthStepException> {

    @Override
    protected String authenticate(OperationReviewRequest request) throws AuthStepException {
        //TODO: Check pre-authenticated user here
        return getOperation().getUserId();
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.SHOW_OPERATION_DETAIL;
    }

    @RequestMapping(value = "/detail")
    public @ResponseBody OperationReviewDetailResponse getOperationDetails() {
        final GetOperationDetailResponse operation = getOperation();
        if (operation != null) {
            OperationReviewDetailResponse response = new OperationReviewDetailResponse();
            response.setData(operation.getOperationData());
            response.setDisplayDetails(operation.getDisplayDetails());
            return response;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody OperationReviewResponse getOperationDetails(@RequestBody OperationReviewRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public OperationReviewResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    return response;
                }

                @Override
                public OperationReviewResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    return response;
                }

                @Override
                public OperationReviewResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody
    OperationReviewResponse cancelAuthentication() {
        try {
            cancelAuthorization(getOperation().getOperationId(), null, "UNKNOWN", null);
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            return response;
        } catch (NextStepServiceException e) {
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

}
