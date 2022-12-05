/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.CertificateAuthenticationMode;
import io.getlime.security.powerauth.lib.dataadapter.model.response.InitAuthMethodResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.request.ApprovalScaAuthRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.request.ApprovalScaInitRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.response.ApprovalScaAuthResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.response.ApprovalScaInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Controller for initialization of SCA approval.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "/api/auth/approval-sca")
public class ApprovalScaController extends AuthMethodController<ApprovalScaAuthRequest, ApprovalScaAuthResponse, AuthStepException> {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalScaController.class);

    private final String FIELD_BANK_ACCOUNT_CHOICE_DISABLED = "operation.bankAccountChoice.disabled";

    private final DataAdapterClient dataAdapterClient;
    private final NextStepClient nextStepClient;
    private final AuthMethodQueryService authMethodQueryService;
    private final AuthenticationManagementService authenticationManagementService;
    private final HttpSession httpSession;

    /**
     * Controller constructor.
     * @param dataAdapterClient Data Adapter client.
     * @param nextStepClient Next Step client.
     * @param authMethodQueryService Service for querying authentication methods.
     * @param authenticationManagementService Authentication management service.
     * @param httpSession HTTP session.
     */
    @Autowired
    public ApprovalScaController(DataAdapterClient dataAdapterClient, NextStepClient nextStepClient, AuthMethodQueryService authMethodQueryService, AuthenticationManagementService authenticationManagementService, HttpSession httpSession) {
        this.dataAdapterClient = dataAdapterClient;
        this.nextStepClient = nextStepClient;
        this.authMethodQueryService = authMethodQueryService;
        this.authenticationManagementService = authenticationManagementService;
        this.httpSession = httpSession;
    }

    /**
     * Authenticate SCA approval.
     * @param request Authentication request.
     * @return SCA approval authentication response.
     * @throws AuthStepException In case SCA approval authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ApprovalScaAuthResponse authenticateScaApproval(@RequestBody ApprovalScaAuthRequest request) throws AuthStepException {
        GetOperationDetailResponse operation = getOperation();
        logger.info("Step authentication started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
        String userId = operation.getUserId();
        if (userId == null) {
            // At this point user ID must be known, method cannot continue
            throw new InvalidRequestException("User ID is missing");
        }

        ApprovalScaAuthResponse response = new ApprovalScaAuthResponse();

        boolean mobileTokenEnabled = false;
        try {
            // Disable bank account choice
            operation.getFormData().getUserInput().put(FIELD_BANK_ACCOUNT_CHOICE_DISABLED, "true");
            nextStepClient.updateOperationFormData(operation.getOperationId(), operation.getFormData());

            // Upgrade operation to SCA
            authenticationManagementService.upgradeToStrongCustomerAuthentication();

            // Find out whether mobile token is enabled
            if (authMethodQueryService.isMobileTokenAvailable(userId, operation.getOperationId())) {
                nextStepClient.updateMobileToken(operation.getOperationId(), true);
                mobileTokenEnabled = true;
            }
        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
        }
        response.setMobileTokenEnabled(mobileTokenEnabled);
        if (mobileTokenEnabled) {
            response.setResult(AuthStepResult.CONFIRMED);
            logger.debug("Step authentication succeeded with mobile token, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } else {
            response.setResult(AuthStepResult.CONFIRMED);
            logger.debug("Step authentication succeeded with SMS authorization, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        }
    }

    /**
     * Initialize SCA approval.
     * @param request SCA approval initialization request.
     * @return SCA approval initialization response.
     * @throws AuthStepException In case SCA approval initialization fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ApprovalScaInitResponse initScaApproval(@RequestBody ApprovalScaInitRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        try {
            logger.info("Step init started, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            String userId = operation.getUserId();
            if (userId == null) {
                // At this point user ID must be known, method cannot continue
                throw new InvalidRequestException("User ID is missing");
            }

            // Set chosen authentication method to APPROVAL_SCA
            nextStepClient.updateChosenAuthMethod(operation.getOperationId(), AuthMethod.APPROVAL_SCA);

            FormData formData = new FormDataConverter().fromOperationFormData(operation.getFormData());
            ApplicationContext applicationContext = operation.getApplicationContext();
            OperationContext operationContext = new OperationContext(operation.getOperationId(), operation.getOperationName(), operation.getOperationData(), operation.getExternalTransactionId(), formData, applicationContext);
            ObjectResponse<InitAuthMethodResponse> objectResponse = dataAdapterClient.initAuthMethod(operation.getUserId(), operation.getOrganizationId(), AuthMethod.APPROVAL_SCA, operationContext);
            InitAuthMethodResponse initResponse = objectResponse.getResponseObject();

            final boolean approvalByCertificateEnabled = initResponse.getCertificateAuthenticationMode() == CertificateAuthenticationMode.ENABLED;
            setApprovalByCertificateEnabled(approvalByCertificateEnabled);
            setOperationDataExternal(initResponse.getOperationDataExternal());

            logger.debug("Step init succeeded, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName());
            return new ApprovalScaInitResponse();

        } catch (NextStepClientException ex) {
            logger.error("Error occurred in Next Step server", ex);
            throw new CommunicationFailedException("Communication with Next Step service failed");
        } catch (DataAdapterClientErrorException ex) {
            logger.error("Error occurred in Data Adapter", ex);
            throw new CommunicationFailedException("Communication with Data Adapter service failed");
        }
    }

    /**
     * Get current authentication method name.
     * @return Current authentication method name.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.APPROVAL_SCA;
    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public AuthStepResponse cancelAuthentication() throws AuthStepException {
        try {
            final GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), operation.getUserId(), OperationCancelReason.UNKNOWN, null, true);
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            logger.info("Step result: CANCELED, operation ID: {}, authentication method: {}", operation.getOperationId(), getAuthMethodName().toString());
            return response;
        } catch (CommunicationFailedException ex) {
            final AuthStepResponse response = new AuthStepResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("error.communication");
            logger.info("Step result: AUTH_FAILED, authentication method: {}", getAuthMethodName().toString());
            return response;
        }
    }

    private void setApprovalByCertificateEnabled(boolean approvalByCertificateEnabled) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.APPROVAL_BY_CERTIFICATE_ENABLED, approvalByCertificateEnabled);
        }
    }

    private void setOperationDataExternal(String operationDataExternal) {
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute(HttpSessionAttributeNames.OPERATION_DATA_EXTERNAL, operationDataExternal);
        }
    }
}
