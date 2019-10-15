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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This service handles client communication with the Next Step server.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepClient {

    private final String serviceUrl;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * Create a new client with provided base URL.
     * @param serviceUrl Base URL.
     */
    public NextStepClient(String serviceUrl) {
        this(serviceUrl, null);
    }


    /**
     * Create a new client with provided base URL and custom object mapper.
     * @param serviceUrl Base URL.
     * @param objectMapper Object mapper.
     */
    public NextStepClient(String serviceUrl, ObjectMapper objectMapper) {
        this.serviceUrl = serviceUrl;
        if (objectMapper != null) {
            this.objectMapper = objectMapper;
        } else {
            this.objectMapper = objectMapper();
        }
        restTemplate = new RestTemplate();

        // Prepare message converters
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(this.objectMapper);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);
        restTemplate.setMessageConverters(converters);

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    /**
     * Construct object mapper with default configuration which allows sending empty objects and allows unknown properties.
     * @return Constructed object mapper.
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Get default instance of REST client.
     * @return RestTemplate with default configuration.
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationData Operation data.
     * @param params List of generic parameters.
     * @return A Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationData, List<KeyValueParameter> params) throws NextStepServiceException {
        return createOperation(operationName, null, operationData, null, null, params, null);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationId Operation ID (optional - if null, unique ID is automatically generated).
     * @param operationData Operation data.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with CreateOperationResponse object for OK status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationId, String operationData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepServiceException {
        return createOperation(operationName, operationId, operationData, null, null, params, applicationContext);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationData Operation data.
     * @param formData Operation form data, such as title, message and displayable attributes.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationData, OperationFormData formData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepServiceException {
        return createOperation(operationName, null, operationData, null, formData, params, applicationContext);
    }

    /**
     * Calls the create operation endpoint via POST method to create a new operation.
     *
     * @param operationName Operation name.
     * @param operationId Operation ID (optional - if null, unique ID is automatically generated).
     * @param operationData Operation data.
     * @param organizationId Organization ID.
     * @param formData Operation form data, such as title, message and displayable attributes.
     * @param params List of generic parameters.
     * @param applicationContext Context of application requesting the OAuth 2.0 consent.
     * @return A Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationId, String operationData, String organizationId, OperationFormData formData, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            CreateOperationRequest request = new CreateOperationRequest();
            request.setOperationName(operationName);
            request.setOperationId(operationId);
            request.setOperationData(operationData);
            request.setOrganizationId(organizationId);
            request.setFormData(formData);
            if (params != null) {
                request.getParams().addAll(params);
            }
            request.setApplicationContext(applicationContext);
            HttpEntity<ObjectRequest<CreateOperationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<CreateOperationResponse>> response = restTemplate.exchange(serviceUrl + "/operation", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<CreateOperationResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
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
     * @return A Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationData, OperationFormData formData, String organizationId, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepServiceException {
        return createOperation(operationName, null, operationData, organizationId, formData, params, applicationContext);
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
     * @return A Response with UpdateOperationResponse object for OK status or ErrorModel for ERROR status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<UpdateOperationResponse> updateOperation(String operationId, String userId, String organizationId, AuthMethod authMethod, List<AuthInstrument> authInstruments, AuthStepResult authStepResult, String authStepResultDescription, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateOperationRequest request = new UpdateOperationRequest();
            request.setOperationId(operationId);
            request.setUserId(userId);
            request.setOrganizationId(organizationId);
            request.setAuthMethod(authMethod);
            request.setAuthInstruments(authInstruments);
            request.setAuthStepResult(authStepResult);
            request.setAuthStepResultDescription(authStepResultDescription);
            if (params != null) {
                request.getParams().addAll(params);
            }
            request.setApplicationContext(applicationContext);
            HttpEntity<ObjectRequest<UpdateOperationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UpdateOperationResponse>> response = restTemplate.exchange(serviceUrl + "/operation", HttpMethod.PUT, entity, new ParameterizedTypeReference<ObjectResponse<UpdateOperationResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
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
     * @return A Response with UpdateOperationResponse object for OK status or ErrorModel for ERROR status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<UpdateOperationResponse> updateOperationPost(String operationId, String userId, String organizationId, AuthMethod authMethod, List<AuthInstrument> authInstruments, AuthStepResult authStepResult, String authStepResultDescription, List<KeyValueParameter> params, ApplicationContext applicationContext) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateOperationRequest request = new UpdateOperationRequest();
            request.setOperationId(operationId);
            request.setUserId(userId);
            request.setOrganizationId(organizationId);
            request.setAuthMethod(authMethod);
            request.setAuthInstruments(authInstruments);
            request.setAuthStepResult(authStepResult);
            request.setAuthStepResultDescription(authStepResultDescription);
            if (params != null) {
                request.getParams().addAll(params);
            }
            request.setApplicationContext(applicationContext);
            HttpEntity<ObjectRequest<UpdateOperationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UpdateOperationResponse>> response = restTemplate.exchange(serviceUrl + "/operation/update", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<UpdateOperationResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Update user and organization for an operation via PUT method.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus User account status.
     * @return Response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public Response updateOperationUser(String operationId, String userId, String organizationId, UserAccountStatus accountStatus) throws NextStepServiceException {
        try {
            // Exchange request with NextStep server.
            UpdateOperationUserRequest request = new UpdateOperationUserRequest(operationId, userId, organizationId, accountStatus);
            HttpEntity<ObjectRequest<UpdateOperationUserRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<Response> nsResponse = restTemplate.exchange(serviceUrl + "/operation/user", HttpMethod.PUT, entity, Response.class);
            return nsResponse.getBody();
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Update user and organization for an operation via POST method.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus User account status.
     * @return Response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public Response updateOperationUserPost(String operationId, String userId, String organizationId, UserAccountStatus accountStatus) throws NextStepServiceException {
        try {
            // Exchange request with NextStep server.
            UpdateOperationUserRequest request = new UpdateOperationUserRequest(operationId, userId, organizationId, accountStatus);
            HttpEntity<ObjectRequest<UpdateOperationUserRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<Response> nsResponse = restTemplate.exchange(serviceUrl + "/operation/user/update", HttpMethod.POST, entity, Response.class);
            return nsResponse.getBody();
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Update operation form data via PUT method.
     * @param operationId Operation ID.
     * @param formData Form data.
     * @return Object response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse updateOperationFormData(String operationId, OperationFormData formData) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateFormDataRequest request = new UpdateFormDataRequest();
            request.setOperationId(operationId);
            request.setFormData(formData);
            HttpEntity<ObjectRequest<UpdateFormDataRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = restTemplate.exchange(serviceUrl + "/operation/formData", HttpMethod.PUT, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Update operation form data via POST method.
     * @param operationId Operation ID.
     * @param formData Form data.
     * @return Object response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse updateOperationFormDataPost(String operationId, OperationFormData formData) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateFormDataRequest request = new UpdateFormDataRequest();
            request.setOperationId(operationId);
            request.setFormData(formData);
            HttpEntity<ObjectRequest<UpdateFormDataRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = restTemplate.exchange(serviceUrl + "/operation/formData/update", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Update chosen authentication method for current operation step via PUT method.
     * @param operationId Operation ID.
     * @param chosenAuthMethod Chosen authentication method.
     * @return Object response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse updateChosenAuthMethod(String operationId, AuthMethod chosenAuthMethod) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateChosenAuthMethodRequest request = new UpdateChosenAuthMethodRequest();
            request.setOperationId(operationId);
            request.setChosenAuthMethod(chosenAuthMethod);
            HttpEntity<ObjectRequest<UpdateChosenAuthMethodRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = restTemplate.exchange(serviceUrl + "/operation/chosenAuthMethod", HttpMethod.PUT, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Update chosen authentication method for current operation step via POST method.
     * @param operationId Operation ID.
     * @param chosenAuthMethod Chosen authentication method.
     * @return Object response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse updateChosenAuthMethodPost(String operationId, AuthMethod chosenAuthMethod) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateChosenAuthMethodRequest request = new UpdateChosenAuthMethodRequest();
            request.setOperationId(operationId);
            request.setChosenAuthMethod(chosenAuthMethod);
            HttpEntity<ObjectRequest<UpdateChosenAuthMethodRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = restTemplate.exchange(serviceUrl + "/operation/chosenAuthMethod/update", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Calls the operation details endpoint via POST method to get operation details.
     *
     * @param id Operation ID.
     * @return A Response with {@link GetOperationDetailResponse} object for OK status or.
     * {@link io.getlime.core.rest.model.base.entity.Error} for ERROR status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<GetOperationDetailResponse> getOperationDetail(String id) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetOperationDetailRequest request = new GetOperationDetailRequest();
            request.setOperationId(id);
            HttpEntity<ObjectRequest<GetOperationDetailRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetOperationDetailResponse>> response = restTemplate.exchange(serviceUrl + "/operation/detail", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetOperationDetailResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get operation configuration.
     * @param operationName Operation name.
     * @return Operation configuration.
     * @throws NextStepServiceException Thrown when operation configuration is missing.
     */
    public ObjectResponse<GetOperationConfigDetailResponse> getOperationConfigDetail(String operationName) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetOperationConfigDetailRequest request = new GetOperationConfigDetailRequest();
            request.setOperationName(operationName);
            HttpEntity<ObjectRequest<GetOperationConfigDetailRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetOperationConfigDetailResponse>> response = restTemplate.exchange(serviceUrl + "/operation/config/detail", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetOperationConfigDetailResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get all operation configurations.
     * @return All operation configurations.
     * @throws NextStepServiceException Thrown when communication with Next Step service fails.
     */
    public ObjectResponse<GetOperationConfigListResponse> getOperationConfigList() throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetOperationConfigListRequest request = new GetOperationConfigListRequest();
            HttpEntity<ObjectRequest<GetOperationConfigListRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetOperationConfigListResponse>> response = restTemplate.exchange(serviceUrl + "/operation/config/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetOperationConfigListResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get organization detail.
     * @param organizationId Organization ID.
     * @return Organization detail.
     * @throws NextStepServiceException Thrown when organization is missing or communication with Next Step service fails.
     */
    public ObjectResponse<GetOrganizationDetailResponse> getOrganizationDetail(String organizationId) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetOrganizationDetailRequest request = new GetOrganizationDetailRequest();
            request.setOrganizationId(organizationId);
            HttpEntity<ObjectRequest<GetOrganizationDetailRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetOrganizationDetailResponse>> response = restTemplate.exchange(serviceUrl + "/organization/detail", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetOrganizationDetailResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get all organizations.
     * @return All organizations.
     * @throws NextStepServiceException Thrown when communication with Next Step service fails.
     */
    public ObjectResponse<GetOrganizationListResponse> getOrganizationList() throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetOrganizationListRequest request = new GetOrganizationListRequest();
            HttpEntity<ObjectRequest<GetOrganizationListRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetOrganizationListResponse>> response = restTemplate.exchange(serviceUrl + "/organization/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetOrganizationListResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get list of pending operations for given user.
     * @param userId User ID.
     * @return List of pending operations.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(String userId) throws NextStepServiceException {
        return getPendingOperations(userId, false);
    }

    /**
     * Get list of pending operations for given user and authentication method.
     * @param userId User ID.
     * @param mobileTokenOnly Whether pending operation list should be filtered for only next step with mobile token support.
     * @return A Response with list of {@link GetOperationDetailResponse} for OK status.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(String userId, boolean mobileTokenOnly) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetPendingOperationsRequest request = new GetPendingOperationsRequest();
            request.setUserId(userId);
            request.setMobileTokenOnly(mobileTokenOnly);
            HttpEntity<ObjectRequest<GetPendingOperationsRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<List<GetOperationDetailResponse>>> response = restTemplate.exchange(serviceUrl + "/user/operation/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<List<GetOperationDetailResponse>>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @return List of authentication methods wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<GetAuthMethodsResponse> getAuthMethods() throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetAuthMethodListRequest request = new GetAuthMethodListRequest();
            HttpEntity<ObjectRequest<GetAuthMethodListRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetAuthMethodsResponse>> response = restTemplate.exchange(serviceUrl + "/auth-method/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetAuthMethodsResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get all enabled authentication methods for given user.
     *
     * @param userId User ID.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsEnabledForUser(String userId) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetUserAuthMethodsRequest request = new GetUserAuthMethodsRequest();
            request.setUserId(userId);
            HttpEntity<ObjectRequest<GetUserAuthMethodsRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetUserAuthMethodsResponse>> response = restTemplate.exchange(serviceUrl + "/user/auth-method/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetUserAuthMethodsResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @param config Authentication method configuration.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<GetAuthMethodsResponse> enableAuthMethodForUser(String userId, AuthMethod authMethod, Map<String, String> config) throws NextStepServiceException {
        try {
            UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            request.setConfig(config);
            HttpEntity<ObjectRequest<UpdateAuthMethodRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            // Exchange next step request with NextStep server.
            ResponseEntity<ObjectResponse<GetAuthMethodsResponse>> response = restTemplate.exchange(serviceUrl + "/user/auth-method", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetAuthMethodsResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Disable an authentication method for given user via DELETE method.
     *
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<GetAuthMethodsResponse> disableAuthMethodForUser(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            HttpEntity<ObjectRequest<UpdateAuthMethodRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            // Exchange next step request with NextStep server.
            ResponseEntity<ObjectResponse<GetAuthMethodsResponse>> response = restTemplate.exchange(serviceUrl + "/user/auth-method", HttpMethod.DELETE, entity, new ParameterizedTypeReference<ObjectResponse<GetAuthMethodsResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Disable an authentication method for given user via POST method.
     *
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails, including {@link Error} with ERROR code.
     */
    public ObjectResponse<GetAuthMethodsResponse> disableAuthMethodForUserPost(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            HttpEntity<ObjectRequest<UpdateAuthMethodRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            // Exchange next step request with NextStep server.
            ResponseEntity<ObjectResponse<GetAuthMethodsResponse>> response = restTemplate.exchange(serviceUrl + "/user/auth-method/delete", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetAuthMethodsResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Create an AFS action in Next Step and log its request and response parameters.
     * @param operationId Operation ID.
     * @param afsAction AFS action.
     * @param stepIndex Step index.
     * @param requestAfsExtras AFS request extras.
     * @param afsLabel AFS label.
     * @param afsResponseApplied Whether AFS response was applied.
     * @param responseAfsExtras AFS response extras.
     * @return Response.
     * @throws NextStepServiceException In case communication with Next Step fails.
     */
    public Response createAfsAction(String operationId, String afsAction, int stepIndex, String requestAfsExtras, String afsLabel,
                                    boolean afsResponseApplied, String responseAfsExtras) throws NextStepServiceException {
        try {
            CreateAfsActionRequest request = new CreateAfsActionRequest();
            request.setOperationId(operationId);
            request.setAfsAction(afsAction);
            request.setStepIndex(stepIndex);
            request.setRequestAfsExtras(requestAfsExtras);
            request.setAfsLabel(afsLabel);
            request.setAfsResponseApplied(afsResponseApplied);
            request.setResponseAfsExtras(responseAfsExtras);
            request.setTimestampCreated(new Date());
            HttpEntity<ObjectRequest<CreateAfsActionRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            // Exchange next step request with NextStep server.
            ResponseEntity<Response> response = restTemplate.exchange(serviceUrl + "/operation/afs", HttpMethod.POST, entity, Response.class);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Handle resource access error (i.e. server not available).
     * @param ex Exception to handle.
     */
    private NextStepServiceException handleResourceAccessError(ResourceAccessException ex) {
        Error error = new Error(NextStepServiceException.COMMUNICATION_ERROR, ex.getMessage());
        return new NextStepServiceException(ex, error);
    }

    /**
     * Handle HTTP error.
     * @param ex Exception to handle.
     * @return Next step service exception.
     */
    private NextStepServiceException handleHttpError(HttpStatusCodeException ex) {
        try {
            TypeReference<ErrorResponse> typeReference = new TypeReference<ErrorResponse>() {};
            ErrorResponse errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
            Error error = errorResponse.getResponseObject();
            switch (error.getCode()) {
                case OperationAlreadyFinishedException.CODE:
                    return new OperationAlreadyFinishedException(error.getMessage());
                case OperationAlreadyFailedException.CODE:
                    return new OperationAlreadyFailedException(error.getMessage());
                case OperationAlreadyCanceledException.CODE:
                    return new OperationAlreadyCanceledException(error.getMessage());
                case OperationNotFoundException.CODE:
                    return new OperationNotFoundException(error.getMessage());
                case OperationNotConfiguredException.CODE:
                    return new OperationNotConfiguredException(error.getMessage());
                case OperationAlreadyExistsException.CODE:
                    return new OperationAlreadyExistsException(error.getMessage());
                case InvalidOperationDataException.CODE:
                    return new InvalidOperationDataException(error.getMessage());
                case OrganizationNotFoundException.CODE:
                    return new OrganizationNotFoundException(error.getMessage());
                default:
                    return new NextStepServiceException(ex, error);
            }
        } catch (IOException ex2) {
            Error error;
            if (ex.getStatusCode() != HttpStatus.OK) {
                error = new Error(NextStepServiceException.COMMUNICATION_ERROR, "HTTP error occurred: " + ex.getMessage());
                return new NextStepServiceException(ex, error);
            } else {
                error = new Error(Error.Code.ERROR_GENERIC, "IO error occurred: " + ex2.getMessage());
                return new NextStepServiceException(ex2, error);
            }
        }
    }

}
