/*
 * Copyright 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.base;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;

import java.util.List;

/**
 * Base class for any authentication step requests.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public abstract class AuthStepRequest {

    /**
     * Get authentication / authorization instruments used in this step.
     * @return Authentication / authorization instruments used in this step.
     */
    public abstract List<AuthInstrument> getAuthInstruments();
}
