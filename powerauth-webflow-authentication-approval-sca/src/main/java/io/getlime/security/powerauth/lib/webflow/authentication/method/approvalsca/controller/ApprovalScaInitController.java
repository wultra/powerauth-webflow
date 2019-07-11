/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.controller;

import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.request.ApprovalScaInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.response.ApprovalScaInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller for initialization of SCA approval.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/approval-sca")
public class ApprovalScaInitController extends AuthMethodController<ApprovalScaInitRequest, ApprovalScaInitResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalScaInitController.class);

    private final NextStepClient nextStepClient;
    private final AuthMethodQueryService authMethodQueryService;

    /**
     * Controller constructor.
     * @param nextStepClient Next Step client.
     * @param authMethodQueryService Service for querying authentication methods.
     */
    @Autowired
    public ApprovalScaInitController(NextStepClient nextStepClient, AuthMethodQueryService authMethodQueryService) {
        this.nextStepClient = nextStepClient;
        this.authMethodQueryService = authMethodQueryService;
    }

    /**
     * Initialize SCA approval.
     * @param request Initialization request.
     * @return SCA approval initialization response.
     * @throws AuthStepException In case SCA approval initialization fails.
     * @throws NextStepServiceException In case communication with Next Step service fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    protected ApprovalScaInitResponse initScaApproval(@RequestBody ApprovalScaInitRequest request) throws AuthStepException, NextStepServiceException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step init started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        checkOperationExpiration(operation);
        String userId = operation.getUserId();
        if (userId == null) {
            // At this point user ID must be known, method cannot continue
            throw new InvalidRequestException("User ID is missing");
        }
        ApprovalScaInitResponse response = new ApprovalScaInitResponse();
        nextStepClient.updateChosenAuthMethod(operation.getOperationId(), AuthMethod.APPROVAL_SCA);
        // Find out whether mobile token is enabled
        boolean mobileTokenEnabled = false;
        try {
            if (authMethodQueryService.isMobileTokenAuthMethodAvailable(userId, operation.getOperationId())) {
                mobileTokenEnabled = true;
            }
        } catch (NextStepServiceException e) {
            logger.error(e.getMessage(), e);
        }
        response.setMobileTokenEnabled(mobileTokenEnabled);
        if (mobileTokenEnabled) {
            response.setResult(AuthStepResult.CONFIRMED);
            logger.debug("Step initialization succeeded with mobile token, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } else {
            response.setResult(AuthStepResult.CONFIRMED);
            logger.debug("Step initialization succeeded with SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Get current authentication method name.
     * @return Current authentication method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.APPROVAL_SCA;
    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public AuthStepResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (NextStepServiceException e) {
            logger.error("Error occurred in Next Step server", e);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

}
