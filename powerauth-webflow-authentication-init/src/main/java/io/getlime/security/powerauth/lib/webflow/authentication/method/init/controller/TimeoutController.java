/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @PostMapping("/verify")
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
