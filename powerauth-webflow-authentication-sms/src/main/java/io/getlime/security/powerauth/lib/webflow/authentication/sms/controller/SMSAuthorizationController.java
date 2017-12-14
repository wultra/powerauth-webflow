package io.getlime.security.powerauth.lib.webflow.authentication.sms.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.request.SMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response.SMSAuthorizationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller which provides endpoints for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/sms")
public class SMSAuthorizationController extends AuthMethodController<SMSAuthorizationRequest, SMSAuthorizationResponse, AuthStepException> {

    private final DataAdapterClient dataAdapterClient;
    private final HttpSession httpSession;

    /**
     * Controller constructor.
     * @param dataAdapterClient Data adapter client.
     * @param httpSession HTTP session.
     */
    @Autowired
    public SMSAuthorizationController(DataAdapterClient dataAdapterClient, HttpSession httpSession) {
        this.dataAdapterClient = dataAdapterClient;
        this.httpSession = httpSession;
    }

    private static final String MESSAGE_ID = "messageId";

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
        final Object messageId = httpSession.getAttribute(MESSAGE_ID);
        if (messageId == null) {
            // verify called before create or other error occurred, request is rejected
            throw new AuthStepException("error.invalidRequest", new NullPointerException());
        }
        try {
            dataAdapterClient.verifyAuthorizationSMS(messageId.toString(), request.getAuthCode());
            httpSession.removeAttribute(MESSAGE_ID);
            return operation.getUserId();
        } catch (DataAdapterClientErrorException e) {
            // log failed authorization into operation history so that maximum number of Next Step update calls can be checked
            Integer remainingAttemptsNS;
            try {
                UpdateOperationResponse response = failAuthorization(operation.getOperationId(), operation.getUserId(), null);
                if (response.getResult() == AuthResult.FAILED) {
                    // FAILED result instead of CONTINUE means the authentication method is failed
                    throw new AuthStepException("authentication.maxAttemptsExceeded", e);
                }
                GetOperationDetailResponse updatedOperation = getOperation();
                remainingAttemptsNS = updatedOperation.getRemainingAttempts();
            } catch (NextStepServiceException e2) {
                throw new AuthStepException(e2.getError().getMessage(), e2);
            }
            AuthStepException authEx = new AuthStepException(e.getError().getMessage(), e);
            Integer remainingAttemptsDA = e.getError().getRemainingAttempts();
            Integer remainingAttempts = resolveRemainingAttempts(remainingAttemptsDA, remainingAttemptsNS);
            authEx.setRemainingAttempts(remainingAttempts);
            throw authEx;
        }
    }

    /**
     * Get current authentication method.
     * @return Current authentication method.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.SMS_KEY;
    }

    /**
     * Initializes the SMS authorization process by creating authorization SMS using Data Adapter.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public @ResponseBody SMSAuthorizationResponse initSMSAuthorization() throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        SMSAuthorizationResponse initResponse = new SMSAuthorizationResponse();

        final String userId = operation.getUserId();
        try {
            ObjectResponse<CreateSMSAuthorizationResponse> baResponse = dataAdapterClient.createAuthorizationSMS(
                    operation.getOperationId(), userId, operation.getOperationName(), operation.getFormData(),
                    LocaleContextHolder.getLocale().getLanguage());
            String messageId = baResponse.getResponseObject().getMessageId();
            httpSession.setAttribute(MESSAGE_ID, messageId);
            initResponse.setResult(AuthStepResult.CONFIRMED);
            return initResponse;
        } catch (DataAdapterClientErrorException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error when sending SMS message.", e);
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
    public @ResponseBody SMSAuthorizationResponse authenticateHandler(@RequestBody SMSAuthorizationRequest request) {
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error when validating an SMS message.", e);
            final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            response.setRemainingAttempts(e.getRemainingAttempts());
            return response;
        }

    }

    /**
     * Cancels the SMS authorization.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody SMSAuthorizationResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            httpSession.removeAttribute(MESSAGE_ID);
            cancelAuthorization(operation.getOperationId(), null, OperationCancelReason.UNKNOWN, null);
            final SMSAuthorizationResponse cancelResponse = new SMSAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.CANCELED);
            cancelResponse.setMessage("operation.canceled");
            return cancelResponse;
        } catch (NextStepServiceException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error when canceling SMS message validation.", e);
            final SMSAuthorizationResponse cancelResponse = new SMSAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.AUTH_FAILED);
            cancelResponse.setMessage(e.getMessage());
            return cancelResponse;
        }
    }

}
