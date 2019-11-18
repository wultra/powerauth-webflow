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
package io.getlime.security.powerauth.lib.webflow.authentication.method.init.controller;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.request.VerifyTimeoutRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.response.VerifyTimeoutResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.init.service.TimeoutInformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for operation timeouts. Note that this controller does not have regular /authenticate method, because
 * such method is not applicable to timeouts.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/timeout")
public class TimeoutController extends AuthMethodController<VerifyTimeoutRequest, VerifyTimeoutResponse, AuthStepException> {

    public static final Logger logger = LoggerFactory.getLogger(TimeoutController.class);

    private final TimeoutInformationService timeoutService;

    /**
     * Default constructor.
     * @param timeoutService Service for obtaining timeout information.
     */
    @Autowired
    public TimeoutController(TimeoutInformationService timeoutService) {
        this.timeoutService = timeoutService;
    }

    /**
     * Verify operation timeout and get timeout detail.
     * @param request Init timeout request.
     * @return Init timeout response.
     * @throws AuthStepException Thrown when request is invalid.
     */
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public VerifyTimeoutResponse verifyTimeout(VerifyTimeoutRequest request) throws AuthStepException {
        if (request == null) {
            throw new AuthStepException("Invalid request in verifyTimeout", "error.invalidRequest");
        }
        final GetOperationDetailResponse operation = getOperation();
        logger.debug("Verify timeout started, operation: {}", operation.getOperationId());
        final VerifyTimeoutResponse response = new VerifyTimeoutResponse();

        response.setTimeoutDelayMs(timeoutService.getTimeoutDelay(operation));
        response.setTimeoutWarningDelayMs(timeoutService.getTimeoutWarningDelay(operation));

        logger.debug("Verify timeout succeeded, operation: {}", operation.getOperationId());
        return response;
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.INIT;
    }
}
