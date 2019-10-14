package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import java.util.Collections;
import java.util.List;

/**
 * Request for online mobile token authentication.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class MobileTokenAuthenticationRequest extends AuthStepRequest {

    @Override
    public List<AuthInstrument> getAuthInstruments() {
        return Collections.singletonList(AuthInstrument.POWERAUTH_TOKEN);
    }
}
