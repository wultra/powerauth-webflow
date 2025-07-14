/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.controller;

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.Response;
import com.wultra.push.client.PushServerClient;
import com.wultra.push.client.PushServerClientException;
import com.wultra.push.model.enumeration.MobilePlatform;
import com.wultra.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.mtoken.model.request.PushRegisterRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidRequestObjectException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.MobileAppApiException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.PushRegistrationFailedException;
import com.wultra.security.powerauth.rest.api.spring.annotation.PowerAuth;
import com.wultra.security.powerauth.rest.api.spring.annotation.PowerAuthToken;
import com.wultra.security.powerauth.rest.api.spring.authentication.PowerAuthApiAuthentication;
import com.wultra.security.powerauth.rest.api.spring.exception.PowerAuthAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that is responsible for handling mobile device registrations
 * for the push notifications.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping("/api/push")
public class PushRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(PushRegistrationController.class);

    private final PushServerClient pushServerClient;

    /**
     * Controller constructor.
     * @param pushServerClient PowerAuth Push server client.
     */
    @Autowired
    public PushRegistrationController(PushServerClient pushServerClient) {
        this.pushServerClient = pushServerClient;
    }

    /**
     * Register device for push notifications using 1FA signature.
     * @param request Push registration request.
     * @param apiAuthentication API authentication.
     * @return Push registration response.
     * @throws PowerAuthAuthenticationException Thrown when PowerAuth authentication fails.
     * @throws MobileAppApiException Thrown when registration fails.
     */
    @PostMapping("/device/register/signature")
    @PowerAuth(resourceId = "/device/register/signature", signatureType = {PowerAuthSignatureTypes.POSSESSION})
    public @ResponseBody Response registerDevice(@RequestBody ObjectRequest<PushRegisterRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, MobileAppApiException {
        return registerDeviceImpl(request, apiAuthentication);
    }

    /**
     * Register device for push notifications using simple token-based authentication.
     * @param request Push registration request.
     * @param apiAuthentication API authentication.
     * @return Push registration response.
     * @throws PowerAuthAuthenticationException Thrown when PowerAuth authentication fails.
     * @throws MobileAppApiException Thrown when registration fails.
     */
    @PostMapping("/device/register/token")
    @PowerAuthToken(signatureType = {
            PowerAuthSignatureTypes.POSSESSION,
            PowerAuthSignatureTypes.POSSESSION_BIOMETRY,
            PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE,
            PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE_BIOMETRY
    })
    public @ResponseBody Response registerDeviceToken(@RequestBody ObjectRequest<PushRegisterRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, MobileAppApiException {
        return registerDeviceImpl(request, apiAuthentication);
    }

    /**
     * Register device for push notifications using simple token-based authentication.
     * @param request Push registration request.
     * @param apiAuthentication API authentication.
     * @return Push registration response.
     * @throws PowerAuthAuthenticationException Thrown when PowerAuth authentication fails.
     * @throws MobileAppApiException Thrown when registration fails.
     */
    @PostMapping("/device/register")
    @PowerAuthToken(signatureType = {
            PowerAuthSignatureTypes.POSSESSION,
            PowerAuthSignatureTypes.POSSESSION_BIOMETRY,
            PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE,
            PowerAuthSignatureTypes.POSSESSION_KNOWLEDGE_BIOMETRY
    })
    public @ResponseBody Response registerDeviceDefault(@RequestBody ObjectRequest<PushRegisterRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, MobileAppApiException {
        return registerDeviceImpl(request, apiAuthentication);
    }

    /**
     * Implementation of the push device registration business logic
     * @param request Push registration request.
     * @param apiAuthentication API authentication.
     * @return Push registration response.
     * @throws PowerAuthAuthenticationException Thrown when PowerAuth authentication fails.
     * @throws MobileAppApiException Thrown when registration fails.
     */
    private Response registerDeviceImpl(@RequestBody ObjectRequest<PushRegisterRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, MobileAppApiException {
        // Check if the authentication object is present
        if (apiAuthentication == null) {
            logger.error("Unable to verify device registration");
            throw new PowerAuthAuthenticationException("Unable to verify device registration");
        }

        logger.info("Push registration started, user ID: {}", apiAuthentication.getUserId());

        // Check the request body presence
        final PushRegisterRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            logger.error("Invalid request object in push registration, user ID: {}", apiAuthentication.getUserId());
            throw new InvalidRequestObjectException();
        }

        // Get the values from the request
        String token = requestObject.getToken();

        // Check if the context is authenticated - if it is, add activation ID.
        // This assures that the activation is assigned with a correct device.
        String activationId = apiAuthentication.getActivationContext().getActivationId();
        String applicationId = apiAuthentication.getApplicationId();

        // Verify that applicationId and activationId are set
        if (applicationId == null || activationId == null) {
            logger.error("Invalid activation in push registration, user ID: {}", apiAuthentication.getUserId());
            throw new PushRegistrationFailedException();
        }

        final MobilePlatform platform = convert(requestObject.getPlatform());
        try {
            boolean result = pushServerClient.createDevice(applicationId, token, platform, activationId);
            if (result) {
                logger.info("Push registration succeeded, user ID: {}", apiAuthentication.getUserId());
                return new Response();
            } else {
                logger.warn("Push registration failed, user ID: {}", apiAuthentication.getUserId());
                throw new PushRegistrationFailedException();
            }
        } catch (PushServerClientException ex) {
            logger.error("Push registration failed", ex);
            throw new PushRegistrationFailedException();
        }
    }

    private static MobilePlatform convert(final PushRegisterRequest.Platform source) {
        return switch (source) {
            case IOS -> MobilePlatform.IOS;
            case ANDROID -> MobilePlatform.ANDROID;
        };
    }

}
