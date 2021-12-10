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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

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

}
