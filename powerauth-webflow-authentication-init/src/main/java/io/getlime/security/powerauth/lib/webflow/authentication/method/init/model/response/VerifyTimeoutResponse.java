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
package io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

/**
 * Request for operation timeout verification including information about next timeout check.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifyTimeoutResponse extends AuthStepResponse {

    private Long timeoutWarningDelayMs;
    private Long timeoutDelayMs;

    /**
     * Get delay in milliseconds when timeout warning should be displayed.
     * @return Delay in milliseconds when timeout warning should be displayed.
     */
    public Long getTimeoutWarningDelayMs() {
        return timeoutWarningDelayMs;
    }

    /**
     * Set delay in milliseconds when timeout warning should be displayed.
     * @param timeoutWarningDelayMs Delay in milliseconds when timeout warning should be displayed.
     */
    public void setTimeoutWarningDelayMs(Long timeoutWarningDelayMs) {
        this.timeoutWarningDelayMs = timeoutWarningDelayMs;
    }

    /**
     * Get delay in milliseconds when operation is expected to time out.
     * @return Delay in milliseconds when operation is expected to time out.
     */
    public Long getTimeoutDelayMs() {
        return timeoutDelayMs;
    }

    /**
     * Set delay in milliseconds when operation is expected to time out.
     * @param timeoutDelayMs Delay in milliseconds when operation is expected to time out.
     */
    public void setTimeoutDelayMs(Long timeoutDelayMs) {
        this.timeoutDelayMs = timeoutDelayMs;
    }

}
