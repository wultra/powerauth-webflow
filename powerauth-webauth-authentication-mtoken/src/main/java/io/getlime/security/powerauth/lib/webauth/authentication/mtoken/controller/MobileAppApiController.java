package io.getlime.security.powerauth.lib.webauth.authentication.mtoken.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.client.MobilePlatform;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.configuration.PushServiceConfiguration;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.exception.InvalidRequestObjectException;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.exception.PushRegistrationFailedException;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenCancelOperationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenPushRegisterRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenSignRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.service.WebSocketMessageService;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * This controller presents endpoints that are consumed by the native mobile app,
 * not the web application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/token/app")
public class MobileAppApiController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    @Autowired
    private WebSocketMessageService webSocketMessageService;

    @Autowired
    private PushServiceConfiguration configuration;

    @Autowired
    private PushServerClient pushServerClient;

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    @RequestMapping(value="/push/register", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/push/register", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody Response registerDevice(@RequestBody ObjectRequest<MobileTokenPushRegisterRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, PushRegistrationFailedException {

        if (request.getRequestObject() == null) {
            throw new PushRegistrationFailedException();
        }

        // Get the values from the request
        String platform = request.getRequestObject().getPlatform();
        String token = request.getRequestObject().getToken();

        // Check if the context is authenticated - if it is, add activation ID.
        // This assures that the activation is assigned with a correct device.
        String activationId = null;
        if (apiAuthentication != null) {
            activationId = apiAuthentication.getActivationId();
        }

        // Register the device and return response
        MobilePlatform p = MobilePlatform.Android;
        if ("ios".equalsIgnoreCase(platform)) {
            p = MobilePlatform.iOS;
        }
        try {
            boolean result = pushServerClient.registerDevice(configuration.getPushServerApplication(), token, p, activationId);
            if (result) {
                return new Response();
            } else {
                throw new PowerAuthAuthenticationException("Authentication failed: Unable to register for push messages");
            }
        } catch (PushServerClientException ex) {
            throw new PushRegistrationFailedException();
        }
    }

    @RequestMapping(value = "/operation/list", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/list", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody ObjectResponse<List<GetOperationDetailResponse>> getOperationList(PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            final List<GetOperationDetailResponse> operationList = getOperationListForUser(userId);
            return new ObjectResponse(operationList);
        } else {
            throw new PowerAuthAuthenticationException("Authentication failed: Unable to download pending operations");
        }

    }

    @RequestMapping(value = "/operation/authorize", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/authorize")
    public @ResponseBody Response verifySignature(@RequestBody ObjectRequest<MobileTokenSignRequest> request, PowerAuthApiAuthentication apiAuthentication) throws NextStepServiceException, PowerAuthAuthenticationException, InvalidRequestObjectException {

        if (request.getRequestObject() == null) {
            throw new InvalidRequestObjectException();
        }

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            final GetOperationDetailResponse operation = getOperation(operationId);
            if (operation.getOperationData().equals(request.getRequestObject().getData()) && operation.getUserId().equals(apiAuthentication.getUserId())) {
                final UpdateOperationResponse updateOperationResponse = authorize(operationId, userId);
                webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
                return new Response();
            } else {
                throw new PowerAuthAuthenticationException("Authentication failed: Unable to sign operation");
            }
        } else {
            throw new PowerAuthAuthenticationException("Authentication failed: Unable to sign operation");
        }

    }

    @RequestMapping(value = "/operation/reject", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/reject", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody Object rejectOperation(@RequestBody ObjectRequest<MobileTokenCancelOperationRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, NextStepServiceException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            //TODO: Use cancel authorization method, see #51
            final UpdateOperationResponse updateOperationResponse = failAuthorization(operationId, userId, null);
            webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
            return new Response();

        } else {
            throw new PowerAuthAuthenticationException("Authentication failed: Unable to cancel pending operation");
        }

    }

}
