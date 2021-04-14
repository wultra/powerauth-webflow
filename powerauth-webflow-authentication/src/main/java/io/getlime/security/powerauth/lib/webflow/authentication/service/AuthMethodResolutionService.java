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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import org.springframework.stereotype.Service;

/**
 * Service for resolution of authentication methods. SCA methods override authentication method name to support
 * delegation to other authentication methods.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthMethodResolutionService {

    /**
     * Check whether current authentication method is overridden. Overriding is used for SCA methods which delegate
     * functionality to other methods.
     * @param operation Operation.
     * @return Override authentication method or null in case authentication method is not overridden.
     */
    public AuthMethod resolveAuthMethodOverride(GetOperationDetailResponse operation) {
        if (operation == null || operation.getHistory().isEmpty()) {
            return null;
        }
        OperationHistory currentHistory = operation.getHistory().get(operation.getHistory().size() - 1);
        // Handle special case when LOGIN_SCA method is used, this authentication method delegates work
        // to other authentication methods. The first case is when current step is confirmed and contains LOGIN_SCA
        // as chosen authentication method (typically in INIT step).
        // Same logic is valid for APPROVAL_SCA method.
        if (currentHistory.getRequestAuthStepResult() == AuthStepResult.CONFIRMED &&
                (operation.getChosenAuthMethod() == AuthMethod.LOGIN_SCA || operation.getChosenAuthMethod() == AuthMethod.APPROVAL_SCA)) {
            return operation.getChosenAuthMethod();
        }
        // The second case is when the current step has LOGIN_SCA or APPROVAL_SCA as an authentication method and next
        // authentication method has not been chosen yet.
        if (operation.getChosenAuthMethod() == null &&
                (currentHistory.getAuthMethod() == AuthMethod.LOGIN_SCA || currentHistory.getAuthMethod() == AuthMethod.APPROVAL_SCA)) {
            return currentHistory.getAuthMethod();
        }
        // Regular case with no delegation.
        return null;
    }

    /**
     * Update operation for SCA login for first step of an approval operation.
     * @param operation Operation to update.
     */
    public void updateOperationForScaLogin(GetOperationDetailResponse operation) {
        // Make sure Mobile Token and Data Adapter recognize the operation name
        operation.setOperationName("login");
        // Update operation data for login
        operation.setOperationData("A2");
        // Update operation form data
        OperationFormData formData = new OperationFormData();
        formData.addTitle("login.title");
        formData.addGreeting("login.greeting");
        formData.addSummary("login.summary");
        formData.getUserInput().putAll(operation.getFormData().getUserInput());
        operation.setFormData(formData);
    }

}
