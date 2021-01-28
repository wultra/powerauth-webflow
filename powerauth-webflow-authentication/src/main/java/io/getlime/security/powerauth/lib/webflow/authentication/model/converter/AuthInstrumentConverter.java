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
package io.getlime.security.powerauth.lib.webflow.authentication.model.converter;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter for authentication / authorization instruments.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthInstrumentConverter {

    public List<AfsAuthInstrument> fromAuthInstruments(List<AuthInstrument> authInstruments) {
        List<AfsAuthInstrument> authInstrumentsAfs = new ArrayList<>();
        for (AuthInstrument instrument: authInstruments) {
            switch (instrument) {
                case PASSWORD:
                    authInstrumentsAfs.add(AfsAuthInstrument.PASSWORD);
                    break;
                case OTP_KEY:
                    authInstrumentsAfs.add(AfsAuthInstrument.OTP_KEY);
                    break;
                case POWERAUTH_TOKEN:
                    authInstrumentsAfs.add(AfsAuthInstrument.POWERAUTH_TOKEN);
                    break;
                case HW_TOKEN:
                    authInstrumentsAfs.add(AfsAuthInstrument.HW_TOKEN);
                    break;
            }
        }
        return authInstrumentsAfs;
    }
}
