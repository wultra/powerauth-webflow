package io.getlime.security.powerauth.lib.webauth.authentication.mtoken.controller;

import io.getlime.push.client.MobilePlatform;
import io.getlime.push.client.PushServerClient;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.configuration.PushServiceConfiguration;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenCancelOperationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenPushRegisterRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenSignRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenCancelOperationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenPushRegisterResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenSignResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.service.WebSocketMessageService;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;
import io.getlime.security.powerauth.rest.api.model.base.PowerAuthApiRequest;
import io.getlime.security.powerauth.rest.api.model.base.PowerAuthApiResponse;
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
    public @ResponseBody PowerAuthApiResponse registerDevice(@RequestBody MobileTokenPushRegisterRequest request,
                                                                        PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException {

        // Get the values from the request
        String platform = request.getPlatform();
        String token = request.getToken();

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
        boolean result = pushServerClient.registerDevice(configuration.getPushServerApplication(), token, p, activationId);
        if (result) {
            return new PowerAuthApiResponse(PowerAuthApiResponse.Status.OK, PowerAuthApiResponse.Encryption.NONE, null);
        } else {
            throw new PowerAuthAuthenticationException("Authentication failed: Unable to register for push messages");
        }
    }

    @RequestMapping(value = "/operation/list", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/list", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody PowerAuthApiResponse<List<GetOperationDetailResponse>> getOperationList(PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException {
        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            final List<GetOperationDetailResponse> operationList = getOperationListForUser(userId);
            return new PowerAuthApiResponse(PowerAuthApiResponse.Status.OK, PowerAuthApiResponse.Encryption.NONE, operationList);
        } else {
            throw new PowerAuthAuthenticationException("Authentication failed: Unable to download pending operations");
        }
    }

    @RequestMapping(value = "/operation/authorize", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/authorize")
    public @ResponseBody PowerAuthApiResponse<MobileTokenSignResponse> verifySignature(
            @RequestBody PowerAuthApiRequest<MobileTokenSignRequest> request,
            PowerAuthApiAuthentication apiAuthentication) throws NextStepServiceException, PowerAuthAuthenticationException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            final GetOperationDetailResponse operation = getOperation(operationId);
            if (operation.getOperationData().equals(request.getRequestObject().getData()) && operation.getUserId().equals(apiAuthentication.getUserId())) {
                final UpdateOperationResponse updateOperationResponse = authorize(operationId, userId);
                webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
                return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.OK, null);
            } else {
                throw new PowerAuthAuthenticationException("Authentication failed: Unable to sign operation");
            }
        } else {
            throw new PowerAuthAuthenticationException("Authentication failed: Unable to sign operation");
        }
    }

    @RequestMapping(value = "/operation/reject", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/reject", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody PowerAuthApiResponse<MobileTokenCancelOperationResponse> rejectOperation(
            @RequestBody PowerAuthApiRequest<MobileTokenCancelOperationRequest> request,
            PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, NextStepServiceException {
        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            final UpdateOperationResponse updateOperationResponse = failAuthorization(operationId, userId, null);
            webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
            return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.OK, null);

        } else {
            throw new PowerAuthAuthenticationException("Authentication failed: Unable to cancel pending operation");
        }
    }

}
