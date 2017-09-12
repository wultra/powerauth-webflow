package io.getlime.security.powerauth.lib.webauth.authentication.sms.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClientErrorException;
import io.getlime.security.powerauth.lib.credentials.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.sms.model.request.SMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.sms.model.response.SMSAuthorizationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This controller provides endpoints for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/sms")
public class SMSAuthorizationController extends AuthMethodController<SMSAuthorizationRequest, SMSAuthorizationResponse, AuthStepException> {

    @Autowired
    private CredentialStoreClient credentialStoreClient;

    // Storage for mapping of operationId->messageId.
    // TODO - switch operationId->messageId mapping to database to avoid memory leaks
    private Map<String, String> operationIdToMessageIdMap = new HashMap<>();

    /**
     * Verifies the authorization code entered by user against code generated during initialization.
     *
     * @param request Request with authentication object information.
     * @return User ID.
     * @throws AuthStepException Exception is thrown when authorization fails.
     */
    @Override
    protected String authenticate(SMSAuthorizationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final String messageId = operationIdToMessageIdMap.get(operation.getOperationId());
        if (messageId == null) {
            // verify called before create or other error occurred, request is rejected
            throw new AuthStepException("error.invalidRequest", new NullPointerException());
        }
        try {
            credentialStoreClient.verifyAuthorizationSMS(messageId, request.getAuthCode());
            operationIdToMessageIdMap.remove(operation.getOperationId());
            return operation.getUserId();
        } catch (CredentialStoreClientErrorException e) {
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.SMS_KEY;
    }

    /**
     * Initializes the SMS authorization process by creating authorization SMS using Bank Adapter.
     *
     * @return Authorization response.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public @ResponseBody
    SMSAuthorizationResponse initSMSAuthorization() {
        final GetOperationDetailResponse operation = getOperation();
        final String userId = operation.getUserId();
        SMSAuthorizationResponse initResponse = new SMSAuthorizationResponse();
        try {
            ObjectResponse<CreateSMSAuthorizationResponse> baResponse = credentialStoreClient.createAuthorizationSMS(userId, operation.getOperationName(), operation.getOperationData(), LocaleContextHolder.getLocale().getLanguage());
            String messageId = baResponse.getResponseObject().getMessageId();
            operationIdToMessageIdMap.put(operation.getOperationId(), messageId);
            initResponse.setResult(AuthStepResult.CONFIRMED);
            return initResponse;
        } catch (CredentialStoreClientErrorException e) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage(e.getMessage());
            return initResponse;
        }
    }

    /**
     * Performs the authorization and resolves the next step.
     *
     * @param request Authorization request which includes the authorization code.
     * @return Authorization response.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody
    SMSAuthorizationResponse authenticateHandler(@RequestBody SMSAuthorizationRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public SMSAuthorizationResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    return response;
                }

                @Override
                public SMSAuthorizationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    return response;
                }

                @Override
                public SMSAuthorizationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }

    }

    /**
     * Cancels the SMS authorization.
     *
     * @return Authorization response.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody
    SMSAuthorizationResponse cancelAuthentication() {
        try {
            operationIdToMessageIdMap.remove(getOperation().getOperationId());
            cancelAuthorization(getOperation().getOperationId(), null, OperationCancelReason.UNKNOWN, null);
            final SMSAuthorizationResponse cancelResponse = new SMSAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.CANCELED);
            cancelResponse.setMessage("operation.canceled");
            return cancelResponse;
        } catch (NextStepServiceException e) {
            final SMSAuthorizationResponse cancelResponse = new SMSAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.AUTH_FAILED);
            cancelResponse.setMessage(e.getMessage());
            return cancelResponse;
        }
    }

}
