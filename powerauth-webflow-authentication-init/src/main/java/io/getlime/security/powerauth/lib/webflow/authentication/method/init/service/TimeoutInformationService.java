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
