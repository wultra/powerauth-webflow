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
package io.getlime.security.powerauth.lib.webflow.authentication.consent.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import java.util.Collections;
import java.util.List;

/**
 * Request for OAuth 2.0 consent.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentAuthRequest extends AuthStepRequest {

    private List<ConsentOption> options;

    public ConsentAuthRequest() {
    }

    public ConsentAuthRequest(List<ConsentOption> options) {
        this.options = options;
    }

    public List<ConsentOption> getOptions() {
        return options;
    }

    @Override
    public List<AuthInstrument> getAuthInstruments() {
        return Collections.emptyList();
    }
}
