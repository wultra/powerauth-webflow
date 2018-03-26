/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.client.MobilePlatform;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidActivationException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidRequestObjectException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.MobileAppApiException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.PushRegistrationFailedException;
import io.getlime.security.powerauth.lib.mtoken.model.request.PushRegisterRequest;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuth;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that is responsible for handling mobile device registrations
 * for the push notifications.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping("/api/push")
public class PushRegistrationController {

    private final PushServerClient pushServerClient;

    /**
     * Controller constructor.
     * @param pushServerClient PowerAuth 2.0 Push server client.
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
    @RequestMapping(value = "/device/register/signature", method = RequestMethod.POST)
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
    @RequestMapping(value = "/device/register/token", method = RequestMethod.POST)
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
    @RequestMapping(value = "/device/register", method = RequestMethod.POST)
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
            throw new PowerAuthAuthenticationException("Unable to verify device registration");
        }

        // Check the request body presence
        final PushRegisterRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new InvalidRequestObjectException();
        }

        // Get the values from the request
        String platform = requestObject.getPlatform();
        String token = requestObject.getToken();

        // Check if the context is authenticated - if it is, add activation ID.
        // This assures that the activation is assigned with a correct device.
        String activationId = apiAuthentication.getActivationId();
        Long applicationId = apiAuthentication.getApplicationId();

        // Verify that applicationId and activationId are set
        if (applicationId == null || activationId == null) {
            throw new InvalidActivationException();
        }

        // Register the device and return response
        MobilePlatform p = MobilePlatform.Android;
        if ("ios".equalsIgnoreCase(platform)) {
            p = MobilePlatform.iOS;
        }
        try {
            boolean result = pushServerClient.createDevice(applicationId, token, p, activationId);
            if (result) {
                return new Response();
            } else {
                throw new PushRegistrationFailedException();
            }
        } catch (PushServerClientException ex) {
            throw new PushRegistrationFailedException();
        }
    }

}
