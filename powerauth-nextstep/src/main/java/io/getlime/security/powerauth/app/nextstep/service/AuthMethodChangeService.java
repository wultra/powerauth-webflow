/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service handles authentication method changes within an operation with specific logic
 * for various use cases.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthMethodChangeService {

    private static final Logger logger = LoggerFactory.getLogger(AuthMethodChangeService.class);

    /**
     * Downgrade an authentication method for an operation.
     * @param request Update operation request.
     * @return Update operation response.
     */
    public UpdateOperationResponse downgradeAuthMethod(UpdateOperationRequest request, UpdateOperationResponse response, List<StepDefinitionEntity> stepDefinitions) {
        AuthMethod targetAuthMethod = request.getTargetAuthMethod();
        if (targetAuthMethod == null) {
            // Invalid request - authentication method downgrade expects a target authentication method
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
            return response;
        }
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            if (stepDef.getResponseAuthMethod() == targetAuthMethod) {
                AuthStep authStep = new AuthStep();
                authStep.setAuthMethod(targetAuthMethod);
                response.getSteps().add(authStep);
                response.setResult(AuthResult.CONTINUE);
                return response;
            }
        }
        // Target authentication method set for downgrade is not available
        response.setResult(AuthResult.FAILED);
        response.setResultDescription("error.noAuthMethod");
        return response;
    }

    /**
     * Set chosen authentication method for an operation.
     * @param request Update operation request.
     * @return Update operation response.
     */
    public UpdateOperationResponse setChosenAuthMethod(UpdateOperationRequest request, UpdateOperationResponse response) {
        // TODO
        return response;
    }

}
