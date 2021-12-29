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

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccountChoice;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.response.DecorateOperationFormDataResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.GetPAOperationMappingResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.OperationDetailRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.OperationReviewRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.UpdateOperationChosenAuthMethodRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.UpdateOperationFormDataRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response.OperationReviewDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response.OperationReviewResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.AuthResultDetail;
import io.getlime.security.powerauth.lib.webflow.authentication.service.MessageTranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Operation review controller which shows operation details to the user and handles operation form data updates.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
@RequestMapping(value = "/api/auth/operation")
public class OperationReviewController extends AuthMethodController<OperationReviewRequest, OperationReviewResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(OperationReviewController.class);

    private static final String FIELD_BANK_ACCOUNT_CHOICE = "operation.bankAccountChoice";
    private static final String FIELD_BANK_ACCOUNT_CHOICE_DISABLED = "operation.bankAccountChoice.disabled";

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;
    private final MessageTranslationService messageTranslationService;

    private final FormDataConverter formDataConverter = new FormDataConverter();

    /**
     * Controller constructor.
     *
     * @param dataAdapterClient Data adapter client.
     * @param nextStepClient Next step client.
     * @param messageTranslationService Message translation service.
     */
    @Autowired
    public OperationReviewController(DataAdapterClient dataAdapterClient, NextStepClient nextStepClient, MessageTranslationService messageTranslationService) {
        this.dataAdapterClient = dataAdapterClient;
        this.nextStepClient = nextStepClient;
        this.messageTranslationService = messageTranslationService;
    }

    /**
     * Authentication step - step is automatically authenticated if operation is valid.
     * @param request Authentication request.
     * @return Authentication result with user ID and organization ID.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @Override
    protected AuthResultDetail authenticate(OperationReviewRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        //TODO: Check pre-authenticated user here
        logger.info("Step authentication succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        return new AuthResultDetail(operation.getUserId(), operation.getOrganizationId(), false, null);
    }

    /**
     * Get current authentication method.
     * @return Current authentication method.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.SHOW_OPERATION_DETAIL;
    }

    /**
     * Get operation detail.
     * @param request Operation detail request.
     * @return Operation detail response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    public @ResponseBody OperationReviewDetailResponse getOperationDetails(@RequestBody OperationDetailRequest request) throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            FormData formData = formDataConverter.fromOperationFormData(operation.getFormData());
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formData, applicationContext);
            GetPAOperationMappingResponse mappingResponse = dataAdapterClient.getPAOperationMapping(operation.getUserId(), operation.getOrganizationId(), getAuthMethodName(operation), operationContext).getResponseObject();
            operation.setOperationName(mappingResponse.getOperationName());
            operation.setOperationData(mappingResponse.getOperationData());
            operation.setFormData(formDataConverter.fromFormData(mappingResponse.getFormData()));
            OperationReviewDetailResponse response = new OperationReviewDetailResponse();
            response.setData(operation.getOperationData());
            response.setFormData(decorateFormData(operation));
            response.setChosenAuthMethod(operation.getChosenAuthMethod());
            return response;
        } catch (DataAdapterClientErrorException ex) {
            logger.error("Error occurred in Data Adapter server", ex);
            throw new CommunicationFailedException("Operation mapping is not available");
        }
    }

    /**
     * Perform step authentication and return response.
     * @param request Operation review request.
     * @return Operation review response.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody OperationReviewResponse getOperationDetails(@RequestBody OperationReviewRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public OperationReviewResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    logger.info("Step result: CONFIRMED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public OperationReviewResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
                    return response;
                }

                @Override
                public OperationReviewResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    logger.info("Step result: CONFIRMED, operation ID: {}, authentication method: {}", operationId, getAuthMethodName().toString());
                    return response;
                }
            });
        } catch (AuthStepException e) {
            logger.warn("Error occurred while reviewing operation: {}", e.getMessage());
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            if (e.getMessageId() != null) {
                // prefer localized message over regular message string
                response.setMessage(e.getMessageId());
            } else {
                response.setMessage(e.getMessage());
            }
            return response;
        }
    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody OperationReviewResponse cancelAuthentication() throws AuthStepException {
        try {
            GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null, false);
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (CommunicationFailedException ex) {
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Update operation form data (PUT method).
     * @param request Update operation form data request.
     * @return Object response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/formData", method = RequestMethod.PUT)
    public @ResponseBody Response updateFormData(@RequestBody UpdateOperationFormDataRequest request) throws AuthStepException {
        return updateFormDataImpl(request);
    }

    /**
     * Update operation form data (POST method alternative).
     * @param request Update operation form data request.
     * @return Object response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/formData/update", method = RequestMethod.POST)
    public @ResponseBody Response updateFormDataPost(@RequestBody UpdateOperationFormDataRequest request) throws AuthStepException {
        return updateFormDataImpl(request);
    }

    private Response updateFormDataImpl(UpdateOperationFormDataRequest request) throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            // update formData in Next Step server
            nextStepClient.updateOperationFormData(operation.getOperationId(), request.getFormData());
            // Send notification to Data Adapter if the bank account has changed.
            // In case there is no bank account choice, the notification is not performed.
            Map<String, String> userInput = request.getFormData().getUserInput();
            if (userInput.containsKey(FIELD_BANK_ACCOUNT_CHOICE_DISABLED) && userInput.containsKey(FIELD_BANK_ACCOUNT_CHOICE)) {
                BankAccountChoice bankAccountChoice = new BankAccountChoice();
                bankAccountChoice.setBankAccountId(request.getFormData().getUserInput().get(FIELD_BANK_ACCOUNT_CHOICE));
                FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
                ApplicationContext applicationContext = operation.getApplicationContext();
                OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formData, applicationContext);
                dataAdapterClient.formDataChangedNotification(bankAccountChoice, operation.getUserId(), operation.getOrganizationId(), operationContext);
            }
        } catch (NextStepClientException | DataAdapterClientErrorException ex) {
            logger.error(ex.getMessage(), ex);
            throw new AuthStepException("Form data could not be updated", "error.communication");
        }
        return new Response();
    }

    /**
     * Update chosen authentication method (PUT method).
     * @param request Update chosen authentication method request.
     * @return Object response.
     */
    @RequestMapping(value = "/chosenAuthMethod", method = RequestMethod.PUT)
    public @ResponseBody Response updateChosenAuthenticationMethod(@RequestBody UpdateOperationChosenAuthMethodRequest request) throws AuthStepException {
        return updateChosenAuthenticationMethodImpl(request);
    }

    /**
     * Update chosen authentication method (POST method alternative).
     * @param request Update chosen authentication method request.
     * @return Object response.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/chosenAuthMethod/update", method = RequestMethod.POST)
    public @ResponseBody Response updateChosenAuthenticationMethodPost(@RequestBody UpdateOperationChosenAuthMethodRequest request) throws AuthStepException {
        return updateChosenAuthenticationMethodImpl(request);
    }

    private Response updateChosenAuthenticationMethodImpl(UpdateOperationChosenAuthMethodRequest request) throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            // update chosenAuthMethod in Next Step server
            nextStepClient.updateChosenAuthMethod(operation.getOperationId(), request.getChosenAuthMethod());
        } catch (NextStepClientException ex) {
            logger.error(ex.getMessage(), ex);
            throw new AuthStepException("Chosen authentication method could not be saved", "error.communication");
        }
        return new Response();
    }

    /**
     * Decorate form data in Data Adapter.
     * @param operation Operation.
     * @return Decorated operation form data.
     */
    private OperationFormData decorateFormData(GetOperationDetailResponse operation) {
        OperationFormData formDataNS = operation.getFormData();
        if (formDataNS == null) {
            return null;
        }
        if (!formDataNS.isDynamicDataLoaded() && operation.getUserId() != null) {
            // Dynamic data has not been loaded yet. At this point the user is authenticated, so we can
            // load dynamic data based on user id. For now dynamic data contains the bank account list,
            // however it can be easily extended in the future.
            try {
                FormDataConverter converter = new FormDataConverter();
                FormData formDataDA = converter.fromOperationFormData(operation.getFormData());
                ApplicationContext applicationContext = operation.getApplicationContext();
                final AuthMethod authMethod = getAuthMethodName(operation);
                OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formDataDA, applicationContext);
                ObjectResponse<DecorateOperationFormDataResponse> response = dataAdapterClient.decorateOperationFormData(operation.getUserId(), operation.getOrganizationId(), authMethod, operationContext);
                DecorateOperationFormDataResponse responseObject = response.getResponseObject();
                formDataNS = converter.fromFormData(responseObject.getFormData());
                formDataNS.setDynamicDataLoaded(true);
                operation.setFormData(formDataNS);
            } catch (DataAdapterClientErrorException e) {
                // Failed to load dynamic data, log the error. The UI will handle missing dynamic data error separately.
                logger.error("Failed to load dynamic operation data", e);
            }
        }
        // translate new formData messages
        messageTranslationService.translateFormData(formDataNS);
        return formDataNS;
    }

}
