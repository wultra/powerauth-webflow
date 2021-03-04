/*
 * Copyright 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.nextstep.client;

import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.NextStepError;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * REST client for Next Step services.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepClient {

    private static final Logger logger = LoggerFactory.getLogger(NextStepClient.class);

    private final RestClient restClient;

    /**
     * Create a new client with provided base URL.
     * @param serviceBaseUrl REST service base URL.
     * @throws NextStepClientException Thrown when REST client initialization fails.
     */
    public NextStepClient(String serviceBaseUrl) throws NextStepClientException {
        try {
            restClient = new DefaultRestClient(serviceBaseUrl);
        } catch (RestClientException ex) {
            throw new NextStepClientException(ex, new NextStepError(resolveErrorCode(ex), "Rest client initialization failed."));
        }
    }

    /**
     * Create a new client with provided base URL.
     * @param restClientConfiguration REST service client configuration.
     * @throws NextStepClientException Thrown when REST client initialization fails.
     */
    public NextStepClient(RestClientConfiguration restClientConfiguration) throws NextStepClientException {
        try {
            restClient = new DefaultRestClient(restClientConfiguration);
        } catch (RestClientException ex) {
            throw new NextStepClientException(ex, new NextStepError(resolveErrorCode(ex), "Rest client initialization failed."));
        }
    }

    // Operation related methods

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationData Operation data.
     * @param params List of generic parameters.
     * @return A Response with CreateOperationResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, @NotNull String operationData, List<KeyValueParameter> params) throws NextStepClientException {
        return createOperation(operationName, null, operationData, null, null, null, params, null);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationId Operation ID (optional - if null, unique ID is automatically generated).
     * @param operationData Operation data.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with CreateOperationResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
    */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, String operationId, @NotNull String operationData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        return createOperation(operationName, operationId, operationData, null, null, new OperationFormData(), params, applicationContext);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationData Operation data.
     * @param formData Operation form data, such as title, message and displayable attributes.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with CreateOperationResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, @NotNull String operationData, @NotNull OperationFormData formData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        return createOperation(operationName, null, operationData, null, null, formData, params, applicationContext);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationId Operation ID (optional - if null, unique ID is automatically generated).
     * @param operationData Operation data.
     * @param organizationId Organization ID.
     * @param externalTransactionId External transaction ID.
     * @param formData Operation form data, such as title, message and displayable attributes.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with CreateOperationResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, String operationId, @NotNull String operationData, String organizationId, String externalTransactionId, @NotNull OperationFormData formData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        CreateOperationRequest request = new CreateOperationRequest();
        request.setOperationName(operationName);
        request.setOperationId(operationId);
        request.setOperationData(operationData);
        request.setOrganizationId(organizationId);
        request.setExternalTransactionId(externalTransactionId);
        request.setFormData(formData);
        if (params != null) {
            request.getParams().addAll(params);
        }
        request.setApplicationContext(applicationContext);
        return postObjectImpl("/operation", new ObjectRequest<>(request), CreateOperationResponse.class);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationData Operation data.
     * @param organizationId Organization ID.
     * @param formData Operation form data, such as title, message and displayable attributes.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with CreateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, @NotNull String operationData, @NotNull OperationFormData formData, String organizationId, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        return createOperation(operationName, null, operationData, organizationId, null, formData, params, applicationContext);
    }

    /**
     * Calls the update operation endpoint via PUT method to update an existing operation.
     *
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @param authInstruments Authentication / authorization instruments.
     * @param organizationId Organization ID.
     * @param authStepResult Result of the last step.
     * @param authStepResultDescription Description of the result of the last step.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with UpdateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOperationResponse> updateOperation(@NotNull String operationId, String userId, String organizationId, @NotNull AuthMethod authMethod, List<AuthInstrument> authInstruments, @NotNull AuthStepResult authStepResult, String authStepResultDescription, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        UpdateOperationRequest request = new UpdateOperationRequest();
        request.setOperationId(operationId);
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setAuthMethod(authMethod);
        request.getAuthInstruments().addAll(authInstruments);
        request.setAuthStepResult(authStepResult);
        request.setAuthStepResultDescription(authStepResultDescription);
        if (params != null) {
            request.getParams().addAll(params);
        }
        request.setApplicationContext(applicationContext);
        return putObjectImpl("/operation", new ObjectRequest<>(request), UpdateOperationResponse.class);
    }
  
    /**
     * Calls the update operation endpoint via POST method to update an existing operation.
     *
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @param authInstruments Used authentication / authorization instruments.
     * @param organizationId Organization ID.
     * @param authStepResult Result of the last step.
     * @param authStepResultDescription Description of the result of the last step.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with UpdateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOperationResponse> updateOperationPost(@NotNull String operationId, String userId, String organizationId, @NotNull AuthMethod authMethod, List<AuthInstrument> authInstruments, @NotNull AuthStepResult authStepResult, String authStepResultDescription, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        UpdateOperationRequest request = new UpdateOperationRequest();
        request.setOperationId(operationId);
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setAuthMethod(authMethod);
        request.getAuthInstruments().addAll(authInstruments);
        request.setAuthStepResult(authStepResult);
        request.setAuthStepResultDescription(authStepResultDescription);
        if (params != null) {
            request.getParams().addAll(params);
        }
        request.setApplicationContext(applicationContext);
        return postObjectImpl("/operation/update", new ObjectRequest<>(request), UpdateOperationResponse.class);
    }

    /**
     * Update user, organization and account status for an operation via PUT method.
     *
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus User account status.
     * @return Response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationUser(@NotNull String operationId, @NotNull String userId, String organizationId, UserAccountStatus accountStatus) throws NextStepClientException {
        UpdateOperationUserRequest request = new UpdateOperationUserRequest();
        request.setOperationId(operationId);
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setAccountStatus(accountStatus);
        return putObjectImpl("/operation/user", new ObjectRequest<>(request));
    }

    /**
     * UUpdate user, organization and account status for an operation via POST method.
     *
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus User account status.
     * @return Response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationUserPost(@NotNull String operationId, @NotNull String userId, String organizationId, UserAccountStatus accountStatus) throws NextStepClientException {
        UpdateOperationUserRequest request = new UpdateOperationUserRequest();
        request.setOperationId(operationId);
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setAccountStatus(accountStatus);
        return postObjectImpl("/operation/user/update", new ObjectRequest<>(request));
    }

    /**
     * Update operation form data via PUT method.
     *
     * @param operationId Operation ID.
     * @param formData Form data.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationFormData(@NotNull String operationId, @NotNull OperationFormData formData) throws NextStepClientException {
        UpdateFormDataRequest request = new UpdateFormDataRequest();
        request.setOperationId(operationId);
        request.setFormData(formData);
        return putObjectImpl("/operation/formData", new ObjectRequest<>(request));
    }

    /**
     * Update operation form data via POST method.
     *
     * @param operationId Operation ID.
     * @param formData Form data.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationFormDataPost(@NotNull String operationId, @NotNull OperationFormData formData) throws NextStepClientException {
        UpdateFormDataRequest request = new UpdateFormDataRequest();
        request.setOperationId(operationId);
        request.setFormData(formData);
        return postObjectImpl("/operation/formData/update", new ObjectRequest<>(request));
    }

    /**
     * Update chosen authentication method for current operation step via PUT method.
     *
     * @param operationId Operation ID.
     * @param chosenAuthMethod Chosen authentication method.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateChosenAuthMethod(@NotNull String operationId, @NotNull AuthMethod chosenAuthMethod) throws NextStepClientException {
        UpdateChosenAuthMethodRequest request = new UpdateChosenAuthMethodRequest();
        request.setOperationId(operationId);
        request.setChosenAuthMethod(chosenAuthMethod);
        return putObjectImpl("/operation/chosenAuthMethod", new ObjectRequest<>(request));
    }

    /**
     * Update chosen authentication method for current operation step via POST method.
     *
     * @param operationId Operation ID.
     * @param chosenAuthMethod Chosen authentication method.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateChosenAuthMethodPost(@NotNull String operationId, @NotNull AuthMethod chosenAuthMethod) throws NextStepClientException {
        UpdateChosenAuthMethodRequest request = new UpdateChosenAuthMethodRequest();
        request.setOperationId(operationId);
        request.setChosenAuthMethod(chosenAuthMethod);
        return postObjectImpl("/operation/chosenAuthMethod/update", new ObjectRequest<>(request));
    }

    /**
     * Update application context for current operation step via PUT method.
     *
     * @param operationId Operation ID.
     * @param applicationContext Application context.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateApplicationContext(@NotNull String operationId, @NotNull ApplicationContext applicationContext) throws NextStepClientException {
        UpdateApplicationContextRequest request = new UpdateApplicationContextRequest();
        request.setOperationId(operationId);
        request.setApplicationContext(applicationContext);
        return putObjectImpl("/operation/application", new ObjectRequest<>(request));
    }

    /**
     * Update application context for current operation step via POST method.
     *
     * @param operationId Operation ID.
     * @param applicationContext Application context.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateApplicationContextPost(@NotNull String operationId, @NotNull ApplicationContext applicationContext) throws NextStepClientException {
        UpdateApplicationContextRequest request = new UpdateApplicationContextRequest();
        request.setOperationId(operationId);
        request.setApplicationContext(applicationContext);
        return postObjectImpl("/operation/application/update", new ObjectRequest<>(request));
    }

    /**
     * Update mobile token status for current operation step via PUT method.
     *
     * @param operationId Operation ID.
     * @param mobileTokenActive Whether mobile token is active.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateMobileToken(@NotNull String operationId, boolean mobileTokenActive) throws NextStepClientException {
        UpdateMobileTokenRequest request = new UpdateMobileTokenRequest();
        request.setOperationId(operationId);
        request.setMobileTokenActive(mobileTokenActive);
        return putObjectImpl("/operation/mobileToken/status", new ObjectRequest<>(request));
    }

    /**
     * Update mobile token status for current operation step via POST method.
     *
     * @param operationId Operation ID.
     * @param mobileTokenActive Whether mobile token is active.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateMobileTokenPost(@NotNull String operationId, boolean mobileTokenActive) throws NextStepClientException {
        UpdateMobileTokenRequest request = new UpdateMobileTokenRequest();
        request.setOperationId(operationId);
        request.setMobileTokenActive(mobileTokenActive);
        return postObjectImpl("/operation/mobileToken/status/update", new ObjectRequest<>(request));
    }

    /**
     * Get mobile token configuration configuration.
     *
     * @param userId User ID.
     * @param operationName Operation name.
     * @param authMethod Authentication method.
     * @return Mobile token configuration.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetMobileTokenConfigResponse> getMobileTokenConfig(@NotNull String userId, @NotNull String operationName, @NotNull AuthMethod authMethod) throws NextStepClientException {
        GetMobileTokenConfigRequest request = new GetMobileTokenConfigRequest();
        request.setUserId(userId);
        request.setOperationName(operationName);
        request.setAuthMethod(authMethod);
        return postObjectImpl("/operation/mobileToken/config/detail", new ObjectRequest<>(request), GetMobileTokenConfigResponse.class);
    }

    /**
     * Calls the operation details endpoint via POST method to get operation details.
     *
     * @param operationId Operation ID.
     * @return A Response with {@link GetOperationDetailResponse} object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOperationDetailResponse> getOperationDetail(@NotNull String operationId) throws NextStepClientException {
        GetOperationDetailRequest request = new GetOperationDetailRequest();
        request.setOperationId(operationId);
        return postObjectImpl("/operation/detail", new ObjectRequest<>(request), GetOperationDetailResponse.class);
    }

    /**
     * Calls the operation lookup by external transaction ID endpoint via POST method.
     *
     * @param externalTransactionId Operation ID.
     * @return A Response with {@link GetOperationDetailResponse} object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<LookupOperationsByExternalIdResponse> lookupOperationByExternalTransactionId(@NotNull String externalTransactionId) throws NextStepClientException {
        LookupOperationsByExternalIdRequest request = new LookupOperationsByExternalIdRequest();
        request.setExternalTransactionId(externalTransactionId);
        return postObjectImpl("/operation/lookup/external", new ObjectRequest<>(request), LookupOperationsByExternalIdResponse.class);
    }

    /**
     * Create an AFS action in Next Step and log its request and response parameters.
     *
     * @param operationId Operation ID.
     * @param afsAction AFS action.
     * @param stepIndex Step index.
     * @param requestAfsExtras AFS request extras.
     * @param afsLabel AFS label.
     * @param afsResponseApplied Whether AFS response was applied.
     * @param responseAfsExtras AFS response extras.
     * @return Response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response createAfsAction(@NotNull String operationId, @NotNull String afsAction, int stepIndex, String requestAfsExtras, @NotNull String afsLabel,
                                    boolean afsResponseApplied, String responseAfsExtras) throws NextStepClientException {
        CreateAfsActionRequest request = new CreateAfsActionRequest();
        request.setOperationId(operationId);
        request.setAfsAction(afsAction);
        request.setStepIndex(stepIndex);
        request.setRequestAfsExtras(requestAfsExtras);
        request.setAfsLabel(afsLabel);
        request.setAfsResponseApplied(afsResponseApplied);
        request.setResponseAfsExtras(responseAfsExtras);
        request.setTimestampCreated(new Date());
        return postObjectImpl("/operation/afs/action", new ObjectRequest<>(request));
    }

    /**
     * Create operation configuration.
     *
     * @param request Create operation configuration request.
     * @return Operation configuration.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOperationConfigResponse> createOperationConfig(@NotNull CreateOperationConfigRequest request) throws NextStepClientException {
        return postObjectImpl("/operation/config", new ObjectRequest<>(request), CreateOperationConfigResponse.class);
    }

    /**
     * Get operation configuration.
     *
     * @param operationName Operation name.
     * @return Operation configuration.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOperationConfigDetailResponse> getOperationConfigDetail(@NotNull String operationName) throws NextStepClientException {
        GetOperationConfigDetailRequest request = new GetOperationConfigDetailRequest();
        request.setOperationName(operationName);
        return postObjectImpl("/operation/config/detail", new ObjectRequest<>(request), GetOperationConfigDetailResponse.class);
    }

    /**
     * Get all operation configurations.
     *
     * @return All operation configurations.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOperationConfigListResponse> getOperationConfigList() throws NextStepClientException {
        GetOperationConfigListRequest request = new GetOperationConfigListRequest();
        return postObjectImpl("/operation/config/list", new ObjectRequest<>(request), GetOperationConfigListResponse.class);
    }

    /**
     * Delete an operation configuration.
     *
     * @param operationName Operation name.
     * @return Delete operation configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteOperationConfigResponse> deleteOperationConfig(@NotNull String operationName) throws NextStepClientException {
        DeleteOperationConfigRequest request = new DeleteOperationConfigRequest();
        request.setOperationName(operationName);
        return postObjectImpl("/operation/config/delete", new ObjectRequest<>(request), DeleteOperationConfigResponse.class);
    }

    /**
     * Get list of pending operations for given user.
     *
     * @param userId User ID.
     * @return List of pending operations.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(@NotNull String userId) throws NextStepClientException {
        return getPendingOperations(userId, false);
    }

    /**
     * Get list of pending operations for given user and authentication method.
     *
     * @param userId User ID.
     * @param mobileTokenOnly Whether pending operation list should be filtered for only next step with mobile token support.
     * @return A Response with list of {@link GetOperationDetailResponse}.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(@NotNull String userId, boolean mobileTokenOnly) throws NextStepClientException {
        GetPendingOperationsRequest request = new GetPendingOperationsRequest();
        request.setUserId(userId);
        request.setMobileTokenOnly(mobileTokenOnly);
        return postImpl("/user/operation/list", new ObjectRequest<>(request), new ParameterizedTypeReference<ObjectResponse<List<GetOperationDetailResponse>>>() {});
    }

    // Organization related methods

    /**
     * Create an organization.
     *
     * @param organizationId Organization ID.
     * @param displayNameKey Display name key for localization.
     * @param isDefault Whether organization is the default one.
     * @param orderNumber Order number.
     * @return Create organization response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOrganizationResponse> createOrganization(@NotNull String organizationId, String displayNameKey,
                                                                         boolean isDefault, @NotNull Integer orderNumber) throws NextStepClientException {
        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setOrganizationId(organizationId);
        request.setDisplayNameKey(displayNameKey);
        request.setDefault(isDefault);
        request.setOrderNumber(orderNumber);
        return postObjectImpl("/organization", new ObjectRequest<>(request), CreateOrganizationResponse.class);
    }

    /**
     * Get organization detail.
     *
     * @param organizationId Organization ID.
     * @return Organization detail.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOrganizationDetailResponse> getOrganizationDetail(@NotNull String organizationId) throws NextStepClientException {
        GetOrganizationDetailRequest request = new GetOrganizationDetailRequest();
        request.setOrganizationId(organizationId);
        return postObjectImpl("/organization/detail", new ObjectRequest<>(request), GetOrganizationDetailResponse.class);
    }

    /**
     * Get all organizations.
     *
     * @return All organizations.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOrganizationListResponse> getOrganizationList() throws NextStepClientException {
        GetOrganizationListRequest request = new GetOrganizationListRequest();
        return postObjectImpl("/organization/list", new ObjectRequest<>(request), GetOrganizationListResponse.class);
    }

    /**
     * Delete an organization.
     *
     * @param organizationId Organization ID.
     * @return Delete organization response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteOrganizationResponse> deleteOrganization(@NotNull String organizationId) throws NextStepClientException {
        DeleteOrganizationRequest request = new DeleteOrganizationRequest();
        request.setOrganizationId(organizationId);
        return postObjectImpl("/organization/delete", new ObjectRequest<>(request), DeleteOrganizationResponse.class);
    }

    // Step definition related methods

    /**
     * Create a step definition.
     *
     * @param request Create step definition request.
     * @return Create step definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateStepDefinitionResponse> createStepDefinition(@NotNull CreateStepDefinitionRequest request) throws NextStepClientException {
        return postObjectImpl("/step/definition", new ObjectRequest<>(request), CreateStepDefinitionResponse.class);
    }

    /**
     * Delete a step definition.
     *
     * @param stepDefinitionId Step definition ID.
     * @return Delete step definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteStepDefinitionResponse> deleteStepDefinition(long stepDefinitionId) throws NextStepClientException {
        DeleteStepDefinitionRequest request = new DeleteStepDefinitionRequest();
        request.setStepDefinitionId(stepDefinitionId);
        return postObjectImpl("/step/definition/delete", new ObjectRequest<>(request), DeleteStepDefinitionResponse.class);
    }

    // Authentication method related methods

    /**
     * Create an authentication method.
     *
     * @param request Create authentication method request.
     * @return Create authentication method response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateAuthMethodResponse> createAuthMethod(@NotNull CreateAuthMethodRequest request) throws NextStepClientException {
        return postObjectImpl("/auth-method", new ObjectRequest<>(request), CreateAuthMethodResponse.class);
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @return List of authentication methods wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetAuthMethodsResponse> getAuthMethodList() throws NextStepClientException {
        GetAuthMethodListRequest request = new GetAuthMethodListRequest();
        return postObjectImpl("/auth-method/list", new ObjectRequest<>(request), GetAuthMethodsResponse.class);
    }

    /**
     * Delete an authentication method.
     *
     * @param authMethod Authentication method.
     * @return Delete authentication method response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteAuthMethodResponse> deleteAuthMethod(AuthMethod authMethod) throws NextStepClientException {
        DeleteAuthMethodRequest request = new DeleteAuthMethodRequest();
        request.setAuthMethod(authMethod);
        return postObjectImpl("/auth-method/delete", new ObjectRequest<>(request), DeleteAuthMethodResponse.class);
    }

    /**
     * Get all globally enabled authentication methods for given user. Do not perform checks of individual
     * authentication methods whether they are currently available for authentication.
     *
     * @param userId User ID.
     * @return List of globally enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsForUser(@NotNull String userId) throws NextStepClientException {
        GetUserAuthMethodsRequest request = new GetUserAuthMethodsRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/auth-method/list", new ObjectRequest<>(request), GetUserAuthMethodsResponse.class);
    }

    /**
     * Get all currently enabled authentication methods for given user and operation name. Perform an actual check of each
     * authentication method at the current moment to make they are currently available for authentication. Filter the
     * authentication method list by the steps in given operation.
     *
     * @param userId User ID.
     * @param operationName Operation name.
     * @return List of currently enabled and available authentication methods for given user wrapped in GetEnabledMethodListResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetEnabledMethodListResponse> getAuthMethodsEnabledForUser(@NotNull String userId, @NotNull String operationName) throws NextStepClientException {
        GetEnabledMethodListRequest request = new GetEnabledMethodListRequest();
        request.setUserId(userId);
        request.setOperationName(operationName);
        return postObjectImpl("/user/auth-method/enabled/list", new ObjectRequest<>(request), GetEnabledMethodListResponse.class);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @param config Authentication method configuration.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetAuthMethodsResponse> enableAuthMethodForUser(@NotNull String userId, @NotNull AuthMethod authMethod, Map<String, String> config) throws NextStepClientException {
        UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
        request.setUserId(userId);
        request.setAuthMethod(authMethod);
        if (config != null) {
            request.getConfig().putAll(config);
        }
        return postObjectImpl("/user/auth-method", new ObjectRequest<>(request), GetAuthMethodsResponse.class);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetAuthMethodsResponse> disableAuthMethodForUser(@NotNull String userId, @NotNull AuthMethod authMethod, Map<String, String> config) throws NextStepClientException {
        UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
        request.setUserId(userId);
        request.setAuthMethod(authMethod);
        if (config != null) {
            request.getConfig().putAll(config);
        }
        return postObjectImpl("/user/auth-method/delete", new ObjectRequest<>(request), GetAuthMethodsResponse.class);
    }

    // Next Step application related methods

    /**
     * Create a Next Step application.
     *
     * @param applicationName Application name.
     * @param description Application description.
     * @param organizationId Organization ID.
     * @return Create application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateApplicationResponse> createApplication(@NotNull String applicationName, String description, String organizationId) throws NextStepClientException {
        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setApplicationName(applicationName);
        request.setDescription(description);
        request.setOrganizationId(organizationId);
        return postObjectImpl("/application", new ObjectRequest<>(request), CreateApplicationResponse.class);
    }

    /**
     * Update a Next Step application via PUT method.
     *
     * @param applicationName Application name.
     * @param description Application description.
     * @param organizationId Organization ID.
     * @param status Application status.
     * @return Update application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateApplicationResponse> updateApplication(@NotNull String applicationName, String description, String organizationId, ApplicationStatus status) throws NextStepClientException {
        UpdateApplicationRequest request = new UpdateApplicationRequest();
        request.setApplicationName(applicationName);
        request.setDescription(description);
        request.setOrganizationId(organizationId);
        request.setApplicationStatus(status);
        return putObjectImpl("/application", new ObjectRequest<>(request), UpdateApplicationResponse.class);
    }

    /**
     * Update a Next Step application via POST method.
     *
     * @param applicationName Application name.
     * @param description Application description.
     * @param organizationId Organization ID.
     * @param status Application status.
     * @return Update application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateApplicationResponse> updateApplicationPost(@NotNull String applicationName, String description, String organizationId, ApplicationStatus status) throws NextStepClientException {
        UpdateApplicationRequest request = new UpdateApplicationRequest();
        request.setApplicationName(applicationName);
        request.setDescription(description);
        request.setOrganizationId(organizationId);
        request.setApplicationStatus(status);
        return postObjectImpl("/application/update", new ObjectRequest<>(request), UpdateApplicationResponse.class);
    }

    /**
     * Get Next Step application list.
     *
     * @param includeRemoved Whether removed applications should be included in the application list.
     * @return Get application list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetApplicationListResponse> getApplicationList(boolean includeRemoved) throws NextStepClientException {
        GetApplicationListRequest request = new GetApplicationListRequest();
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/application/list", new ObjectRequest<>(request), GetApplicationListResponse.class);
    }

    /**
     * Delete a Next Step application via PUT method.
     *
     * @param applicationName Application name.
     * @return Delete application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteApplicationResponse> deleteApplication(@NotNull String applicationName) throws NextStepClientException {
        DeleteApplicationRequest request = new DeleteApplicationRequest();
        request.setApplicationName(applicationName);
        return postObjectImpl("/application/delete", new ObjectRequest<>(request), DeleteApplicationResponse.class);
    }

    // Role related methods

    /**
     * Create a user role.
     *
     * @param roleName Role name.
     * @param description Role description.
     * @return Create user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateRoleResponse> createRole(@NotNull String roleName, String description) throws NextStepClientException {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName(roleName);
        request.setDescription(description);
        return postObjectImpl("/role", new ObjectRequest<>(request), CreateRoleResponse.class);
    }

    /**
     * Get the list of user roles.
     *
     * @return Get user role list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetRoleListResponse> getRoleList() throws NextStepClientException {
        GetRoleListRequest request = new GetRoleListRequest();
        return postObjectImpl("/role/list", new ObjectRequest<>(request), GetRoleListResponse.class);
    }

    /**
     * Delete a user role.
     *
     * @param roleName Role name.
     * @return Delete user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteRoleResponse> deleteRole(@NotNull String roleName) throws NextStepClientException {
        DeleteRoleRequest request = new DeleteRoleRequest();
        request.setRoleName(roleName);
        return postObjectImpl("/role/delete", new ObjectRequest<>(request), DeleteRoleResponse.class);
    }

    // Hashing configuration related methods

    /**
     * Create a hashing configuration.
     *
     * @param hashConfigName Hashing configuration name.
     * @param algorithm Hashing algorithm.
     * @param parameters Hashing algorithm parameters.
     * @return Create hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateHashConfigResponse> createHashConfig(@NotNull String hashConfigName, @NotNull String algorithm, Map<String, String> parameters) throws NextStepClientException {
        CreateHashConfigRequest request = new CreateHashConfigRequest();
        request.setHashConfigName(hashConfigName);
        request.setAlgorithm(algorithm);
        if (parameters != null) {
            request.getParameters().putAll(parameters);
        }
        return postObjectImpl("/hashconfig", new ObjectRequest<>(request), CreateHashConfigResponse.class);
    }

    /**
     * Update a hashing configuration via PUT method.
     *
     * @param hashConfigName Hashing configuration name.
     * @param algorithm Hashing algorithm.
     * @param parameters Hashing algorithm parameters.
     * @param status Hashing configuration status.
     * @return Update hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfig(@NotNull String hashConfigName, @NotNull String algorithm, Map<String, String> parameters, HashConfigStatus status) throws NextStepClientException {
        UpdateHashConfigRequest request = new UpdateHashConfigRequest();
        request.setHashConfigName(hashConfigName);
        request.setAlgorithm(algorithm);
        if (parameters != null) {
            request.getParameters().putAll(parameters);
        }
        request.setHashConfigStatus(status);
        return putObjectImpl("/hashconfig", new ObjectRequest<>(request), UpdateHashConfigResponse.class);
    }

    /**
     * Update a hashing configuration via POST method.
     *
     * @param hashConfigName Hashing configuration name.
     * @param algorithm Hashing algorithm.
     * @param parameters Hashing algorithm parameters.
     * @param status Hashing configuration status.
     * @return Update hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfigPost(@NotNull String hashConfigName, @NotNull String algorithm, Map<String, String> parameters, HashConfigStatus status) throws NextStepClientException {
        UpdateHashConfigRequest request = new UpdateHashConfigRequest();
        request.setHashConfigName(hashConfigName);
        request.setAlgorithm(algorithm);
        if (parameters != null) {
            request.getParameters().putAll(parameters);
        }
        request.setHashConfigStatus(status);
        return postObjectImpl("/hashconfig/update", new ObjectRequest<>(request), UpdateHashConfigResponse.class);
    }

    /**
     * Get list of hashing configurations.
     *
     * @return Get hashing configuration list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetHashConfigListResponse> getHashConfigList(boolean includeRemoved) throws NextStepClientException {
        GetHashConfigListRequest request = new GetHashConfigListRequest();
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/hashconfig/list", new ObjectRequest<>(request), GetHashConfigListResponse.class);
    }

    /**
     * Delete a hashing configurations.
     *
     * @return Delete hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteHashConfigResponse> deleteHashConfig(@NotNull String hashConfigName) throws NextStepClientException {
        DeleteHashConfigRequest request = new DeleteHashConfigRequest();
        request.setHashConfigName(hashConfigName);
        return postObjectImpl("/hashconfig/delete", new ObjectRequest<>(request), DeleteHashConfigResponse.class);
    }

    // Credential policy related methods

    /**
     * Create a credential policy.
     *
     * @param request Create credential policy request.
     * @return Create credential policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateCredentialPolicyResponse> createCredentialPolicy(@NotNull CreateCredentialPolicyRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/policy", new ObjectRequest<>(request), CreateCredentialPolicyResponse.class);
    }

    /**
     * Update a credential policy via PUT method.
     *
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicy(@NotNull UpdateCredentialRequest request) throws NextStepClientException {
        return putObjectImpl("/credential/policy", new ObjectRequest<>(request), UpdateCredentialPolicyResponse.class);
    }

    /**
     * Update a credential policy via POST method.
     *
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicyPost(@NotNull UpdateCredentialPolicyRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/policy/update", new ObjectRequest<>(request), UpdateCredentialPolicyResponse.class);
    }

    /**
     * Get credential policy list.
     *
     * @param includeRemoved Whether removed credential policies should be included.
     * @return Get credential policy list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetCredentialPolicyListResponse> getCredentialPolicyList(boolean includeRemoved) throws NextStepClientException {
        GetCredentialPolicyListRequest request = new GetCredentialPolicyListRequest();
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/credential/policy/list", new ObjectRequest<>(request), GetCredentialPolicyListResponse.class);
    }

    /**
     * Delete a credential policy.
     *
     * @param credentialPolicyName Credential policy name.
     * @return Delete credential policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteCredentialPolicyResponse> deleteCredentialPolicy(@NotNull String credentialPolicyName) throws NextStepClientException {
        DeleteCredentialPolicyRequest request = new DeleteCredentialPolicyRequest();
        request.setCredentialPolicyName(credentialPolicyName);
        return postObjectImpl("/credential/policy/delete", new ObjectRequest<>(request), DeleteCredentialPolicyResponse.class);
    }

    // Credential definition related methods

    /**
     * Create a credential definition.
     *
     * @param request Create credential definition request.
     * @return Create credential definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateCredentialDefinitionResponse> createCredentialDefinition(@NotNull CreateCredentialDefinitionRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/definition", new ObjectRequest<>(request), CreateCredentialDefinitionResponse.class);
    }

    /**
     * Update a credential definition via PUT method.
     *
     * @param request Update credential definition request.
     * @return Update credential definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialDefinitionResponse> updateCredentialDefinition(@NotNull UpdateCredentialDefinitionRequest request) throws NextStepClientException {
        return putObjectImpl("/credential/definition", new ObjectRequest<>(request), UpdateCredentialDefinitionResponse.class);
    }

    /**
     * Update a credential definition via POST method.
     *
     * @param request Update credential definition request.
     * @return Update credential definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialDefinitionResponse> updateCredentialDefinitionPost(@NotNull UpdateCredentialRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/definition/update", new ObjectRequest<>(request), UpdateCredentialDefinitionResponse.class);
    }

    /**
     * Get credential definition list.
     *
     * @param includeRemoved Whether removed credential definitions should be included.
     * @return Get credential definition list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetCredentialDefinitionListResponse> getCredentialDefinitionList(boolean includeRemoved) throws NextStepClientException {
        GetCredentialDefinitionListRequest request = new GetCredentialDefinitionListRequest();
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/credential/definition/list", new ObjectRequest<>(request), GetCredentialDefinitionListResponse.class);
    }

    /**
     * Delete a credential definition.
     *
     * @param credentialDefinitionName Credential definition name.
     * @return Delete credential definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteCredentialDefinitionResponse> deleteCredentialDefinition(@NotNull String credentialDefinitionName) throws NextStepClientException {
        DeleteCredentialDefinitionRequest request = new DeleteCredentialDefinitionRequest();
        request.setCredentialDefinitionName(credentialDefinitionName);
        return postObjectImpl("/credential/definition/delete", new ObjectRequest<>(request), DeleteCredentialDefinitionResponse.class);
    }

    // OTP policy related methods

    /**
     * Create a OTP policy.
     *
     * @param request Create OTP policy request.
     * @return Create OTP policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOtpPolicyResponse> createOtpPolicy(@NotNull CreateOtpPolicyRequest request) throws NextStepClientException {
        return postObjectImpl("/otp/policy", new ObjectRequest<>(request), CreateOtpPolicyResponse.class);
    }

    /**
     * Update a OTP policy via PUT method.
     *
     * @param request Update OTP policy request.
     * @return Update OTP policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicy(@NotNull UpdateOtpPolicyRequest request) throws NextStepClientException {
        return putObjectImpl("/otp/policy", new ObjectRequest<>(request), UpdateOtpPolicyResponse.class);
    }

    /**
     * Update a otp policy via POST method.
     *
     * @param request Update OTP policy request.
     * @return Update OTP policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicyPost(@NotNull UpdateOtpPolicyRequest request) throws NextStepClientException {
        return postObjectImpl("/otp/policy/update", new ObjectRequest<>(request), UpdateOtpPolicyResponse.class);
    }

    /**
     * Get OTP policy list.
     *
     * @param includeRemoved Whether removed OTP policies should be included.
     * @return Get OTP policy list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpPolicyListResponse> getOtpPolicyList(boolean includeRemoved) throws NextStepClientException {
        GetOtpPolicyListRequest request = new GetOtpPolicyListRequest();
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/otp/policy/list", new ObjectRequest<>(request), GetOtpPolicyListResponse.class);
    }

    /**
     * Delete a OTP policy.
     *
     * @param otpPolicyName OTP policy name.
     * @return Delete OTP policy response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteOtpPolicyResponse> deleteOtpPolicy(@NotNull String otpPolicyName) throws NextStepClientException {
        DeleteOtpPolicyRequest request = new DeleteOtpPolicyRequest();
        request.setOtpPolicyName(otpPolicyName);
        return postObjectImpl("/otp/policy/delete", new ObjectRequest<>(request), DeleteOtpPolicyResponse.class);
    }

    // OTP definition related methods

    /**
     * Create a otp definition.
     *
     * @param request Create otp definition request.
     * @return Create otp definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOtpDefinitionResponse> createOtpDefinition(@NotNull CreateOtpDefinitionRequest request) throws NextStepClientException {
        return postObjectImpl("/otp/definition", new ObjectRequest<>(request), CreateOtpDefinitionResponse.class);
    }

    /**
     * Update a OTP definition via PUT method.
     *
     * @param request Update OTP definition request.
     * @return Update OTP definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOtpDefinitionResponse> updateOtpDefinition(@NotNull UpdateOtpDefinitionRequest request) throws NextStepClientException {
        return putObjectImpl("/otp/definition", new ObjectRequest<>(request), UpdateOtpDefinitionResponse.class);
    }

    /**
     * Update a OTP definition via POST method.
     *
     * @param request Update OTP definition request.
     * @return Update OTP definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOtpDefinitionResponse> updateOtpDefinitionPost(@NotNull UpdateOtpDefinitionRequest request) throws NextStepClientException {
        return postObjectImpl("/otp/definition/update", new ObjectRequest<>(request), UpdateOtpDefinitionResponse.class);
    }

    /**
     * Get OTP definition list.
     *
     * @param includeRemoved Whether removed OTP definitions should be included.
     * @return Get OTP definition list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpDefinitionListResponse> getOtpDefinitionList(boolean includeRemoved) throws NextStepClientException {
        GetOtpDefinitionListRequest request = new GetOtpDefinitionListRequest();
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/otp/definition/list", new ObjectRequest<>(request), GetOtpDefinitionListResponse.class);
    }

    /**
     * Delete a OTP definition.
     *
     * @param otpDefinitionName OTP definition name.
     * @return Delete OTP definition response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteOtpDefinitionResponse> deleteOtpDefinition(@NotNull String otpDefinitionName) throws NextStepClientException {
        DeleteOtpDefinitionRequest request = new DeleteOtpDefinitionRequest();
        request.setOtpDefinitionName(otpDefinitionName);
        return postObjectImpl("/otp/definition/delete", new ObjectRequest<>(request), DeleteOtpDefinitionResponse.class);
    }

    // User identity related methods

    /**
     * Create a user identity.
     *
     * @param request Create user request.
     * @return Create user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateUserResponse> createUser(@NotNull CreateUserRequest request) throws NextStepClientException {
        return postObjectImpl("/user", new ObjectRequest<>(request), CreateUserResponse.class);
    }

    /**
     * Update a user identity via PUT method.
     *
     * @param request Update user request.
     * @return Update user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserResponse> updateUser(@NotNull UpdateUserRequest request) throws NextStepClientException {
        return putObjectImpl("/user", new ObjectRequest<>(request), UpdateUserResponse.class);
    }

    /**
     * Update a user identity via POST method.
     *
     * @param request Update user request.
     * @return Update user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserResponse> updateUserPost(@NotNull UpdateUserRequest request) throws NextStepClientException {
        return postObjectImpl("/user/update", new ObjectRequest<>(request), UpdateUserResponse.class);
    }

    /**
     * Update statuses of multiple user identities.
     *
     * @param userIds User identifiers.
     * @param status Status to use.
     * @return Update users response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUsersResponse> updateUsers(@NotNull List<String> userIds, @NotNull UserIdentityStatus status) throws NextStepClientException {
        UpdateUsersRequest request = new UpdateUsersRequest();
        request.getUserIds().addAll(userIds);
        request.setUserIdentityStatus(status);
        return postObjectImpl("/user/update/multi", new ObjectRequest<>(request), UpdateUsersResponse.class);
    }

    /**
     * Get user detail.
     *
     * @param userId User ID.
     * @return Get user detail response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserDetailResponse> getUserDetail(@NotNull String userId) throws NextStepClientException {
        GetUserDetailRequest request = new GetUserDetailRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/detail", new ObjectRequest<>(request), GetUserDetailResponse.class);
    }

    /**
     * Lookup a user identity.
     *
     * @param request Lookup user request.
     * @return Lookup user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<LookupUserResponse> lookupUser(@NotNull LookupUserRequest request) throws NextStepClientException {
        return postObjectImpl("/user/lookup", new ObjectRequest<>(request), LookupUserResponse.class);
    }

    /**
     * Block a user identity.
     *
     * @param userId User ID.
     * @return Block user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<BlockUserResponse> blockUser(@NotNull String userId) throws NextStepClientException {
        BlockUserRequest request = new BlockUserRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/block", new ObjectRequest<>(request), BlockUserResponse.class);
    }

    /**
     * Unblock a user identity.
     *
     * @param userId User ID.
     * @return Unblock user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UnblockUserResponse> unblockUser(@NotNull String userId) throws NextStepClientException {
        UnblockUserRequest request = new UnblockUserRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/unblock", new ObjectRequest<>(request), UnblockUserResponse.class);
    }

    /**
     * Delete a user identity.
     *
     * @param userId User ID.
     * @return Delete user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteUserResponse> deleteUser(@NotNull String userId) throws NextStepClientException {
        DeleteUserRequest request = new DeleteUserRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/delete", new ObjectRequest<>(request), DeleteUserResponse.class);
    }

    /**
     * Add a role to a user.
     *
     * @param userId User ID.
     * @param roleName Role name.
     * @return Add user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<AddUserRoleResponse> addUserRole(@NotNull String userId, @NotNull String roleName) throws NextStepClientException {
        AddUserRoleRequest request = new AddUserRoleRequest();
        request.setUserId(userId);
        request.setRoleName(roleName);
        return postObjectImpl("/user/role", new ObjectRequest<>(request), AddUserRoleResponse.class);
    }

    /**
     * Remove a role to a user.
     *
     * @param userId User ID.
     * @param roleName Role name.
     * @return Remove user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<RemoveUserRoleResponse> removeUserRole(@NotNull String userId, @NotNull String roleName) throws NextStepClientException {
        RemoveUserRoleRequest request = new RemoveUserRoleRequest();
        request.setUserId(userId);
        request.setRoleName(roleName);
        return postObjectImpl("/user/role/remove", new ObjectRequest<>(request), RemoveUserRoleResponse.class);
    }

    /**
     * Create a user contact.
     *
     * @param userId User ID.
     * @param contactName Contact name.
     * @param contactType Contact type.
     * @param contactValue Contact value.
     * @param primary Whether contact is primary.
     * @return Create user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateUserContactResponse> createUserContact(@NotNull String userId, @NotNull String contactName, @NotNull ContactType contactType,
                                                                       @NotNull String contactValue, boolean primary) throws NextStepClientException {
        CreateUserContactRequest request = new CreateUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        request.setContactType(contactType);
        request.setContactValue(contactValue);
        request.setPrimary(primary);
        return postObjectImpl("/user/contact", new ObjectRequest<>(request), CreateUserContactResponse.class);
    }

    /**
     * Update a user contact via PUT method.
     *
     * @param userId User ID.
     * @param contactName Contact name.
     * @param contactType Contact type.
     * @param contactValue Contact value.
     * @param primary Whether contact is primary.
     * @return Update user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserContactResponse> updateUserContact(@NotNull String userId, @NotNull String contactName, @NotNull ContactType contactType,
                                                                       @NotNull String contactValue, boolean primary) throws NextStepClientException {
        UpdateUserContactRequest request = new UpdateUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        request.setContactType(contactType);
        request.setContactValue(contactValue);
        request.setPrimary(primary);
        return putObjectImpl("/user/contact", new ObjectRequest<>(request), UpdateUserContactResponse.class);
    }

    /**
     * Update a user contact via POST method.
     *
     * @param userId User ID.
     * @param contactName Contact name.
     * @param contactType Contact type.
     * @param contactValue Contact value.
     * @param primary Whether contact is primary.
     * @return Update user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserContactResponse> updateUserContactPost(@NotNull String userId, @NotNull String contactName, @NotNull ContactType contactType,
                                                                           @NotNull String contactValue, boolean primary) throws NextStepClientException {
        UpdateUserContactRequest request = new UpdateUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        request.setContactType(contactType);
        request.setContactValue(contactValue);
        request.setPrimary(primary);
        return postObjectImpl("/user/contact/update", new ObjectRequest<>(request), UpdateUserContactResponse.class);
    }

    /**
     * Get user contact list.
     *
     * @param userId User ID.
     * @return Get user contact list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserContactListResponse> getUserContactList(@NotNull String userId) throws NextStepClientException {
        GetUserContactListRequest request = new GetUserContactListRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/contact/list", new ObjectRequest<>(request), GetUserContactListResponse.class);
    }

    /**
     * Delete a user contact.
     *
     * @param userId User ID.
     * @param contactName Contact name.
     * @return Delete user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteUserContactResponse> deleteUserContact(@NotNull String userId, @NotNull String contactName) throws NextStepClientException {
        DeleteUserContactRequest request = new DeleteUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        return postObjectImpl("/user/contact/delete", new ObjectRequest<>(request), DeleteUserContactResponse.class);
    }

    /**
     * Create a user alias.
     *
     * @param userId User ID.
     * @param aliasName Alias name.
     * @param aliasValue Alias value.
     * @param extras Extra information related to the alias.
     * @return Create user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateUserAliasResponse> createUserAlias(@NotNull String userId, @NotNull String aliasName, @NotNull String aliasValue,
                                                                   Map<String, Object> extras) throws NextStepClientException {
        CreateUserAliasRequest request = new CreateUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        request.setAliasValue(aliasValue);
        if (extras != null) {
            request.getExtras().putAll(extras);
        }
        return postObjectImpl("/user/alias", new ObjectRequest<>(request), CreateUserAliasResponse.class);
    }

    /**
     * Update a user alias via PUT method.
     *
     * @param userId User ID.
     * @param aliasName Alias name.
     * @param aliasValue Alias value.
     * @param extras Extra information related to the alias.
     * @return Update user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserAliasResponse> updateUserAlias(@NotNull String userId, @NotNull String aliasName, @NotNull String aliasValue,
                                                                   Map<String, Object> extras) throws NextStepClientException {
        UpdateUserAliasRequest request = new UpdateUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        request.setAliasValue(aliasValue);
        if (extras != null) {
            request.getExtras().putAll(extras);
        }
        return putObjectImpl("/user/alias", new ObjectRequest<>(request), UpdateUserAliasResponse.class);
    }

    /**
     * Update a user alias via POST method.
     *
     * @param userId User ID.
     * @param aliasName Alias name.
     * @param aliasValue Alias value.
     * @param extras Extra information related to the alias.
     * @return Update user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserAliasResponse> updateUserAliasPost(@NotNull String userId, @NotNull String aliasName, @NotNull String aliasValue,
                                                                       Map<String, Object> extras) throws NextStepClientException {
        UpdateUserAliasRequest request = new UpdateUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        request.setAliasValue(aliasValue);
        if (extras != null) {
            request.getExtras().putAll(extras);
        }
        return postObjectImpl("/user/alias/update", new ObjectRequest<>(request), UpdateUserAliasResponse.class);
    }

    /**
     * Get user alias list.
     *
     * @param userId User ID.
     * @return Get user alias list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserAliasListResponse> getUserAliasList(@NotNull String userId) throws NextStepClientException {
        GetUserAliasListRequest request = new GetUserAliasListRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/alias/list", new ObjectRequest<>(request), GetUserAliasListResponse.class);
    }

    /**
     * Delete a user alias.
     *
     * @param userId User ID.
     * @param aliasName Alias name.
     * @return Delete user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteUserAliasResponse> deleteUserAlias(@NotNull String userId, @NotNull String aliasName) throws NextStepClientException {
        DeleteUserAliasRequest request = new DeleteUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        return postObjectImpl("/user/alias/delete", new ObjectRequest<>(request), DeleteUserAliasResponse.class);
    }

    /**
     * Get user credential list.
     *
     * @param userId User ID.
     * @param includeRemoved Whether removed credentials should be included.
     * @return Get user credential list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserCredentialListResponse> getUserCredentialList(@NotNull String userId, boolean includeRemoved) throws NextStepClientException {
        GetUserCredentialListRequest request = new GetUserCredentialListRequest();
        request.setUserId(userId);
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/user/credential/list", new ObjectRequest<>(request), GetUserCredentialListResponse.class);
    }

    /**
     * Get user authentication list.
     *
     * @param userId User ID.
     * @param createdStartDate Start of interval to use for date filter.
     * @param createdEndDate End of interval to use for date filter.
     * @return Get user authentication list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserAuthenticationListResponse> getUserAuthenticationList(@NotNull String userId, Date createdStartDate, Date createdEndDate) throws NextStepClientException {
        GetUserAuthenticationListRequest request = new GetUserAuthenticationListRequest();
        request.setUserId(userId);
        request.setCreatedStartDate(createdStartDate);
        request.setCreatedEndDate(createdEndDate);
        return postObjectImpl("/user/authentication/list", new ObjectRequest<>(request), GetUserAuthenticationListResponse.class);
    }

    // Credential related methods

    /**
     * Create a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @param credentialType Credential type.
     * @param username Username.
     * @param credentialValue Credential value.
     * @return Create credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateCredentialResponse> createCredential(@NotNull String userId, @NotNull String credentialName, @NotNull CredentialType credentialType,
                                                                     String username, String credentialValue) throws NextStepClientException {
        CreateCredentialRequest request = new CreateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        return postObjectImpl("/credential", new ObjectRequest<>(request), CreateCredentialResponse.class);
    }

    /**
     * Create a credential with credential history import.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @param credentialType Credential type.
     * @param username Username.
     * @param credentialValue Credential value.
     * @param validationMode Credential validation mode.
     * @param credentialHistory List with pairs of username -> credentialValue for credential history.
     * @return Create credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateCredentialResponse> createCredential(@NotNull String userId, @NotNull String credentialName, @NotNull CredentialType credentialType,
                                                                     String username, String credentialValue, CredentialValidationMode validationMode,
                                                                     List<KeyValueParameter> credentialHistory) throws NextStepClientException {
        CreateCredentialRequest request = new CreateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        request.setValidationMode(validationMode);
        if (credentialHistory != null) {
            credentialHistory.forEach(pair -> {
                CreateCredentialRequest.CredentialHistory h = new CreateCredentialRequest.CredentialHistory();
                h.setUsername(pair.getKey());
                h.setCredentialValue(pair.getValue());
                request.getCredentialHistory().add(h);
            });
        }
        return postObjectImpl("/credential", new ObjectRequest<>(request), CreateCredentialResponse.class);
    }

    /**
     * Update a credential via PUT method.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @param credentialType Credential type.
     * @param username Username.
     * @param credentialValue Credential value.
     * @param credentialStatus Credential status.
     * @return Update credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialResponse> updateCredential(@NotNull String userId, @NotNull String credentialName, CredentialType credentialType,
                                                                     String username, String credentialValue, CredentialStatus credentialStatus) throws NextStepClientException {
        UpdateCredentialRequest request = new UpdateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        request.setCredentialStatus(credentialStatus);
        return putObjectImpl("/credential", new ObjectRequest<>(request), UpdateCredentialResponse.class);
    }

    /**
     * Update a credential via POST method.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @param credentialType Credential type.
     * @param username Username.
     * @param credentialValue Credential value.
     * @return Update credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialResponse> updateCredentialPost(@NotNull String userId, @NotNull String credentialName, CredentialType credentialType,
                                                                         String username, String credentialValue, CredentialStatus credentialStatus) throws NextStepClientException {
        UpdateCredentialRequest request = new UpdateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        request.setCredentialStatus(credentialStatus);
        return postObjectImpl("/credential/update", new ObjectRequest<>(request), UpdateCredentialResponse.class);
    }

    /**
     * Validate a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @param username Username.
     * @param credentialValue Credential value.
     * @param validationMode Credential validation mode.
     * @return Validate credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<ValidateCredentialResponse> validateCredential(@NotNull String userId, @NotNull String credentialName, String username,
                                                                         String credentialValue, @NotNull CredentialValidationMode validationMode) throws NextStepClientException {
        ValidateCredentialRequest request = new ValidateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        request.setValidationMode(validationMode);
        return postObjectImpl("/credential/validate", new ObjectRequest<>(request), ValidateCredentialResponse.class);
    }

    /**
     * Reset a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @param credentialType Credential type.
     * @return Reset credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<ResetCredentialResponse> resetCredential(@NotNull String userId, @NotNull String credentialName, CredentialType credentialType) throws NextStepClientException {
        ResetCredentialRequest request = new ResetCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        return postObjectImpl("/credential/reset", new ObjectRequest<>(request), ResetCredentialResponse.class);
    }

    /**
     * Block a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @return Block credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<BlockCredentialResponse> blockCredential(@NotNull String userId, @NotNull String credentialName) throws NextStepClientException {
        BlockCredentialRequest request = new BlockCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        return postObjectImpl("/credential/block", new ObjectRequest<>(request), BlockCredentialResponse.class);
    }

    /**
     * Unblock a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @return Unblock credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UnblockCredentialResponse> unblockCredential(@NotNull String userId, @NotNull String credentialName) throws NextStepClientException {
        UnblockCredentialRequest request = new UnblockCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        return postObjectImpl("/credential/unblock", new ObjectRequest<>(request), UnblockCredentialResponse.class);
    }

    /**
     * Delete a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @return Delete credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteCredentialResponse> deleteCredential(@NotNull String userId, @NotNull String credentialName) throws NextStepClientException {
        DeleteCredentialRequest request = new DeleteCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        return postObjectImpl("/credential/delete", new ObjectRequest<>(request), DeleteCredentialResponse.class);
    }

    // Credential counter related methods

    /**
     * Update a credential counter.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @param authenticationResult Authentication result.
     * @return Update credential counter response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounter(@NotNull String userId, @NotNull String credentialName, @NotNull AuthenticationResult authenticationResult) throws NextStepClientException {
        UpdateCounterRequest request = new UpdateCounterRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setAuthenticationResult(authenticationResult);
        return postObjectImpl("/credential/counter/update", new ObjectRequest<>(request), UpdateCounterResponse.class);
    }

    /**
     * Reset all soft counters.
     *
     * @return Reset counters response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<ResetCountersResponse> resetAllCounters() throws NextStepClientException {
        ResetCountersRequest request = new ResetCountersRequest();
        return postObjectImpl("/credential/counter/reset-all", new ObjectRequest<>(request), ResetCountersResponse.class);
    }

    // OTP related methods

    /**
     * Create a OTP with operation.
     *
     * @param userId User ID.
     * @param otpName OTP name.
     * @param credentialName Credential name.
     * @param otpData OTP data.
     * @param operationId Operation ID.
     * @return Create OTP response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOtpResponse> createOtp(String userId, @NotNull String otpName, String credentialName, String otpData, String operationId) throws NextStepClientException {
        CreateOtpRequest request = new CreateOtpRequest();
        request.setUserId(userId);
        request.setOtpName(otpName);
        request.setCredentialName(credentialName);
        request.setOtpData(otpData);
        request.setOperationId(operationId);
        return postObjectImpl("/otp", new ObjectRequest<>(request), CreateOtpResponse.class);
    }

    /**
     * Create a OTP without operation.
     *
     * @param userId User ID.
     * @param otpName OTP name.
     * @param credentialName Credential name.
     * @param otpData OTP data.
     * @return Create OTP response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOtpResponse> createOtp(String userId, @NotNull String otpName, String credentialName, String otpData) throws NextStepClientException {
        CreateOtpRequest request = new CreateOtpRequest();
        request.setUserId(userId);
        request.setOtpName(otpName);
        request.setCredentialName(credentialName);
        request.setOtpData(otpData);
        return postObjectImpl("/otp", new ObjectRequest<>(request), CreateOtpResponse.class);
    }

    /**
     * Get OTP list.
     *
     * @param operationId Operation ID.
     * @param includeRemoved Whether removed OTPs should be included.
     * @return Get OTP list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpListResponse> getOtpList(@NotNull String operationId, boolean includeRemoved) throws NextStepClientException {
        GetOtpListRequest request = new GetOtpListRequest();
        request.setOperationId(operationId);
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/otp/list", new ObjectRequest<>(request), GetOtpListResponse.class);
    }

    /**
     * Get OTP detail.
     *
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @return Get OTP list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpDetailResponse> getOtpDetail(String otpId, String operationId) throws NextStepClientException {
        GetOtpDetailRequest request = new GetOtpDetailRequest();
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        return postObjectImpl("/otp/detail", new ObjectRequest<>(request), GetOtpDetailResponse.class);
    }

    /**
     * Delete an OTP.
     *
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @return Delete OTP response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteOtpResponse> deleteOtp(String otpId, String operationId) throws NextStepClientException {
        DeleteOtpRequest request = new DeleteOtpRequest();
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        return postObjectImpl("/otp/delete", new ObjectRequest<>(request), DeleteOtpResponse.class);
    }

    // Authentication related methods

    /**
     * Authenticate using OTP without operation.
     *
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<OtpAuthenticationResponse> authenticationWithOtp(String otpId, @NotNull String otpValue) throws NextStepClientException {
        OtpAuthenticationRequest request = new OtpAuthenticationRequest();
        request.setOtpId(otpId);
        request.setOtpValue(otpValue);
        return postObjectImpl("/auth/otp", new ObjectRequest<>(request), OtpAuthenticationResponse.class);
    }

    /**
     * Authenticate using OTP with operation.
     *
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @param otpValue OTP value.
     * @param updateOperation Whether operation should be updated.
     * @param authMethod Authentication method used for operation update.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<OtpAuthenticationResponse> authenticationWithOtp(String otpId, String operationId, @NotNull String otpValue,
                                                                           boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        OtpAuthenticationRequest request = new OtpAuthenticationRequest();
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        request.setOtpValue(otpValue);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return postObjectImpl("/auth/otp", new ObjectRequest<>(request), OtpAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential without operation.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticationWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue) throws NextStepClientException {
        CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        return postObjectImpl("/auth/credential", new ObjectRequest<>(request), CredentialAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential without operation with additional details.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param authenticationMode Authentication mode.
     * @param credentialPositionsToVerify Credential positions to verify.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticationWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                         CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify) throws NextStepClientException {
        CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        return postObjectImpl("/auth/credential", new ObjectRequest<>(request), CredentialAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential with operation.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param operationId Operation ID.
     * @param updateOperation Whether operation should be updated.
     * @param authMethod Authentication method used for operation update.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticationWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                         String operationId, boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setOperationId(operationId);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return postObjectImpl("/auth/credential", new ObjectRequest<>(request), CredentialAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential with operation and additional details.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param authenticationMode Authentication mode.
     * @param credentialPositionsToVerify Credential positions to verify.
     * @param operationId Operation ID.
     * @param updateOperation Whether operation should be updated.
     * @param authMethod Authentication method used for operation update.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticationWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                         CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify,
                                                                                         String operationId, boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        request.setOperationId(operationId);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return postObjectImpl("/auth/credential", new ObjectRequest<>(request), CredentialAuthenticationResponse.class);
    }


    /**
     * Authenticate using credential and OTP without operation.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticationCombined(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                 String otpId, @NotNull String otpValue) throws NextStepClientException {
        CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setOtpId(otpId);
        request.setOtpValue(otpValue);
        return postObjectImpl("/auth/combined", new ObjectRequest<>(request), CombinedAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential and OTP without operation and with additional details.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param authenticationMode Authentication mode.
     * @param credentialPositionsToVerify Credential positions to verify.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticationCombined(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                 CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify,
                                                                                 String otpId, @NotNull String otpValue) throws NextStepClientException {
        CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        request.setOtpId(otpId);
        request.setOtpValue(otpValue);
        return postObjectImpl("/auth/combined", new ObjectRequest<>(request), CombinedAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential and OTP with operation.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @param otpValue OTP value.
     * @param updateOperation Whether operation should be updated.
     * @param authMethod Authentication method used for operation update.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticationCombined(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                 String otpId, String operationId, @NotNull String otpValue,
                                                                                 boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        request.setOtpValue(otpValue);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return postObjectImpl("/auth/combined", new ObjectRequest<>(request), CombinedAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential and OTP with operation and with additional details.
     *
     * @param credentialName Credential name.
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param authenticationMode Authentication mode.
     * @param credentialPositionsToVerify Credential positions to verify.
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @param otpValue OTP value.
     * @param updateOperation Whether operation should be updated.
     * @param authMethod Authentication method used for operation update.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticationCombined(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                 CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify,
                                                                                 String otpId, String operationId, @NotNull String otpValue,
                                                                                 boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        request.setOtpValue(otpValue);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return postObjectImpl("/auth/combined", new ObjectRequest<>(request), CombinedAuthenticationResponse.class);
    }

    // Audit log related methods

    /**
     * Create an audit log.
     *
     * @return Create audit log response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateAuditResponse> createAudit(@NotNull String action, String data) throws NextStepClientException {
        CreateAuditRequest request = new CreateAuditRequest();
        request.setAction(action);
        request.setData(data);
        return postObjectImpl("/audit", new ObjectRequest<>(request), CreateAuditResponse.class);
    }

    /**
     * Get audit log list.
     *
     * @param startDate Audit log interval start date.
     * @param endDate Audit log interval end date.
     * @return Delete audit log response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetAuditListResponse> getAuditList(@NotNull Date startDate, @NotNull Date endDate) throws NextStepClientException {
        GetAuditListRequest request = new GetAuditListRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return postObjectImpl("/audit/list", new ObjectRequest<>(request), GetAuditListResponse.class);
    }

    // Generic HTTP client methods

    /**
     * Prepare a generic POST response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @param typeReference Type reference.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> T postImpl(String path, Object request, ParameterizedTypeReference<T> typeReference) throws NextStepClientException {
        try {
            return restClient.post(path, request, typeReference).getBody();
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new NextStepClientException(ex, new NextStepError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Prepare POST object response. Uses default {@link Response} type reference for response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private Response postObjectImpl(String path, ObjectRequest<?> request) throws NextStepClientException {
        try {
            return restClient.postObject(path, request);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new NextStepClientException(ex, new NextStepError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Prepare POST object response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> postObjectImpl(String path, ObjectRequest<?> request, Class<T> responseType) throws NextStepClientException {
        try {
            return restClient.postObject(path, request, responseType);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new NextStepClientException(ex, new NextStepError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Prepare PUT object response. Uses default {@link Response} type reference for response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private Response putObjectImpl(String path, ObjectRequest<?> request) throws NextStepClientException {
        try {
            return restClient.putObject(path, request);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new NextStepClientException(ex, new NextStepError(resolveErrorCode(ex), "HTTP PUT request failed."));
        }
    }

    /**
     * Prepare PUT object response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> putObjectImpl(String path, ObjectRequest<?> request, Class<T> responseType) throws NextStepClientException {
        try {
            return restClient.putObject(path, request, responseType);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new NextStepClientException(ex, new NextStepError(resolveErrorCode(ex), "HTTP PUT request failed."));
        }
    }

    /**
     * Resolve error code based on HTTP status code from REST client exception.
     */
    private String resolveErrorCode(RestClientException ex) {
        if (ex.getStatusCode() == null) {
            // REST client errors, response not received
            return NextStepError.Code.ERROR_GENERIC;
        }
        if (ex.getStatusCode().is4xxClientError()) {
            // Errors caused by invalid Next Step client requests
            return NextStepError.Code.NEXT_STEP_CLIENT_ERROR;
        }
        if (ex.getStatusCode().is5xxServerError()) {
            // Internal errors in Next Step server
            return NextStepError.Code.REMOTE_ERROR;
        }
        // Other errors during communication
        return NextStepError.Code.COMMUNICATION_ERROR;
    }

}
