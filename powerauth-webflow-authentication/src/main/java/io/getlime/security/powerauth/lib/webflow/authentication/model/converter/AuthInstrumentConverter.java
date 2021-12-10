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
                case CREDENTIAL:
                    authInstrumentsAfs.add(AfsAuthInstrument.CREDENTIAL);
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
