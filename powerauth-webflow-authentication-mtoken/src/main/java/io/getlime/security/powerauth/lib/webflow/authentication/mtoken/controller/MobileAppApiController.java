package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.client.MobilePlatform;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.configuration.PushServiceConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception.*;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenCancelOperationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenPushRegisterRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenSignRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.WebSocketMessageService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller presents endpoints that are consumed by the native mobile app,
 * not the web application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/token/app")
public class MobileAppApiController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    private final WebSocketMessageService webSocketMessageService;
    private final PushServiceConfiguration configuration;
    private final PushServerClient pushServerClient;
    private final AuthMethodQueryService authMethodQueryService;


    @Autowired
    public MobileAppApiController(WebSocketMessageService webSocketMessageService, PushServiceConfiguration configuration, PushServerClient pushServerClient, AuthMethodQueryService authMethodQueryService) {
        this.webSocketMessageService = webSocketMessageService;
        this.configuration = configuration;
        this.pushServerClient = pushServerClient;
        this.authMethodQueryService = authMethodQueryService;
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    /**
     * Register device for push notifications.
     * @param request Push registration request.
     * @param apiAuthentication API authentication.
     * @return Push registration response.
     * @throws PowerAuthAuthenticationException Thrown when push registration fails.
     * @throws PushRegistrationFailedException Thrown when push registration fails due to client exception.
     */
    @RequestMapping(value = "/push/register", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/push/register", signatureType = {PowerAuthSignatureTypes.POSSESSION})
    public @ResponseBody Response registerDevice(@RequestBody ObjectRequest<MobileTokenPushRegisterRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException {

        if (request.getRequestObject() == null) {
            throw new InvalidRequestObjectException();
        }

        // Get the values from the request
        String platform = request.getRequestObject().getPlatform();
        String token = request.getRequestObject().getToken();

        // Check if the context is authenticated - if it is, add activation ID.
        // This assures that the activation is assigned with a correct device.
        String activationId = null;
        String userId = null;
        if (apiAuthentication != null) {
            activationId = apiAuthentication.getActivationId();
            userId = apiAuthentication.getUserId();
        }

        // Verify that the activation ID from context matches configured activation ID for given user.
        if (!verifyActivationId(activationId, userId)) {
            throw new InvalidActivationException();
        }

        // Register the device and return response
        MobilePlatform p = MobilePlatform.Android;
        if ("ios".equalsIgnoreCase(platform)) {
            p = MobilePlatform.iOS;
        }
        try {
            boolean result = pushServerClient.createDevice(configuration.getPushServerApplication(), token, p, activationId);
            if (result) {
                return new Response();
            } else {
                throw new PushRegistrationFailedException();
            }
        } catch (PushServerClientException ex) {
            throw new PushRegistrationFailedException();
        }
    }

    /**
     * List pending operations for Mobile Token authorization.
     * @param apiAuthentication API authentication.
     * @return Response with list of pending operations.
     * @throws PowerAuthAuthenticationException Thrown when loading of pending operations fails.
     */
    @RequestMapping(value = "/operation/list", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/list", signatureType = {PowerAuthSignatureTypes.POSSESSION})
    public @ResponseBody ObjectResponse<List<GetOperationDetailResponse>> getOperationList(PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String activationId = apiAuthentication.getActivationId();
            String userId = apiAuthentication.getUserId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException();
            }

            final List<GetOperationDetailResponse> operationList = getOperationListForUser(userId);
            return new ObjectResponse<>(operationList);
        } else {
            throw new PendingOperationListFailedException();
        }

    }

    /**
     * Authorize an operation using Mobile Token.
     * @param request Mobile Token authorization request.
     * @param apiAuthentication API authentication.
     * @return Authorization response.
     * @throws NextStepServiceException Thrown when next step client fails.
     * @throws PowerAuthAuthenticationException Thrown when authentication fails.
     * @throws InvalidRequestObjectException Thrown when requestObject is invalid.
     */
    @RequestMapping(value = "/operation/authorize", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/authorize")
    public @ResponseBody Response verifySignature(@RequestBody ObjectRequest<MobileTokenSignRequest> request, PowerAuthApiAuthentication apiAuthentication) throws NextStepServiceException, PowerAuthAuthenticationException {

        if (request.getRequestObject() == null) {
            throw new InvalidRequestObjectException();
        }

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String activationId = apiAuthentication.getActivationId();
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException();
            }

            final GetOperationDetailResponse operation = getOperation(operationId);
            if (operation.getOperationData().equals(request.getRequestObject().getData()) && operation.getUserId().equals(apiAuthentication.getUserId())) {
                final UpdateOperationResponse updateOperationResponse = authorize(operationId, userId);
                webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
                return new Response();
            } else {
                throw new SignatureVerificationFailedException();
            }
        } else {
            throw new SignatureVerificationFailedException();
        }

    }

    /**
     * Cancel an operation.
     * @param request Cancel request.
     * @param apiAuthentication API authentication.
     * @return Cancel response.
     * @throws PowerAuthAuthenticationException Thrown when operation could not be canceled.
     * @throws NextStepServiceException Thrown when next step client fails.
     */
    @RequestMapping(value = "/operation/cancel", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/cancel", signatureType = {PowerAuthSignatureTypes.POSSESSION})
    public @ResponseBody Object cancelOperation(@RequestBody ObjectRequest<MobileTokenCancelOperationRequest> request, PowerAuthApiAuthentication apiAuthentication) throws PowerAuthAuthenticationException, NextStepServiceException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String activationId = apiAuthentication.getActivationId();
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            // Verify that the activation ID from context matches configured activation ID for given user.
            if (!verifyActivationId(activationId, userId)) {
                throw new InvalidActivationException();
            }

            final UpdateOperationResponse updateOperationResponse = cancelAuthorization(operationId, userId, OperationCancelReason.valueOf(request.getRequestObject().getReason()), null);
            webSocketMessageService.notifyAuthorizationComplete(operationId, updateOperationResponse.getResult());
            return new Response();

        } else {
            throw new CancelOperationFailedException();
        }

    }

    /**
     * Verifies that activation ID matches configured activation ID for given user.
     * @param activationId Checked activation ID.
     * @param userId User ID.
     * @return Whether activation ID matches configured activation ID.
     */
    private boolean verifyActivationId(String activationId, String userId) {
        try {
            String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(userId);
            if (configuredActivationId != null && configuredActivationId.equals(activationId)) {
                return true;
            }
        } catch (NextStepServiceException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not verify activationId", ex);
        }
        return false;
    }

}
