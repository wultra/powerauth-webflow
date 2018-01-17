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

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccount;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccountChoice;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccountList;
import io.getlime.security.powerauth.lib.dataadapter.model.response.BankAccountListResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.BankAccountDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.OperationDetailRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.OperationReviewRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.UpdateOperationChosenAuthMethodRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.UpdateOperationFormDataRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response.OperationReviewDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response.OperationReviewResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.MessageTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Operation review controller which shows operation details to the user and handles operation form data updates.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/operation")
public class OperationReviewController extends AuthMethodController<OperationReviewRequest, OperationReviewResponse, AuthStepException> {

    private final String FIELD_BANK_ACCOUNT_CHOICE = "operation.bankAccountChoice";
    private final String FIELD_BANK_ACCOUNT_CHOICE_DISABLED = "operation.bankAccountChoice.disabled";

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;
    private final MessageTranslationService messageTranslationService;

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
     * @return User ID.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @Override
    protected String authenticate(OperationReviewRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        //TODO: Check pre-authenticated user here
        return operation.getUserId();
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
        final GetOperationDetailResponse operation = getOperation();
        OperationReviewDetailResponse response = new OperationReviewDetailResponse();
        response.setData(operation.getOperationData());
        response.setFormData(loadFormData(operation));
        response.setChosenAuthMethod(operation.getChosenAuthMethod());
        return response;
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
                    return response;
                }

                @Override
                public OperationReviewResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    return response;
                }

                @Override
                public OperationReviewResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
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
            cancelAuthorization(operation.getOperationId(), null, OperationCancelReason.UNKNOWN, null);
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            return response;
        } catch (NextStepServiceException e) {
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    /**
     * Update operation form data.
     * @param request Update operation form data request.
     * @return Object response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails.
     * @throws DataAdapterClientErrorException Thrown when data could not be retrieved from Data Adapter.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/formData", method = RequestMethod.PUT)
    public @ResponseBody ObjectResponse updateFormData(@RequestBody UpdateOperationFormDataRequest request) throws NextStepServiceException, DataAdapterClientErrorException, AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        // update formData in Next Step server
        nextStepClient.updateOperationFormData(operation.getOperationId(), request.getFormData());
        // Send notification to Data Adapter if the bank account has changed.
        // In case there is no bank account choice, the notification is not performed.
        Map<String, String> userInput = request.getFormData().getUserInput();
        if (userInput.containsKey(FIELD_BANK_ACCOUNT_CHOICE_DISABLED) && userInput.containsKey(FIELD_BANK_ACCOUNT_CHOICE)) {
            BankAccountChoice bankAccountChoice = new BankAccountChoice();
            bankAccountChoice.setBankAccountId(request.getFormData().getUserInput().get(FIELD_BANK_ACCOUNT_CHOICE));
            dataAdapterClient.formDataChangedNotification(bankAccountChoice, operation.getUserId(), operation.getOperationId());
        }
        return new ObjectResponse();
    }

    /**
     * Update chosen authentication method.
     * @param request Update chosen authentication method request.
     * @return Object response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails.
     * @throws AuthStepException Thrown when operation is invalid or not available.
     */
    @RequestMapping(value = "/chosenAuthMethod", method = RequestMethod.PUT)
    public @ResponseBody ObjectResponse updateChosenAuthenticationMethod(@RequestBody UpdateOperationChosenAuthMethodRequest request) throws NextStepServiceException, AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        // update chosenAuthMethod in Next Step server
        nextStepClient.updateChosenAuthMethod(operation.getOperationId(), request.getChosenAuthMethod());
        return new ObjectResponse();
    }

    /**
     * Load form data from the server.
     * @param operation Operation.
     * @return Operation form data.
     */
    private OperationFormData loadFormData(GetOperationDetailResponse operation) {
        OperationFormData formData = operation.getFormData();
        if (formData==null || operation.getUserId()==null) {
            return formData;
        }
        if (!formData.isDynamicDataLoaded()) {
            // Dynamic data has not been loaded yet. At this point the user is authenticated, so we can
            // load dynamic data based on user id. For now dynamic data contains the bank account list,
            // however it can be easily extended in the future.
            try {
                ObjectResponse<BankAccountListResponse> response = dataAdapterClient.fetchBankAccounts(operation.getUserId(), operation.getOperationName(), operation.getOperationId(), operation.getFormData());
                BankAccountList bankAccountList = response.getResponseObject().getBankAccounts();
                List<BankAccountDetail> bankAccountDetails = convertBankAccountList(bankAccountList);
                if (!bankAccountDetails.isEmpty()) {
                    formData.addBankAccountChoice(FIELD_BANK_ACCOUNT_CHOICE, bankAccountDetails, bankAccountList.isEnabled(), bankAccountList.getDefaultValue());
                }
                formData.setDynamicDataLoaded(true);
            } catch (DataAdapterClientErrorException e) {
                // Failed to load dynamic data, log the error. The UI will handle missing dynamic data error separately.
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Failed to load dynamic operation data", e);
            }
        }
        // translate new formData messages
        messageTranslationService.translateFormData(formData);
        return formData;
    }

    /**
     * Convert BankAccount into BankAccountDetail.
     * @param bankAccountList List of BankAccount entities.
     * @return List of BankAccountDetail.
     */
    private List<BankAccountDetail> convertBankAccountList(BankAccountList bankAccountList) {
        // TODO - move to a converter class
        List<BankAccountDetail> bankAccountDetails = new ArrayList<>();
        if (bankAccountList == null || bankAccountList.getBankAccounts() == null || bankAccountList.getBankAccounts().isEmpty()) {
            return bankAccountDetails;
        }
        for (BankAccount bankAccountEntity: bankAccountList.getBankAccounts()) {
            BankAccountDetail bankAccount = new BankAccountDetail();
            bankAccount.setName(bankAccountEntity.getName());
            bankAccount.setNumber(bankAccountEntity.getNumber());
            bankAccount.setAccountId(bankAccountEntity.getAccountId());
            bankAccount.setBalance(bankAccountEntity.getBalance());
            bankAccount.setCurrency(bankAccountEntity.getCurrency());
            bankAccount.setUsableForPayment(bankAccountEntity.isUsableForPayment());
            bankAccount.setUnusableForPaymentReason(bankAccountEntity.getUnusableForPaymentReason());
            bankAccountDetails.add(bankAccount);
        }
        return bankAccountDetails;
    }

}
