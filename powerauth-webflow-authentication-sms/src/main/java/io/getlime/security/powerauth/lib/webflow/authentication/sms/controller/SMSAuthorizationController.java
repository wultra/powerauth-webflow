package io.getlime.security.powerauth.lib.webflow.authentication.sms.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
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
import io.getlime.security.powerauth.lib.webflow.authentication.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
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
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authentication started, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
        final Object messageId = httpSession.getAttribute(MESSAGE_ID);
        if (messageId == null) {
            // verify called before create or other error occurred, request is rejected
            throw new InvalidRequestException("Message ID is missing.");
        }
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData);
            dataAdapterClient.verifyAuthorizationSMS(messageId.toString(), request.getAuthCode(), operationContext);
            httpSession.removeAttribute(MESSAGE_ID);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step authentication succeeded, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
            return operation.getUserId();
        } catch (DataAdapterClientErrorException e) {
            // log failed authorization into operation history so that maximum number of Next Step update calls can be checked
            Integer remainingAttemptsNS;
            try {
                UpdateOperationResponse response = failAuthorization(operation.getOperationId(), operation.getUserId(), null);
                if (response.getResult() == AuthResult.FAILED) {
                    // FAILED result instead of CONTINUE means the authentication method is failed
                    throw new MaxAttemptsExceededException("Maximum number of authentication attempts exceeded.");
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
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step started, operation ID: {0}, authentication method: {1}", new String[] {operation.getOperationId(), getAuthMethodName().toString()});
        SMSAuthorizationResponse initResponse = new SMSAuthorizationResponse();

        final String userId = operation.getUserId();
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), formData);
            ObjectResponse<CreateSMSAuthorizationResponse> baResponse = dataAdapterClient.createAuthorizationSMS(userId, operationContext,
                    LocaleContextHolder.getLocale().getLanguage());
            String messageId = baResponse.getResponseObject().getMessageId();
            httpSession.setAttribute(MESSAGE_ID, messageId);
            initResponse.setResult(AuthStepResult.CONFIRMED);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: CONFIRMED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), getAuthMethodName().toString()});
            return initResponse;
        } catch (DataAdapterClientErrorException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error when sending SMS message.", e);
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Init step result: AUTH_FAILED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), getAuthMethodName().toString()});
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
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CONFIRMED, authentication method: {0}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public SMSAuthorizationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public SMSAuthorizationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CONFIRMED, operation ID: {0}, authentication method: {1}", new String[]{operationId, getAuthMethodName().toString()});
                    return response;
                }
            });
        } catch (AuthStepException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error occurred while verifying authorization code from SMS message: {0}", e.getMessage());
            final SMSAuthorizationResponse response = new SMSAuthorizationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
            if (e.getMessageId() != null) {
                // prefer localized message over regular message string
                response.setMessage(e.getMessageId());
            } else {
                response.setMessage(e.getMessage());
            }
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
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final SMSAuthorizationResponse cancelResponse = new SMSAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.CANCELED);
            cancelResponse.setMessage("operation.canceled");
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: CANCELED, operation ID: {0}, authentication method: {1}", new String[]{operation.getOperationId(), getAuthMethodName().toString()});
            return cancelResponse;
        } catch (NextStepServiceException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error when canceling SMS message validation.", e);
            final SMSAuthorizationResponse cancelResponse = new SMSAuthorizationResponse();
            cancelResponse.setResult(AuthStepResult.AUTH_FAILED);
            cancelResponse.setMessage(e.getMessage());
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Step result: AUTH_FAILED, authentication method: {0}", getAuthMethodName().toString());
            return cancelResponse;
        }
    }

}
