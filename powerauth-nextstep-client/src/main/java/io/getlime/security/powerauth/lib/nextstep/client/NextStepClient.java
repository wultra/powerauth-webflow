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

package io.getlime.security.powerauth.lib.nextstep.client;

import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.core.rest.client.base.RestClientException;
import com.wultra.core.rest.model.base.entity.Error;
import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ErrorResponse;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.PAAuthenticationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
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
            NextStepClientException ex2 = new NextStepClientException("Rest client initialization failed.", ex);
            logError(ex2);
            throw ex2;
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
            NextStepClientException ex2 = new NextStepClientException("Rest client initialization failed.", ex);
            logError(ex2);
            throw ex2;
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
     * @see #createOperation(CreateOperationRequest)
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
     * @param applicationContext Context of application requesting the OAuth 2.1 consent.
     * @return A Response with CreateOperationResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createOperation(CreateOperationRequest)
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, String operationId, @NotNull String operationData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        return createOperation(operationName, operationId, operationData, null, null, null, params, applicationContext);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationData Operation data.
     * @param formData Operation form data, such as title, message and displayable attributes.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.1 consent.
     * @return A Response with CreateOperationResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createOperation(CreateOperationRequest)
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, @NotNull String operationData, OperationFormData formData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
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
     * @param applicationContext Context of application requesting the OAuth 2.1 consent.
     * @return A Response with CreateOperationResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createOperation(CreateOperationRequest)
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, String operationId, @NotNull String operationData, String organizationId, String externalTransactionId, OperationFormData formData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        final CreateOperationRequest request = new CreateOperationRequest();
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
        return createOperation(request);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationData Operation data.
     * @param organizationId Organization ID.
     * @param formData Operation form data, such as title, message and displayable attributes.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.1 consent.
     * @return A Response with CreateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createOperation(CreateOperationRequest)
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull String operationName, @NotNull String operationData, OperationFormData formData, String organizationId, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepClientException {
        return createOperation(operationName, null, operationData, organizationId, null, formData, params, applicationContext);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param request Create operation request.
     * @return A Response with CreateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(@NotNull final CreateOperationRequest request) throws NextStepClientException {
        return postObjectImpl("/operation", new ObjectRequest<>(request), CreateOperationResponse.class);
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
     * @param applicationContext Context of application requesting the OAuth 2.1 consent.
     * @return A Response with UpdateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateOperation(UpdateOperationRequest)
     */
    public ObjectResponse<UpdateOperationResponse> updateOperation(@NotNull String operationId, String userId, String organizationId, @NotNull AuthMethod authMethod, List<AuthInstrument> authInstruments, @NotNull AuthStepResult authStepResult, String authStepResultDescription, List<KeyValueParameter> params, ApplicationContext applicationContext, PAAuthenticationContext authenticationContext) throws NextStepClientException {
        final UpdateOperationRequest request = new UpdateOperationRequest();
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
        request.setAuthenticationContext(authenticationContext);
        return updateOperation(request);
    }

    /**
     * Calls the update operation endpoint via PUT method to update an existing operation.
     *
     * @param request Update operation request.
     * @return A Response with UpdateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOperationResponse> updateOperation(@NotNull final UpdateOperationRequest request) throws NextStepClientException {
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
     * @param applicationContext Context of application requesting the OAuth 2.1 consent.
     * @return A Response with UpdateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateOperationPost(UpdateOperationRequest)
     */
    public ObjectResponse<UpdateOperationResponse> updateOperationPost(@NotNull String operationId, String userId, String organizationId, @NotNull AuthMethod authMethod, List<AuthInstrument> authInstruments, @NotNull AuthStepResult authStepResult, String authStepResultDescription, List<KeyValueParameter> params, ApplicationContext applicationContext, PAAuthenticationContext authenticationContext) throws NextStepClientException {
        final UpdateOperationRequest request = new UpdateOperationRequest();
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
        request.setAuthenticationContext(authenticationContext);
        return updateOperationPost(request);
    }

    /**
     * Calls the update operation endpoint via POST method to update an existing operation.
     *
     * @param request Update operation request.
     * @return A Response with UpdateOperationResponse object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateOperationResponse> updateOperationPost(@NotNull final UpdateOperationRequest request) throws NextStepClientException {
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
     * @see #updateOperationUser(UpdateOperationUserRequest)
     */
    public Response updateOperationUser(@NotNull String operationId, @NotNull String userId, String organizationId, UserAccountStatus accountStatus) throws NextStepClientException {
        final UpdateOperationUserRequest request = new UpdateOperationUserRequest();
        request.setOperationId(operationId);
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setAccountStatus(accountStatus);
        return updateOperationUser(request);
    }

    /**
     * Update user, organization and account status for an operation via PUT method.
     *
     * @param request Update operation user request.
     * @return Response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationUser(@NotNull final UpdateOperationUserRequest request) throws NextStepClientException {
        return putObjectImpl("/operation/user", new ObjectRequest<>(request));
    }

    /**
     * Update user, organization and account status for an operation via POST method.
     *
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus User account status.
     * @return Response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateOperationUserPost(UpdateOperationUserRequest)
     */
    public Response updateOperationUserPost(@NotNull String operationId, @NotNull String userId, String organizationId, UserAccountStatus accountStatus) throws NextStepClientException {
        final UpdateOperationUserRequest request = new UpdateOperationUserRequest();
        request.setOperationId(operationId);
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setAccountStatus(accountStatus);
        return updateOperationUserPost(request);
    }

    /**
     * Update user, organization and account status for an operation via POST method.
     *
     * @param request Update operation user request.
     * @return Response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationUserPost(@NotNull final UpdateOperationUserRequest request) throws NextStepClientException {
        return postObjectImpl("/operation/user/update", new ObjectRequest<>(request));
    }

    /**
     * Update operation form data via PUT method.
     *
     * @param operationId Operation ID.
     * @param formData Form data.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateOperationFormData(UpdateFormDataRequest)
     */
    public Response updateOperationFormData(@NotNull String operationId, @NotNull OperationFormData formData) throws NextStepClientException {
        final UpdateFormDataRequest request = new UpdateFormDataRequest();
        request.setOperationId(operationId);
        request.setFormData(formData);
        return updateOperationFormData(request);
    }

    /**
     * Update operation form data via PUT method.
     *
     * @param request Update form data request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationFormData(@NotNull final UpdateFormDataRequest request) throws NextStepClientException {
        return putObjectImpl("/operation/formData", new ObjectRequest<>(request));
    }

    /**
     * Update operation form data via POST method.
     *
     * @param operationId Operation ID.
     * @param formData Form data.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateOperationFormDataPost(UpdateFormDataRequest)
     */
    public Response updateOperationFormDataPost(@NotNull String operationId, @NotNull OperationFormData formData) throws NextStepClientException {
        final UpdateFormDataRequest request = new UpdateFormDataRequest();
        request.setOperationId(operationId);
        request.setFormData(formData);
        return updateOperationFormDataPost(request);
    }
    
    /**
     * Update operation form data via POST method.
     *
     * @param request Update form data request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateOperationFormDataPost(@NotNull final UpdateFormDataRequest request) throws NextStepClientException {
        return postObjectImpl("/operation/formData/update", new ObjectRequest<>(request));
    }

    /**
     * Update chosen authentication method for current operation step via PUT method.
     *
     * @param operationId Operation ID.
     * @param chosenAuthMethod Chosen authentication method.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateChosenAuthMethod(UpdateChosenAuthMethodRequest)
     */
    public Response updateChosenAuthMethod(@NotNull String operationId, @NotNull AuthMethod chosenAuthMethod) throws NextStepClientException {
        final UpdateChosenAuthMethodRequest request = new UpdateChosenAuthMethodRequest();
        request.setOperationId(operationId);
        request.setChosenAuthMethod(chosenAuthMethod);
        return updateChosenAuthMethod(request);
    }

    /**
     * Update chosen authentication method for current operation step via PUT method.
     *
     * @param request Update chosen auth method request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateChosenAuthMethod(@NotNull final UpdateChosenAuthMethodRequest request) throws NextStepClientException {
        return putObjectImpl("/operation/chosenAuthMethod", new ObjectRequest<>(request));
    }

    /**
     * Update chosen authentication method for current operation step via POST method.
     *
     * @param operationId Operation ID.
     * @param chosenAuthMethod Chosen authentication method.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateChosenAuthMethodPost(UpdateChosenAuthMethodRequest)
     */
    public Response updateChosenAuthMethodPost(@NotNull String operationId, @NotNull AuthMethod chosenAuthMethod) throws NextStepClientException {
        final UpdateChosenAuthMethodRequest request = new UpdateChosenAuthMethodRequest();
        request.setOperationId(operationId);
        request.setChosenAuthMethod(chosenAuthMethod);
        return updateChosenAuthMethodPost(request);
    }

    /**
     * Update chosen authentication method for current operation step via POST method.
     *
     * @param request Update chosen auth method request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateChosenAuthMethodPost(@NotNull final UpdateChosenAuthMethodRequest request) throws NextStepClientException {
        return postObjectImpl("/operation/chosenAuthMethod/update", new ObjectRequest<>(request));
    }

    /**
     * Update application context for current operation step via PUT method.
     *
     * @param operationId Operation ID.
     * @param applicationContext Application context.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateApplicationContext(UpdateApplicationContextRequest)
     */
    public Response updateApplicationContext(@NotNull String operationId, @NotNull ApplicationContext applicationContext) throws NextStepClientException {
        final UpdateApplicationContextRequest request = new UpdateApplicationContextRequest();
        request.setOperationId(operationId);
        request.setApplicationContext(applicationContext);
        return updateApplicationContext(request);
    }

    /**
     * Update application context for current operation step via PUT method.
     *
     * @param request Update application context request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateApplicationContext(@NotNull final UpdateApplicationContextRequest request) throws NextStepClientException {
        return putObjectImpl("/operation/application", new ObjectRequest<>(request));
    }

    /**
     * Update application context for current operation step via POST method.
     *
     * @param operationId Operation ID.
     * @param applicationContext Application context.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateApplicationContextPost(UpdateApplicationContextRequest)
     */
    public Response updateApplicationContextPost(@NotNull String operationId, @NotNull ApplicationContext applicationContext) throws NextStepClientException {
        final UpdateApplicationContextRequest request = new UpdateApplicationContextRequest();
        request.setOperationId(operationId);
        request.setApplicationContext(applicationContext);
        return updateApplicationContextPost(request);
    }

    /**
     * Update application context for current operation step via POST method.
     *
     * @param request Update application context request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateApplicationContextPost(@NotNull final UpdateApplicationContextRequest request) throws NextStepClientException {
        return postObjectImpl("/operation/application/update", new ObjectRequest<>(request));
    }

    /**
     * Update mobile token status for current operation step via PUT method.
     *
     * @param operationId Operation ID.
     * @param mobileTokenActive Whether mobile token is active.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateMobileToken(UpdateMobileTokenRequest)
     */
    public Response updateMobileToken(@NotNull String operationId, boolean mobileTokenActive) throws NextStepClientException {
        final UpdateMobileTokenRequest request = new UpdateMobileTokenRequest();
        request.setOperationId(operationId);
        request.setMobileTokenActive(mobileTokenActive);
        return updateMobileToken(request);
    }

    /**
     * Update mobile token status for current operation step via PUT method.
     *
     * @param request Update mobile token request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateMobileToken(@NotNull final UpdateMobileTokenRequest request) throws NextStepClientException {
        return putObjectImpl("/operation/mobileToken/status", new ObjectRequest<>(request));
    }

    /**
     * Update mobile token status for current operation step via POST method.
     *
     * @param operationId Operation ID.
     * @param mobileTokenActive Whether mobile token is active.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateMobileTokenPost(UpdateMobileTokenRequest)
     */
    public Response updateMobileTokenPost(@NotNull String operationId, boolean mobileTokenActive) throws NextStepClientException {
        final UpdateMobileTokenRequest request = new UpdateMobileTokenRequest();
        request.setOperationId(operationId);
        request.setMobileTokenActive(mobileTokenActive);
        return updateMobileTokenPost(request);
    }

    /**
     * Update mobile token status for current operation step via POST method.
     *
     * @param request Update mobile token request.
     * @return Object response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response updateMobileTokenPost(@NotNull final UpdateMobileTokenRequest request) throws NextStepClientException {
        return postObjectImpl("/operation/mobileToken/status/update", new ObjectRequest<>(request));
    }

    /**
     * Get mobile token configuration using GET method.
     *
     * @param userId User ID.
     * @param operationName Operation name.
     * @param authMethod Authentication method.
     * @return Mobile token configuration.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetMobileTokenConfigResponse> getMobileTokenConfig(@NotNull String userId, @NotNull String operationName, @NotNull AuthMethod authMethod) throws NextStepClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        params.put("operationName", Collections.singletonList(operationName));
        if (authMethod != null) {
            params.put("authMethod", Collections.singletonList(authMethod.toString()));
        }
        return getObjectImpl("/operation/mobileToken/config/detail", params, GetMobileTokenConfigResponse.class);
    }

    /**
     * Get mobile token configuration using POST method.
     *
     * @param userId User ID.
     * @param operationName Operation name.
     * @param authMethod Authentication method.
     * @return Mobile token configuration.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #getMobileTokenConfigPost(GetMobileTokenConfigRequest)
     */
    public ObjectResponse<GetMobileTokenConfigResponse> getMobileTokenConfigPost(@NotNull String userId, @NotNull String operationName, @NotNull AuthMethod authMethod) throws NextStepClientException {
        final GetMobileTokenConfigRequest request = new GetMobileTokenConfigRequest();
        request.setUserId(userId);
        request.setOperationName(operationName);
        request.setAuthMethod(authMethod);
        return getMobileTokenConfigPost(request);
    }

    /**
     * Get mobile token configuration using POST method.
     *
     * @param request Get mobile token config request.
     * @return Mobile token configuration.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetMobileTokenConfigResponse> getMobileTokenConfigPost(@NotNull final GetMobileTokenConfigRequest request) throws NextStepClientException {
        return postObjectImpl("/operation/mobileToken/config/detail", new ObjectRequest<>(request), GetMobileTokenConfigResponse.class);
    }

    /**
     * Calls the operation details endpoint via GET method to get operation details.
     *
     * @param operationId Operation ID.
     * @return A Response with {@link GetOperationDetailResponse} object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOperationDetailResponse> getOperationDetail(@NotNull String operationId) throws NextStepClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("operationId", Collections.singletonList(operationId));
        return getObjectImpl("/operation/detail", params, GetOperationDetailResponse.class);
    }

    /**
     * Calls the operation details endpoint via POST method to get operation details.
     *
     * @param operationId Operation ID.
     * @return A Response with {@link GetOperationDetailResponse} object.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOperationDetailResponse> getOperationDetailPost(@NotNull String operationId) throws NextStepClientException {
        final GetOperationDetailRequest request = new GetOperationDetailRequest();
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
        final LookupOperationsByExternalIdRequest request = new LookupOperationsByExternalIdRequest();
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
     * @see #createAfsAction(CreateAfsActionRequest)
     */
    public Response createAfsAction(@NotNull String operationId, @NotNull String afsAction, int stepIndex, String requestAfsExtras, String afsLabel,
                                    boolean afsResponseApplied, String responseAfsExtras) throws NextStepClientException {
        final CreateAfsActionRequest request = new CreateAfsActionRequest();
        request.setOperationId(operationId);
        request.setAfsAction(afsAction);
        request.setStepIndex(stepIndex);
        request.setRequestAfsExtras(requestAfsExtras);
        request.setAfsLabel(afsLabel);
        request.setAfsResponseApplied(afsResponseApplied);
        request.setResponseAfsExtras(responseAfsExtras);
        request.setTimestampCreated(new Date());
        return createAfsAction(request);
    }

    /**
     * Create an AFS action in Next Step and log its request and response parameters.
     *
     * @param request Create AFS action request.
     * @return Response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public Response createAfsAction(@NotNull final CreateAfsActionRequest request) throws NextStepClientException {
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("operationName", Collections.singletonList(operationName));
        return getObjectImpl("/operation/config/detail", params, GetOperationConfigDetailResponse.class);
    }

    /**
     * Get operation configuration using POST method.
     *
     * @param operationName Operation name.
     * @return Operation configuration.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOperationConfigDetailResponse> getOperationConfigDetailPost(@NotNull String operationName) throws NextStepClientException {
        final GetOperationConfigDetailRequest request = new GetOperationConfigDetailRequest();
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
        return getObjectImpl("/operation/config",  GetOperationConfigListResponse.class);
    }

    /**
     * Get all operation configurations using POST method.
     *
     * @return All operation configurations.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOperationConfigListResponse> getOperationConfigListPost() throws NextStepClientException {
        final GetOperationConfigListRequest request = new GetOperationConfigListRequest();
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
        final DeleteOperationConfigRequest request = new DeleteOperationConfigRequest();
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        params.put("mobileTokenOnly", Collections.singletonList(String.valueOf(mobileTokenOnly)));
        return getImpl("/user/operation", params, new ParameterizedTypeReference<>() {});
    }

    /**
     * Get list of pending operations for given user and authentication method using POST method.
     *
     * @param userId User ID.
     * @param mobileTokenOnly Whether pending operation list should be filtered for only next step with mobile token support.
     * @return A Response with list of {@link GetOperationDetailResponse}.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperationsPost(@NotNull String userId, boolean mobileTokenOnly) throws NextStepClientException {
        final GetPendingOperationsRequest request = new GetPendingOperationsRequest();
        request.setUserId(userId);
        request.setMobileTokenOnly(mobileTokenOnly);
        return postImpl("/user/operation/list", new ObjectRequest<>(request), new ParameterizedTypeReference<>() {});
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
     * @see #createOrganization(CreateOrganizationRequest)
     */
    public ObjectResponse<CreateOrganizationResponse> createOrganization(@NotNull String organizationId, String displayNameKey,
                                                                         boolean isDefault, @NotNull Integer orderNumber) throws NextStepClientException {
        final CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setOrganizationId(organizationId);
        request.setDisplayNameKey(displayNameKey);
        request.setDefault(isDefault);
        request.setOrderNumber(orderNumber);
        return createOrganization(request);
    }

    /**
     * Create an organization.
     *
     * @param request Create organization request.
     * @return Create organization response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOrganizationResponse> createOrganization(@NotNull final CreateOrganizationRequest request) throws NextStepClientException {
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("organizationId", Collections.singletonList(organizationId));
        return getObjectImpl("/organization/detail", params, GetOrganizationDetailResponse.class);
    }

    /**
     * Get organization detail using POST method.
     *
     * @param organizationId Organization ID.
     * @return Organization detail.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOrganizationDetailResponse> getOrganizationDetailPost(@NotNull String organizationId) throws NextStepClientException {
        final GetOrganizationDetailRequest request = new GetOrganizationDetailRequest();
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
        return getObjectImpl("/organization", GetOrganizationListResponse.class);
    }

    /**
     * Get all organizations using POST method.
     *
     * @return All organizations.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOrganizationListResponse> getOrganizationListPost() throws NextStepClientException {
        final GetOrganizationListRequest request = new GetOrganizationListRequest();
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
        final DeleteOrganizationRequest request = new DeleteOrganizationRequest();
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
        final DeleteStepDefinitionRequest request = new DeleteStepDefinitionRequest();
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
        return getObjectImpl("/auth-method", GetAuthMethodsResponse.class);
    }


    /**
     * Get all authentication methods supported by Next Step server using POST method.
     *
     * @return List of authentication methods wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetAuthMethodsResponse> getAuthMethodListPost() throws NextStepClientException {
        final GetAuthMethodListRequest request = new GetAuthMethodListRequest();
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
        final DeleteAuthMethodRequest request = new DeleteAuthMethodRequest();
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
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsForUser(String userId) throws NextStepClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (userId != null) {
            params.put("userId", Collections.singletonList(userId));
        }
        return getObjectImpl("/user/auth-method", params, GetUserAuthMethodsResponse.class);
    }

    /**
     * Get all globally enabled authentication methods for given user using POST method. Do not perform checks of individual
     * authentication methods whether they are currently available for authentication.
     *
     * @param userId User ID.
     * @return List of globally enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsForUserPost(String userId) throws NextStepClientException {
        final GetUserAuthMethodsRequest request = new GetUserAuthMethodsRequest();
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        params.put("operationName", Collections.singletonList(operationName));
        return getObjectImpl("/user/auth-method/enabled", params, GetEnabledMethodListResponse.class);
    }

    /**
     * Get all currently enabled authentication methods for given user and operation name using POST method. Perform an actual check of each
     * authentication method at the current moment to make they are currently available for authentication. Filter the
     * authentication method list by the steps in given operation.
     *
     * @param userId User ID.
     * @param operationName Operation name.
     * @return List of currently enabled and available authentication methods for given user wrapped in GetEnabledMethodListResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetEnabledMethodListResponse> getAuthMethodsEnabledForUserPost(@NotNull String userId, @NotNull String operationName) throws NextStepClientException {
        final GetEnabledMethodListRequest request = new GetEnabledMethodListRequest();
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
     * @see #enableAuthMethodForUser(UpdateAuthMethodRequest)
     */
    public ObjectResponse<GetAuthMethodsResponse> enableAuthMethodForUser(@NotNull String userId, @NotNull AuthMethod authMethod, Map<String, String> config) throws NextStepClientException {
        final UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
        request.setUserId(userId);
        request.setAuthMethod(authMethod);
        if (config != null) {
            request.getConfig().putAll(config);
        }
        return enableAuthMethodForUser(request);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param request Update auth method request.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetAuthMethodsResponse> enableAuthMethodForUser(@NotNull final UpdateAuthMethodRequest request) throws NextStepClientException {
        return postObjectImpl("/user/auth-method", new ObjectRequest<>(request), GetAuthMethodsResponse.class);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @param config Authentication method configuration.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #disableAuthMethodForUser(UpdateAuthMethodRequest)
     */
    public ObjectResponse<GetAuthMethodsResponse> disableAuthMethodForUser(@NotNull String userId, @NotNull AuthMethod authMethod, Map<String, String> config) throws NextStepClientException {
        final UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
        request.setUserId(userId);
        request.setAuthMethod(authMethod);
        if (config != null) {
            request.getConfig().putAll(config);
        }
        return disableAuthMethodForUser(request);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param request Update auth method request.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetAuthMethodsResponse> disableAuthMethodForUser(@NotNull final UpdateAuthMethodRequest request) throws NextStepClientException {
        return postObjectImpl("/user/auth-method/delete", new ObjectRequest<>(request), GetAuthMethodsResponse.class);
    }

    // Next Step application related methods

    /**
     * Create a Next Step application.
     *
     * @param applicationName Application name.
     * @param description Application description.
     * @return Create application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createApplication(CreateApplicationRequest)
     */
    public ObjectResponse<CreateApplicationResponse> createApplication(@NotNull String applicationName, String description) throws NextStepClientException {
        final CreateApplicationRequest request = new CreateApplicationRequest();
        request.setApplicationName(applicationName);
        request.setDescription(description);
        return createApplication(request);
    }

    /**
     * Create a Next Step application.
     *
     * @param request Createa application request.
     * @return Create application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateApplicationResponse> createApplication(@NotNull final CreateApplicationRequest request) throws NextStepClientException {
        return postObjectImpl("/application", new ObjectRequest<>(request), CreateApplicationResponse.class);
    }

    /**
     * Update a Next Step application via PUT method.
     *
     * @param applicationName Application name.
     * @param description Application description.
     * @param status Application status.
     * @return Update application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateApplication(UpdateApplicationRequest)
     */
    public ObjectResponse<UpdateApplicationResponse> updateApplication(@NotNull String applicationName, String description, ApplicationStatus status) throws NextStepClientException {
        final UpdateApplicationRequest request = new UpdateApplicationRequest();
        request.setApplicationName(applicationName);
        request.setDescription(description);
        request.setApplicationStatus(status);
        return updateApplication(request);
    }

    /**
     * Update a Next Step application via PUT method.
     *
     * @param request Update application request.
     * @return Update application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateApplicationResponse> updateApplication(@NotNull final UpdateApplicationRequest request) throws NextStepClientException {
        return putObjectImpl("/application", new ObjectRequest<>(request), UpdateApplicationResponse.class);
    }

    /**
     * Update a Next Step application via POST method.
     *
     * @param applicationName Application name.
     * @param description Application description.
     * @param status Application status.
     * @return Update application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateApplicationPost(UpdateApplicationRequest)
     */
    public ObjectResponse<UpdateApplicationResponse> updateApplicationPost(@NotNull String applicationName, String description, ApplicationStatus status) throws NextStepClientException {
        final UpdateApplicationRequest request = new UpdateApplicationRequest();
        request.setApplicationName(applicationName);
        request.setDescription(description);
        request.setApplicationStatus(status);
        return updateApplicationPost(request);
    }

    /**
     * Update a Next Step application via POST method.
     *
     * @param request Update application request.
     * @return Update application response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateApplicationResponse> updateApplicationPost(@NotNull final UpdateApplicationRequest request) throws NextStepClientException {
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/application", params, GetApplicationListResponse.class);
    }

    /**
     * Get Next Step application list using POST method.
     *
     * @param includeRemoved Whether removed applications should be included in the application list.
     * @return Get application list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetApplicationListResponse> getApplicationListPost(boolean includeRemoved) throws NextStepClientException {
        final GetApplicationListRequest request = new GetApplicationListRequest();
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
        final DeleteApplicationRequest request = new DeleteApplicationRequest();
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
     * @see #createRole(CreateRoleRequest)
     */
    public ObjectResponse<CreateRoleResponse> createRole(@NotNull String roleName, String description) throws NextStepClientException {
        final CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName(roleName);
        request.setDescription(description);
        return createRole(request);
    }

    /**
     * Create a user role.
     *
     * @param request Create role request.
     * @return Create user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateRoleResponse> createRole(@NotNull final CreateRoleRequest request) throws NextStepClientException {
        return postObjectImpl("/role", new ObjectRequest<>(request), CreateRoleResponse.class);
    }

    /**
     * Get the list of user roles.
     *
     * @return Get user role list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetRoleListResponse> getRoleList() throws NextStepClientException {
        return getObjectImpl("/role", GetRoleListResponse.class);
    }

    /**
     * Get the list of user roles using POST method.
     *
     * @return Get user role list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetRoleListResponse> getRoleListPost() throws NextStepClientException {
        final GetRoleListRequest request = new GetRoleListRequest();
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
        final DeleteRoleRequest request = new DeleteRoleRequest();
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
     * @see #createHashConfig(CreateHashConfigRequest)
     */
    public ObjectResponse<CreateHashConfigResponse> createHashConfig(@NotNull String hashConfigName, @NotNull HashAlgorithm algorithm, Map<String, String> parameters) throws NextStepClientException {
        final CreateHashConfigRequest request = new CreateHashConfigRequest();
        request.setHashConfigName(hashConfigName);
        request.setAlgorithm(algorithm);
        if (parameters != null) {
            request.getParameters().putAll(parameters);
        }
        return createHashConfig(request);
    }

    /**
     * Create a hashing configuration.
     *
     * @param request Create hash config request.
     * @return Create hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateHashConfigResponse> createHashConfig(@NotNull final CreateHashConfigRequest request) throws NextStepClientException {
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
     * @see #updateHashConfig(UpdateHashConfigRequest)
     */
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfig(@NotNull String hashConfigName, @NotNull HashAlgorithm algorithm, Map<String, String> parameters, HashConfigStatus status) throws NextStepClientException {
        final UpdateHashConfigRequest request = new UpdateHashConfigRequest();
        request.setHashConfigName(hashConfigName);
        request.setAlgorithm(algorithm);
        if (parameters != null) {
            request.getParameters().putAll(parameters);
        }
        request.setHashConfigStatus(status);
        return updateHashConfig(request);
    }

    /**
     * Update a hashing configuration via PUT method.
     *
     * @param request Update hash config request.
     * @return Update hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfig(@NotNull final UpdateHashConfigRequest request) throws NextStepClientException {
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
     * @see #updateHashConfigPost(UpdateHashConfigRequest)
     */
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfigPost(@NotNull String hashConfigName, @NotNull HashAlgorithm algorithm, Map<String, String> parameters, HashConfigStatus status) throws NextStepClientException {
        final UpdateHashConfigRequest request = new UpdateHashConfigRequest();
        request.setHashConfigName(hashConfigName);
        request.setAlgorithm(algorithm);
        if (parameters != null) {
            request.getParameters().putAll(parameters);
        }
        request.setHashConfigStatus(status);
        return updateHashConfigPost(request);
    }

    /**
     * Update a hashing configuration via POST method.
     *
     * @param request Update hash config request.
     * @return Update hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfigPost(@NotNull final UpdateHashConfigRequest request) throws NextStepClientException {
        return postObjectImpl("/hashconfig/update", new ObjectRequest<>(request), UpdateHashConfigResponse.class);
    }

    /**
     * Get list of hashing configurations.
     *
     * @param includeRemoved Whether removed hashing configurations should be included.
     * @return Get hashing configuration list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetHashConfigListResponse> getHashConfigList(boolean includeRemoved) throws NextStepClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/hashconfig", params, GetHashConfigListResponse.class);
    }

    /**
     * Get list of hashing configurations using POST method.
     *
     * @param includeRemoved Whether removed hashing configurations should be included.
     * @return Get hashing configuration list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetHashConfigListResponse> getHashConfigListPost(boolean includeRemoved) throws NextStepClientException {
        final GetHashConfigListRequest request = new GetHashConfigListRequest();
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/hashconfig/list", new ObjectRequest<>(request), GetHashConfigListResponse.class);
    }

    /**
     * Delete a hashing configurations.
     *
     * @param hashConfigName Hashing configuration name.
     * @return Delete hashing configuration response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteHashConfigResponse> deleteHashConfig(@NotNull String hashConfigName) throws NextStepClientException {
        final DeleteHashConfigRequest request = new DeleteHashConfigRequest();
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
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicy(@NotNull UpdateCredentialPolicyRequest request) throws NextStepClientException {
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/credential/policy", params, GetCredentialPolicyListResponse.class);
    }

    /**
     * Get credential policy list using POST method.
     *
     * @param includeRemoved Whether removed credential policies should be included.
     * @return Get credential policy list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetCredentialPolicyListResponse> getCredentialPolicyListPost(boolean includeRemoved) throws NextStepClientException {
        final GetCredentialPolicyListRequest request = new GetCredentialPolicyListRequest();
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
        final DeleteCredentialPolicyRequest request = new DeleteCredentialPolicyRequest();
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/credential/definition", params, GetCredentialDefinitionListResponse.class);
    }

    /**
     * Get credential definition list using POST method.
     *
     * @param includeRemoved Whether removed credential definitions should be included.
     * @return Get credential definition list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetCredentialDefinitionListResponse> getCredentialDefinitionListPost(boolean includeRemoved) throws NextStepClientException {
        final GetCredentialDefinitionListRequest request = new GetCredentialDefinitionListRequest();
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
        final DeleteCredentialDefinitionRequest request = new DeleteCredentialDefinitionRequest();
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/otp/policy", params, GetOtpPolicyListResponse.class);
    }

    /**
     * Get OTP policy list using POST method.
     *
     * @param includeRemoved Whether removed OTP policies should be included.
     * @return Get OTP policy list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpPolicyListResponse> getOtpPolicyListPost(boolean includeRemoved) throws NextStepClientException {
        final GetOtpPolicyListRequest request = new GetOtpPolicyListRequest();
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
        final DeleteOtpPolicyRequest request = new DeleteOtpPolicyRequest();
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/otp/definition", params, GetOtpDefinitionListResponse.class);
    }

    /**
     * Get OTP definition list using POST method.
     *
     * @param includeRemoved Whether removed OTP definitions should be included.
     * @return Get OTP definition list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpDefinitionListResponse> getOtpDefinitionListPost(boolean includeRemoved) throws NextStepClientException {
        final GetOtpDefinitionListRequest request = new GetOtpDefinitionListRequest();
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
        final DeleteOtpDefinitionRequest request = new DeleteOtpDefinitionRequest();
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
     * @see #updateUsers(UpdateUsersRequest)
     */
    public ObjectResponse<UpdateUsersResponse> updateUsers(@NotNull List<String> userIds, @NotNull UserIdentityStatus status) throws NextStepClientException {
        final UpdateUsersRequest request = new UpdateUsersRequest();
        request.getUserIds().addAll(userIds);
        request.setUserIdentityStatus(status);
        return updateUsers(request);
    }

    /**
     * Update statuses of multiple user identities.
     *
     * @param request Update users request.
     * @return Update users response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUsersResponse> updateUsers(@NotNull final UpdateUsersRequest request) throws NextStepClientException {
        return postObjectImpl("/user/update/multi", new ObjectRequest<>(request), UpdateUsersResponse.class);
    }

    /**
     * Get user detail.
     *
     * @param userId User ID.
     * @param includeRemoved Whether removed user identities should be returned.
     * @return Get user detail response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserDetailResponse> getUserDetail(@NotNull String userId, boolean includeRemoved) throws NextStepClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/user/detail", params, GetUserDetailResponse.class);
    }

    /**
     * Get user detail using POST method.
     *
     * @param userId User ID.
     * @param includeRemoved Whether removed user identities should be returned.
     * @return Get user detail response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserDetailResponse> getUserDetailPost(@NotNull String userId, boolean includeRemoved) throws NextStepClientException {
        final GetUserDetailRequest request = new GetUserDetailRequest();
        request.setUserId(userId);
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/user/detail", new ObjectRequest<>(request), GetUserDetailResponse.class);
    }

    /**
     * Lookup user identities.
     *
     * @param request Lookup users request.
     * @return Lookup users response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<LookupUsersResponse> lookupUsers(@NotNull LookupUsersRequest request) throws NextStepClientException {
        return postObjectImpl("/user/lookup", new ObjectRequest<>(request), LookupUsersResponse.class);
    }

    /**
     * Lookup a single user identity without operation.
     *
     * @param username Username.
     * @param credentialName Credential name.
     * @return Lookup user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #lookupUser(LookupUserRequest)
     */
    public ObjectResponse<LookupUserResponse> lookupUser(@NotNull String username, @NotNull String credentialName) throws NextStepClientException {
        final LookupUserRequest request = new LookupUserRequest();
        request.setUsername(username);
        request.setCredentialName(credentialName);
        return lookupUser(request);
    }

    /**
     * Lookup a single user identity with operation.
     *
     * @param username Username.
     * @param credentialName Credential name.
     * @param operationId Operation ID.
     * @return Lookup user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #lookupUser(LookupUserRequest)
     */
    public ObjectResponse<LookupUserResponse> lookupUser(@NotNull String username, @NotNull String credentialName, String operationId) throws NextStepClientException {
        final LookupUserRequest request = new LookupUserRequest();
        request.setUsername(username);
        request.setCredentialName(credentialName);
        request.setOperationId(operationId);
        return lookupUser(request);
    }

    /**
     * Lookup a single user identity.
     *
     * @param request Lookup user request.
     * @return Lookup user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<LookupUserResponse> lookupUser(@NotNull final LookupUserRequest request) throws NextStepClientException {
        return postObjectImpl("/user/lookup/single", new ObjectRequest<>(request), LookupUserResponse.class);
    }

    /**
     * Block a user identity.
     *
     * @param userId User ID.
     * @return Block user response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<BlockUserResponse> blockUser(@NotNull String userId) throws NextStepClientException {
        final BlockUserRequest request = new BlockUserRequest();
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
        final UnblockUserRequest request = new UnblockUserRequest();
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
        final DeleteUserRequest request = new DeleteUserRequest();
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
     * @see #addUserRole(AddUserRoleRequest)
     */
    public ObjectResponse<AddUserRoleResponse> addUserRole(@NotNull String userId, @NotNull String roleName) throws NextStepClientException {
        final AddUserRoleRequest request = new AddUserRoleRequest();
        request.setUserId(userId);
        request.setRoleName(roleName);
        return addUserRole(request);
    }

    /**
     * Add a role to a user.
     *
     * @param request Add user role request.
     * @return Add user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<AddUserRoleResponse> addUserRole(@NotNull final AddUserRoleRequest request) throws NextStepClientException {
        return postObjectImpl("/user/role", new ObjectRequest<>(request), AddUserRoleResponse.class);
    }

    /**
     * Remove a role to a user.
     *
     * @param userId User ID.
     * @param roleName Role name.
     * @return Remove user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #removeUserRole(RemoveUserRoleRequest)
     */
    public ObjectResponse<RemoveUserRoleResponse> removeUserRole(@NotNull String userId, @NotNull String roleName) throws NextStepClientException {
        final RemoveUserRoleRequest request = new RemoveUserRoleRequest();
        request.setUserId(userId);
        request.setRoleName(roleName);
        return removeUserRole(request);
    }

    /**
     * Remove a role to a user.
     *
     * @param request Remove user role request.
     * @return Remove user role response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<RemoveUserRoleResponse> removeUserRole(@NotNull final RemoveUserRoleRequest request) throws NextStepClientException {
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
     * @see #createUserContact(CreateUserContactRequest)
     */
    public ObjectResponse<CreateUserContactResponse> createUserContact(@NotNull String userId, @NotNull String contactName, @NotNull ContactType contactType,
                                                                       @NotNull String contactValue, boolean primary) throws NextStepClientException {
        final CreateUserContactRequest request = new CreateUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        request.setContactType(contactType);
        request.setContactValue(contactValue);
        request.setPrimary(primary);
        return createUserContact(request);
    }

    /**
     * Create a user contact.
     *
     * @param request Create user contact request.
     * @return Create user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateUserContactResponse> createUserContact(@NotNull final CreateUserContactRequest request) throws NextStepClientException {
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
     * @see #updateUserContact(UpdateUserContactRequest)
     */
    public ObjectResponse<UpdateUserContactResponse> updateUserContact(@NotNull String userId, @NotNull String contactName, @NotNull ContactType contactType,
                                                                       @NotNull String contactValue, boolean primary) throws NextStepClientException {
        final UpdateUserContactRequest request = new UpdateUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        request.setContactType(contactType);
        request.setContactValue(contactValue);
        request.setPrimary(primary);
        return updateUserContact(request);
    }

    /**
     * Update a user contact via PUT method.
     *
     * @param request Update user contact request.
     * @return Update user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserContactResponse> updateUserContact(@NotNull final UpdateUserContactRequest request) throws NextStepClientException {
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
     * @see #updateUserContactPost(UpdateUserContactRequest)
     */
    public ObjectResponse<UpdateUserContactResponse> updateUserContactPost(@NotNull String userId, @NotNull String contactName, @NotNull ContactType contactType,
                                                                           @NotNull String contactValue, boolean primary) throws NextStepClientException {
        final UpdateUserContactRequest request = new UpdateUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        request.setContactType(contactType);
        request.setContactValue(contactValue);
        request.setPrimary(primary);
        return updateUserContactPost(request);
    }

    /**
     * Update a user contact via POST method.
     *
     * @param request Update user contact request.
     * @return Update user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserContactResponse> updateUserContactPost(@NotNull final UpdateUserContactRequest request) throws NextStepClientException {
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        return getObjectImpl("/user/contact", params, GetUserContactListResponse.class);
    }

    /**
     * Get user contact list using POST method.
     *
     * @param userId User ID.
     * @return Get user contact list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserContactListResponse> getUserContactListPost(@NotNull String userId) throws NextStepClientException {
        final GetUserContactListRequest request = new GetUserContactListRequest();
        request.setUserId(userId);
        return postObjectImpl("/user/contact/list", new ObjectRequest<>(request), GetUserContactListResponse.class);
    }

    /**
     * Delete a user contact.
     *
     * @param userId User ID.
     * @param contactName Contact name.
     * @param contactType Contact type.
     * @return Delete user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #deleteUserContact(DeleteUserContactRequest)
     */
    public ObjectResponse<DeleteUserContactResponse> deleteUserContact(@NotNull String userId, @NotNull String contactName, @NotNull ContactType contactType) throws NextStepClientException {
        final DeleteUserContactRequest request = new DeleteUserContactRequest();
        request.setUserId(userId);
        request.setContactName(contactName);
        request.setContactType(contactType);
        return deleteUserContact(request);
    }

    /**
     * Delete a user contact.
     *
     * @param request Delete user contact request.
     * @return Delete user contact response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteUserContactResponse> deleteUserContact(@NotNull final DeleteUserContactRequest request) throws NextStepClientException {
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
     * @see #createUserAlias(CreateUserAliasRequest)
     */
    public ObjectResponse<CreateUserAliasResponse> createUserAlias(@NotNull String userId, @NotNull String aliasName, @NotNull String aliasValue,
                                                                   Map<String, Object> extras) throws NextStepClientException {
        final CreateUserAliasRequest request = new CreateUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        request.setAliasValue(aliasValue);
        if (extras != null) {
            request.getExtras().putAll(extras);
        }
        return createUserAlias(request);
    }

    /**
     * Create a user alias.
     *
     * @param request Create user alias request.
     * @return Create user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateUserAliasResponse> createUserAlias(@NotNull final CreateUserAliasRequest request) throws NextStepClientException {
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
     * @see #updateUserAlias(UpdateUserAliasRequest)
     */
    public ObjectResponse<UpdateUserAliasResponse> updateUserAlias(@NotNull String userId, @NotNull String aliasName, @NotNull String aliasValue,
                                                                   Map<String, Object> extras) throws NextStepClientException {
        final UpdateUserAliasRequest request = new UpdateUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        request.setAliasValue(aliasValue);
        if (extras != null) {
            request.getExtras().putAll(extras);
        }
        return updateUserAlias(request);
    }

    /**
     * Update a user alias via PUT method.
     *
     * @param request Update user alias request.
     * @return Update user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserAliasResponse> updateUserAlias(@NotNull final UpdateUserAliasRequest request) throws NextStepClientException {
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
     * @see #updateUserAliasPost(UpdateUserAliasRequest)
     */
    public ObjectResponse<UpdateUserAliasResponse> updateUserAliasPost(@NotNull String userId, @NotNull String aliasName, @NotNull String aliasValue,
                                                                       Map<String, Object> extras) throws NextStepClientException {
        final UpdateUserAliasRequest request = new UpdateUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        request.setAliasValue(aliasValue);
        if (extras != null) {
            request.getExtras().putAll(extras);
        }
        return updateUserAliasPost(request);
    }

    /**
     * Update a user alias via POST method.
     *
     * @param request Update user alias request.
     * @return Update user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateUserAliasResponse> updateUserAliasPost(@NotNull final UpdateUserAliasRequest request) throws NextStepClientException {
        return postObjectImpl("/user/alias/update", new ObjectRequest<>(request), UpdateUserAliasResponse.class);
    }

    /**
     * Get user alias list.
     *
     * @param userId User ID.
     * @param includeRemoved Whether removed aliases should be included.
     * @return Get user alias list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserAliasListResponse> getUserAliasList(@NotNull String userId, boolean includeRemoved) throws NextStepClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/user/alias", params, GetUserAliasListResponse.class);
    }

    /**
     * Get user alias list using POST method.
     *
     * @param userId User ID.
     * @param includeRemoved Whether removed aliases should be included.
     * @return Get user alias list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserAliasListResponse> getUserAliasListPost(@NotNull String userId, boolean includeRemoved) throws NextStepClientException {
        final GetUserAliasListRequest request = new GetUserAliasListRequest();
        request.setUserId(userId);
        request.setIncludeRemoved(includeRemoved);
        return postObjectImpl("/user/alias/list", new ObjectRequest<>(request), GetUserAliasListResponse.class);
    }

    /**
     * Delete a user alias.
     *
     * @param userId User ID.
     * @param aliasName Alias name.
     * @return Delete user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #deleteUserAlias(DeleteUserAliasRequest)
     */
    public ObjectResponse<DeleteUserAliasResponse> deleteUserAlias(@NotNull String userId, @NotNull String aliasName) throws NextStepClientException {
        final DeleteUserAliasRequest request = new DeleteUserAliasRequest();
        request.setUserId(userId);
        request.setAliasName(aliasName);
        return deleteUserAlias(request);
    }

    /**
     * Delete a user alias.
     *
     * @param request Delete user alias request.
     * @return Delete user alias response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteUserAliasResponse> deleteUserAlias(@NotNull final DeleteUserAliasRequest request) throws NextStepClientException {
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/user/credential", params, GetUserCredentialListResponse.class);
    }

    /**
     * Get user credential list using POST method.
     *
     * @param userId User ID.
     * @param includeRemoved Whether removed credentials should be included.
     * @return Get user credential list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetUserCredentialListResponse> getUserCredentialListPost(@NotNull String userId, boolean includeRemoved) throws NextStepClientException {
        final GetUserCredentialListRequest request = new GetUserCredentialListRequest();
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        if (createdStartDate != null) {
            params.put("createdStartDate", Collections.singletonList(createdStartDate.toString()));
        }
        if (createdEndDate != null) {
            params.put("createdEndDate", Collections.singletonList(createdEndDate.toString()));
        }
        return getObjectImpl("/user/authentication", params, GetUserAuthenticationListResponse.class);
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
    public ObjectResponse<GetUserAuthenticationListResponse> getUserAuthenticationListPost(@NotNull String userId, Date createdStartDate, Date createdEndDate) throws NextStepClientException {
        final GetUserAuthenticationListRequest request = new GetUserAuthenticationListRequest();
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
     * @see #createCredential(CreateCredentialRequest)
     */
    public ObjectResponse<CreateCredentialResponse> createCredential(@NotNull String userId, @NotNull String credentialName, @NotNull CredentialType credentialType,
                                                                     String username, String credentialValue) throws NextStepClientException {
        final CreateCredentialRequest request = new CreateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        return createCredential(request);
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
     * @param credentialHistory List with pairs of username : credentialValue for credential history.
     * @return Create credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createCredential(CreateCredentialRequest)
     */
    public ObjectResponse<CreateCredentialResponse> createCredential(@NotNull String userId, @NotNull String credentialName, @NotNull CredentialType credentialType,
                                                                     String username, String credentialValue, CredentialValidationMode validationMode,
                                                                     List<KeyValueParameter> credentialHistory) throws NextStepClientException {
        final CreateCredentialRequest request = new CreateCredentialRequest();
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
        return createCredential(request);
    }

    /**
     * Create a credential.
     *
     * @param request Create credential request.
     * @return Create credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateCredentialResponse> createCredential(@NotNull final CreateCredentialRequest request) throws NextStepClientException {
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
     * @see #updateCredential(UpdateCredentialRequest)
     */
    public ObjectResponse<UpdateCredentialResponse> updateCredential(@NotNull String userId, @NotNull String credentialName, CredentialType credentialType,
                                                                     String username, String credentialValue, CredentialStatus credentialStatus) throws NextStepClientException {
        final UpdateCredentialRequest request = new UpdateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        request.setCredentialStatus(credentialStatus);
        return updateCredential(request);
    }

    /**
     * Update a credential via PUT method.
     *
     * @param request Update credential request.
     * @return Update credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialResponse> updateCredential(@NotNull final UpdateCredentialRequest request) throws NextStepClientException {
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
     * @param credentialStatus Credential status.
     * @return Update credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #updateCredentialPost(UpdateCredentialRequest)
     */
    public ObjectResponse<UpdateCredentialResponse> updateCredentialPost(@NotNull String userId, @NotNull String credentialName, CredentialType credentialType,
                                                                         String username, String credentialValue, CredentialStatus credentialStatus) throws NextStepClientException {
        final UpdateCredentialRequest request = new UpdateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        request.setCredentialStatus(credentialStatus);
        return updateCredentialPost(request);
    }

    /**
     * Update a credential via POST method.
     *
     * @param request Update credential request.
     * @return Update credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCredentialResponse> updateCredentialPost(@NotNull final UpdateCredentialRequest request) throws NextStepClientException {
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
     * @see #validateCredential(ValidateCredentialRequest)
     */
    public ObjectResponse<ValidateCredentialResponse> validateCredential(@NotNull String userId, @NotNull String credentialName, String username,
                                                                         String credentialValue, @NotNull CredentialValidationMode validationMode) throws NextStepClientException {
        final ValidateCredentialRequest request = new ValidateCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setUsername(username);
        request.setCredentialValue(credentialValue);
        request.setValidationMode(validationMode);
        return validateCredential(request);
    }

    /**
     * Validate a credential.
     *
     * @param request Validate credential request.
     * @return Validate credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<ValidateCredentialResponse> validateCredential(@NotNull final ValidateCredentialRequest request) throws NextStepClientException {
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
     * @see #resetCredential(ResetCredentialRequest)
     */
    public ObjectResponse<ResetCredentialResponse> resetCredential(@NotNull String userId, @NotNull String credentialName, CredentialType credentialType) throws NextStepClientException {
        final ResetCredentialRequest request = new ResetCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setCredentialType(credentialType);
        return resetCredential(request);
    }

    /**
     * Reset a credential.
     *
     * @param request Reset credential request.
     * @return Reset credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<ResetCredentialResponse> resetCredential(@NotNull final ResetCredentialRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/reset", new ObjectRequest<>(request), ResetCredentialResponse.class);
    }

    /**
     * Block a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @return Block credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #blockCredential(BlockCredentialRequest)
     */
    public ObjectResponse<BlockCredentialResponse> blockCredential(@NotNull String userId, @NotNull String credentialName) throws NextStepClientException {
        final BlockCredentialRequest request = new BlockCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        return blockCredential(request);
    }

    /**
     * Block a credential.
     *
     * @param request Block credential request.
     * @return Block credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<BlockCredentialResponse> blockCredential(@NotNull final BlockCredentialRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/block", new ObjectRequest<>(request), BlockCredentialResponse.class);
    }

    /**
     * Unblock a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @return Unblock credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #unblockCredential(UnblockCredentialRequest)
     */
    public ObjectResponse<UnblockCredentialResponse> unblockCredential(@NotNull String userId, @NotNull String credentialName) throws NextStepClientException {
        final UnblockCredentialRequest request = new UnblockCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        return unblockCredential(request);
    }

    /**
     * Unblock a credential.
     *
     * @param request Unblock credential request.
     * @return Unblock credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UnblockCredentialResponse> unblockCredential(@NotNull final UnblockCredentialRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/unblock", new ObjectRequest<>(request), UnblockCredentialResponse.class);
    }

    /**
     * Delete a credential.
     *
     * @param userId User ID.
     * @param credentialName Credential name.
     * @return Delete credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #deleteCredential(DeleteCredentialRequest)
     */
    public ObjectResponse<DeleteCredentialResponse> deleteCredential(@NotNull String userId, @NotNull String credentialName) throws NextStepClientException {
        final DeleteCredentialRequest request = new DeleteCredentialRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        return deleteCredential(request);
    }

    /**
     * Delete a credential.
     *
     * @param request Delete credential request.
     * @return Delete credential response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteCredentialResponse> deleteCredential(@NotNull final DeleteCredentialRequest request) throws NextStepClientException {
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
     * @see #updateCredentialCounter(UpdateCounterRequest)
     */
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounter(@NotNull String userId, @NotNull String credentialName, @NotNull AuthenticationResult authenticationResult) throws NextStepClientException {
        final UpdateCounterRequest request = new UpdateCounterRequest();
        request.setUserId(userId);
        request.setCredentialName(credentialName);
        request.setAuthenticationResult(authenticationResult);
        return updateCredentialCounter(request);
    }

    /**
     * Update a credential counter.
     *
     * @param request Update counter request.
     * @return Update credential counter response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounter(@NotNull final UpdateCounterRequest request) throws NextStepClientException {
        return postObjectImpl("/credential/counter/update", new ObjectRequest<>(request), UpdateCounterResponse.class);
    }

    /**
     * Reset all soft counters.
     *
     * @param request Reset all soft counters request.
     * @return Reset counters response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<ResetCountersResponse> resetAllCounters(ResetCountersRequest request) throws NextStepClientException {
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
     * @see #createOtp(CreateOtpRequest)
     */
    public ObjectResponse<CreateOtpResponse> createOtp(String userId, @NotNull String otpName, String credentialName, String otpData, String operationId) throws NextStepClientException {
        final CreateOtpRequest request = new CreateOtpRequest();
        request.setUserId(userId);
        request.setOtpName(otpName);
        request.setCredentialName(credentialName);
        request.setOtpData(otpData);
        request.setOperationId(operationId);
        return createOtp(request);
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
     * @see #createOtp(CreateOtpRequest)
     */
    public ObjectResponse<CreateOtpResponse> createOtp(String userId, @NotNull String otpName, String credentialName, String otpData) throws NextStepClientException {
        final CreateOtpRequest request = new CreateOtpRequest();
        request.setUserId(userId);
        request.setOtpName(otpName);
        request.setCredentialName(credentialName);
        request.setOtpData(otpData);
        return createOtp(request);
    }

    /**
     * Create an OTP.
     *
     * @param request Create OTP request.
     * @return Create OTP response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateOtpResponse> createOtp(final CreateOtpRequest request) throws NextStepClientException {
        return postObjectImpl("/otp", new ObjectRequest<>(request), CreateOtpResponse.class);
    }

    /**
     * Create an OTP with operation and send it.
     *
     * @param userId User ID.
     * @param otpName OTP name.
     * @param credentialName Credential name.
     * @param otpData OTP data.
     * @param operationId Operation ID.
     * @param language Language as defined in ISO-639 with 2 characters.
     * @return Create and send OTP response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createAndSendOtp(CreateAndSendOtpRequest)
     */
    public ObjectResponse<CreateAndSendOtpResponse> createAndSendOtp(String userId, @NotNull String otpName, String credentialName, String otpData, String operationId, String language) throws NextStepClientException {
        final CreateAndSendOtpRequest request = new CreateAndSendOtpRequest();
        request.setUserId(userId);
        request.setOtpName(otpName);
        request.setCredentialName(credentialName);
        request.setOtpData(otpData);
        request.setOperationId(operationId);
        request.setLanguage(language);
        return createAndSendOtp(request);
    }

    /**
     * Create an OTP with operation and send it.
     *
     * @param request Create and send OTP request.
     * @return Create and send OTP response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateAndSendOtpResponse> createAndSendOtp(final CreateAndSendOtpRequest request) throws NextStepClientException {
        return postObjectImpl("/otp/send", new ObjectRequest<>(request), CreateAndSendOtpResponse.class);
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("operationId", Collections.singletonList(operationId));
        params.put("includeRemoved", Collections.singletonList(String.valueOf(includeRemoved)));
        return getObjectImpl("/otp", params, GetOtpListResponse.class);
    }

    /**
     * Get OTP list using POST method.
     *
     * @param operationId Operation ID.
     * @param includeRemoved Whether removed OTPs should be included.
     * @return Get OTP list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpListResponse> getOtpListPost(@NotNull String operationId, boolean includeRemoved) throws NextStepClientException {
        final GetOtpListRequest request = new GetOtpListRequest();
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (otpId != null) {
            params.put("otpId", Collections.singletonList(otpId));
        }
        if (operationId != null) {
            params.put("operationId", Collections.singletonList(operationId));
        }
        return getObjectImpl("/otp/detail", params, GetOtpDetailResponse.class);
    }

    /**
     * Get OTP detail using POST method.
     *
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @return Get OTP list response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<GetOtpDetailResponse> getOtpDetailPost(String otpId, String operationId) throws NextStepClientException {
        final GetOtpDetailRequest request = new GetOtpDetailRequest();
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
     * @see #deleteOtp(DeleteOtpRequest)
     */
    public ObjectResponse<DeleteOtpResponse> deleteOtp(String otpId, String operationId) throws NextStepClientException {
        final DeleteOtpRequest request = new DeleteOtpRequest();
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        return deleteOtp(request);
    }

    /**
     * Delete an OTP.
     *
     * @param request Delete OTP request.
     * @return Delete OTP response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<DeleteOtpResponse> deleteOtp(@NotNull final DeleteOtpRequest request) throws NextStepClientException {
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
     * @see #authenticateWithOtp(OtpAuthenticationRequest)
     */
    public ObjectResponse<OtpAuthenticationResponse> authenticateWithOtp(String otpId, @NotNull String otpValue) throws NextStepClientException {
        final OtpAuthenticationRequest request = new OtpAuthenticationRequest();
        request.setOtpId(otpId);
        request.setOtpValue(otpValue);
        return authenticateWithOtp(request);
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
     * @see #authenticateWithOtp(OtpAuthenticationRequest)
     */
    public ObjectResponse<OtpAuthenticationResponse> authenticateWithOtp(String otpId, String operationId, @NotNull String otpValue,
                                                                         boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        final OtpAuthenticationRequest request = new OtpAuthenticationRequest();
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        request.setOtpValue(otpValue);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return authenticateWithOtp(request);
    }

    /**
     * Authenticate using OTP with operation and checkOnly parameter.
     *
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @param otpValue OTP value.
     * @param checkOnly Whether the OTP value is only being checked, authentication result is not persisted in this case.
     * @param updateOperation Whether operation should be updated.
     * @param authMethod Authentication method used for operation update.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #authenticateWithOtp(OtpAuthenticationRequest)
     */
    public ObjectResponse<OtpAuthenticationResponse> authenticateWithOtp(String otpId, String operationId, @NotNull String otpValue, boolean checkOnly,
                                                                         boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        final OtpAuthenticationRequest request = new OtpAuthenticationRequest();
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        request.setOtpValue(otpValue);
        request.setCheckOnly(checkOnly);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return authenticateWithOtp(request);
    }

    /**
     * Authenticate using OTP with operation.
     *
     * @param request OTP authentication request.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<OtpAuthenticationResponse> authenticateWithOtp(@NotNull final OtpAuthenticationRequest request) throws NextStepClientException {
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
     * @see #authenticateWithCredential(CredentialAuthenticationRequest)
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticateWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue) throws NextStepClientException {
        final CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        return authenticateWithCredential(request);
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
     * @see #authenticateWithCredential(CredentialAuthenticationRequest)
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticateWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                         CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify) throws NextStepClientException {
        final CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        return authenticateWithCredential(request);
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
     * @see #authenticateWithCredential(CredentialAuthenticationRequest)
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticateWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                         String operationId, boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        final CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setOperationId(operationId);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return authenticateWithCredential(request);
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
     * @see #authenticateWithCredential(CredentialAuthenticationRequest)
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticateWithCredential(@NotNull String credentialName, @NotNull String userId, @NotNull String credentialValue,
                                                                                         CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify,
                                                                                         String operationId, boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        final CredentialAuthenticationRequest request = new CredentialAuthenticationRequest();
        request.setCredentialName(credentialName);
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        request.setOperationId(operationId);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return authenticateWithCredential(request);
    }

    /**
     * Authenticate using credential.
     *
     * @param request Credential authentication request.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CredentialAuthenticationResponse> authenticateWithCredential(@NotNull final CredentialAuthenticationRequest request) throws NextStepClientException {
        return postObjectImpl("/auth/credential", new ObjectRequest<>(request), CredentialAuthenticationResponse.class);
    }

    /**
     * Authenticate using credential and OTP without operation.
     *
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #authenticateCombined(CombinedAuthenticationRequest)
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticateCombined(@NotNull String userId, @NotNull String credentialValue,
                                                                                 String otpId, @NotNull String otpValue) throws NextStepClientException {
        final CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setOtpId(otpId);
        request.setOtpValue(otpValue);
        return authenticateCombined(request);
    }

    /**
     * Authenticate using credential and OTP without operation and with additional details.
     *
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param authenticationMode Authentication mode.
     * @param credentialPositionsToVerify Credential positions to verify.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #authenticateCombined(CombinedAuthenticationRequest)
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticateCombined(@NotNull String userId, @NotNull String credentialValue,
                                                                                 CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify,
                                                                                 String otpId, @NotNull String otpValue) throws NextStepClientException {
        final CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        request.setOtpId(otpId);
        request.setOtpValue(otpValue);
        return authenticateCombined(request);
    }

    /**
     * Authenticate using credential and OTP with operation.
     *
     * @param userId User ID.
     * @param credentialValue Credential value.
     * @param otpId OTP ID.
     * @param operationId Operation ID.
     * @param otpValue OTP value.
     * @param updateOperation Whether operation should be updated.
     * @param authMethod Authentication method used for operation update.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #authenticateCombined(CombinedAuthenticationRequest)
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticateCombined(@NotNull String userId, @NotNull String credentialValue,
                                                                                 String otpId, String operationId, @NotNull String otpValue,
                                                                                 boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        final CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        request.setOtpValue(otpValue);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return authenticateCombined(request);
    }

    /**
     * Authenticate using credential and OTP with operation and with additional details.
     *
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
     * @see #authenticateCombined(CombinedAuthenticationRequest)
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticateCombined(@NotNull String userId, @NotNull String credentialValue,
                                                                                 CredentialAuthenticationMode authenticationMode, List<Integer> credentialPositionsToVerify,
                                                                                 String otpId, String operationId, @NotNull String otpValue,
                                                                                 boolean updateOperation, AuthMethod authMethod) throws NextStepClientException {
        final CombinedAuthenticationRequest request = new CombinedAuthenticationRequest();
        request.setUserId(userId);
        request.setCredentialValue(credentialValue);
        request.setAuthenticationMode(authenticationMode);
        request.setCredentialPositionsToVerify(credentialPositionsToVerify);
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        request.setOtpValue(otpValue);
        request.setUpdateOperation(updateOperation);
        request.setAuthMethod(authMethod);
        return authenticateCombined(request);
    }

    /**
     * Authenticate using credential and OTP.
     *
     * @param request Combined authentication request.
     * @return OTP authentication response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CombinedAuthenticationResponse> authenticateCombined(@NotNull final CombinedAuthenticationRequest request) throws NextStepClientException {
        return postObjectImpl("/auth/combined", new ObjectRequest<>(request), CombinedAuthenticationResponse.class);
    }

    // Audit log related methods

    /**
     * Create an audit log.
     *
     * @param action Audited action.
     * @param data Audit log data.
     * @return Create audit log response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     * @see #createAudit(CreateAuditRequest)
     */
    public ObjectResponse<CreateAuditResponse> createAudit(@NotNull String action, String data) throws NextStepClientException {
        final CreateAuditRequest request = new CreateAuditRequest();
        request.setAction(action);
        request.setData(data);
        return createAudit(request);
    }

    /**
     * Create an audit log.
     *
     * @param request Create audit request.
     * @return Create audit log response.
     * @throws NextStepClientException Thrown when REST API call fails, including {@link ErrorResponse} with error code.
     */
    public ObjectResponse<CreateAuditResponse> createAudit(@NotNull final CreateAuditRequest request) throws NextStepClientException {
        return postObjectImpl("/audit", new ObjectRequest<>(request), CreateAuditResponse.class);
    }

    // Generic HTTP client methods

    /**
     * Prepare GET object response.
     *
     * @param path Resource path.
     * @param queryParams Query parameters.
     * @param typeReference Type reference.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> T getImpl(String path, MultiValueMap<String, String> queryParams, ParameterizedTypeReference<T> typeReference) throws NextStepClientException {
        try {
            return restClient.get(path, queryParams, null, typeReference).getBody();
        } catch (RestClientException ex) {
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
        }
    }

    /**
     * Prepare GET object response.
     *
     * @param path Resource path.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> getObjectImpl(String path, Class<T> responseType) throws NextStepClientException {
        try {
            return restClient.getObject(path, responseType);
        } catch (RestClientException ex) {
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
        }
    }

    /**
     * Prepare GET object response.
     *
     * @param path Resource path.
     * @param queryParams Query parameters.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws NextStepClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> getObjectImpl(String path, MultiValueMap<String, String> queryParams, Class<T> responseType) throws NextStepClientException {
        try {
            return restClient.getObject(path, queryParams, null, responseType);
        } catch (RestClientException ex) {
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
        }
    }

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
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
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
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
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
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
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
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
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
            final NextStepClientException ex2 = new NextStepClientException(ex);
            logError(ex2);
            throw ex2;
        }
    }

    /**
     * Log Next Step client exception details.
     * @param ex Next Step client exception.
     */
    private void logError(NextStepClientException ex) {
        Error error = ex.getError();
        if (error != null) {
            logger.warn("Next Step REST API call failed with error code: {}", error.getCode());
        } else {
            logger.warn(ex.getMessage(), ex);
        }
    }

}
