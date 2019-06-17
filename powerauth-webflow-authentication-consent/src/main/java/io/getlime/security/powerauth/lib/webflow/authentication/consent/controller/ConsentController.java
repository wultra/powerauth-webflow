/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.consent.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateConsentFormResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.SaveConsentFormResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.ValidateConsentFormResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.consent.exception.ConsentValidationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.consent.model.request.ConsentAuthRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.consent.model.response.ConsentInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.consent.model.response.ConsentAuthResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.MaxAttemptsExceededException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller which provides endpoints for OAuth 2.0 consent screen.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/consent")
public class ConsentController extends AuthMethodController<ConsentAuthRequest, ConsentAuthResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(ConsentController.class);

    private final DataAdapterClient dataAdapterClient;

    /**
     * Controller constructor.
     * @param dataAdapterClient Data adapter client.
     */
    @Autowired
    public ConsentController(DataAdapterClient dataAdapterClient) {
        this.dataAdapterClient = dataAdapterClient;
    }

    /**
     * Validate the consent form and persist the selected options.
     *
     * @param request Consent validation request.
     * @return User ID.
     * @throws AuthStepException Exception is thrown when consent validation or persistence fails.
     */
    @Override
    protected String authenticate(ConsentAuthRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        checkOperationExpiration(operation);
        final String userId = operation.getUserId();
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            String operationId = operation.getOperationId();
            String operationName = operation.getOperationName();
            String operationData = operation.getOperationData();
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operationId, operationName, operationData, formData, applicationContext);
            List<ConsentOption> options = request.getOptions();
            ObjectResponse<ValidateConsentFormResponse> daResponse = dataAdapterClient.validateConsentForm(userId, operationContext, LocaleContextHolder.getLocale().getLanguage(), options);
            ValidateConsentFormResponse validateResponse = daResponse.getResponseObject();
            if (validateResponse.getConsentValidationPassed()) {
                ObjectResponse<SaveConsentFormResponse> daResponse2 = dataAdapterClient.saveConsentForm(userId, operationContext, request.getOptions());
                SaveConsentFormResponse saveResponse = daResponse2.getResponseObject();
                if (saveResponse.isSaveSucceeded()) {
                    logger.info("Step authentication succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
                    return operation.getUserId();
                }
                // Validation succeeded, however save failed, allow user to retry the consent confirmation
                throw new AuthStepException("User consent could not be saved", "error.communication");
            }
            // Validation failed, return information about errors
            ConsentValidationFailedException ex = new ConsentValidationFailedException("Consent validation failed", "error.consentValidationFailed");
            ex.setErrorMessage(validateResponse.getValidationErrorMessage());
            ex.setOptionValidationResults(validateResponse.getOptionValidationResults());
            throw ex;
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
                logger.error("Error occurred in Next Step server", e);
                throw new AuthStepException(e2.getError().getMessage(), e2, "error.communication");
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
        return AuthMethod.CONSENT;
    }

    /**
     * Initializes the OAuth 2.0 consent form.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ConsentInitResponse initConsentForm() throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        logger.info("Init step started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        checkOperationExpiration(operation);
        ConsentInitResponse initResponse = new ConsentInitResponse();

        final String userId = operation.getUserId();
        try {
            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            ApplicationContext applicationContext = operation.getApplicationContext();
            String operationId = operation.getOperationId();
            String operationName = operation.getOperationName();
            String operationData = operation.getOperationData();
            OperationContext operationContext = new OperationContext(operationId, operationName, operationData, formData, applicationContext);
            ObjectResponse<CreateConsentFormResponse> daResponse = dataAdapterClient.createConsentForm(userId, operationContext,
                    LocaleContextHolder.getLocale().getLanguage());
            CreateConsentFormResponse createResponse = daResponse.getResponseObject();
            initResponse.setResult(AuthStepResult.CONFIRMED);
            initResponse.setConsentHtml(createResponse.getConsentHtml());
            initResponse.getOptions().addAll(createResponse.getOptions());
            logger.info("Init step result: CONFIRMED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return initResponse;
        } catch (DataAdapterClientErrorException e) {
            logger.error("Error when creating consent form.", e);
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Init step result: AUTH_FAILED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            initResponse.setMessage("error.communication");
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
    public ConsentAuthResponse authenticateHandler(@RequestBody ConsentAuthRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public ConsentAuthResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final ConsentAuthResponse response = new ConsentAuthResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    logger.info("Step result: CONFIRMED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public ConsentAuthResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final ConsentAuthResponse response = new ConsentAuthResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public ConsentAuthResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final ConsentAuthResponse response = new ConsentAuthResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
                    return response;
                }
            });
        } catch (AuthStepException e) {
            logger.warn("Error occurred while validating consent options: {}", e.getMessage());
            final ConsentAuthResponse response = new ConsentAuthResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            if (e instanceof ConsentValidationFailedException) {
                ConsentValidationFailedException validationEx = (ConsentValidationFailedException) e;
                response.setConsentValidationPassed(false);
                response.setValidationErrorMessage(validationEx.getErrorMessage());
                response.setOptionValidationResults(validationEx.getOptionValidationResults());
                return response;
            }
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
     * Cancels the OAuth 2.0 consent.
     *
     * @return Authorization response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public ConsentAuthResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null);
            final ConsentAuthResponse cancelResponse = new ConsentAuthResponse();
            cancelResponse.setResult(AuthStepResult.CANCELED);
            cancelResponse.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return cancelResponse;
        } catch (NextStepServiceException e) {
            logger.error("Error when canceling SMS message validation.", e);
            final ConsentAuthResponse cancelResponse = new ConsentAuthResponse();
            cancelResponse.setResult(AuthStepResult.AUTH_FAILED);
            cancelResponse.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return cancelResponse;
        }
    }

}
