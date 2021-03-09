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
package io.getlime.security.powerauth.lib.webflow.authentication.sms.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Request for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class SmsAuthorizationRequest extends AuthStepRequest {

    @Pattern(regexp = "^[0-9]{4,8}$", message = "login.authenticationFailed")
    private String authCode;

    @Pattern(regexp = "^$|^.{4,128}$", message = "login.authenticationFailed")
    private String password;

    @NotNull(message = "error.invalidRequest")
    private List<AuthInstrument> authInstruments = new ArrayList<>();

    /**
     * Get authorization code from SMS message.
     * @return Authorization code from SMS message.
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Set authorization code from SMS message.
     * @param authCode Authorization code from SMS message.
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Get user password (optional).
     * @return User password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set user password (optional).
     * @param password User password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public List<AuthInstrument> getAuthInstruments() {
        return authInstruments;
    }

    /**
     * Set authorization instruments used in this request.
     * @param authInstruments Authorization instruments used in this request.
     */
    public void setAuthInstruments(List<AuthInstrument> authInstruments) {
        this.authInstruments = authInstruments;
    }
}
