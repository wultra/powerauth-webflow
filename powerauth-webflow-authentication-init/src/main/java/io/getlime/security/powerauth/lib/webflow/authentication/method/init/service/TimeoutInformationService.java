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
package io.getlime.security.powerauth.lib.webflow.authentication.method.init.service;

import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service used for obtaining information about operation timeout.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class TimeoutInformationService {

    private final WebFlowServicesConfiguration configuration;

    @Autowired
    public TimeoutInformationService(WebFlowServicesConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Get timeout delay for operation in milliseconds.
     * @param operation Operation.
     * @return Timeout delay for operation in milliseconds.
     */
    public long getTimeoutDelay(GetOperationDetailResponse operation) {
        if (operation == null) {
            return 0;
        }
        long timeoutDelayMs = operation.getTimestampExpires().getTime() - System.currentTimeMillis();
        if (timeoutDelayMs > 0) {
            return timeoutDelayMs;
        }
        return 0;
    }

    /**
     * Get timeout warning delay for operation in milliseconds.
     * @param operation Operation.
     * @return Timeout warning delay for operation in milliseconds.
     */
    public long getTimeoutWarningDelay(GetOperationDetailResponse operation) {
        if (operation == null) {
            return 0;
        }
        long timeoutWarningDelayMs = operation.getTimestampExpires().getTime() - System.currentTimeMillis() - configuration.getTimeoutWarningDelay();
        if (timeoutWarningDelayMs > 0) {
            return timeoutWarningDelayMs;
        }
        return 0;
    }

}
