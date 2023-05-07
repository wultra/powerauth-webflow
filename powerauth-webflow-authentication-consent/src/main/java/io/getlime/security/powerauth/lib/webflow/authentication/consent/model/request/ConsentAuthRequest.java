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
package io.getlime.security.powerauth.lib.webflow.authentication.consent.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import java.util.Collections;
import java.util.List;

/**
 * Request for OAuth 2.1 consent.
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
