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

package io.getlime.security.powerauth.lib.nextstep.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ErrorModel;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOperationDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetPendingOperationsRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuthMethodsResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * This service handles communication with the Next Step server.
 *
 * @author Roman Strobl
 */
public class NextStepClient {

    private String serviceUrl;
    private ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public NextStepClient() {
    }

    /**
     * Create a new client with provided base URL.
     * @param serviceUrl Base URL.
     */
    public NextStepClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a new client with provided base URL and custom object mapper.
     * @param serviceUrl Base URL.
     * @param objectMapper Object mapper.
     */
    public NextStepClient(String serviceUrl, ObjectMapper objectMapper) {
        this.serviceUrl = serviceUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Prepare a default instance of REST client.
     * @return RestTemplate with default configuration.
     */
    private RestTemplate defaultTemplate() {
        RestTemplate template = new RestTemplate();
        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return template;
    }

    /**
     * Calls the operation endpoint via POST method to create a new operation.
     *
     * @param operationName operation name
     * @param operationData operation data
     * @param params        list of generic parameters
     * @return a Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public Response<CreateOperationResponse> createOperation(String operationName, String operationData, List<KeyValueParameter> params) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            CreateOperationRequest request = new CreateOperationRequest();
            request.setOperationName(operationName);
            request.setOperationData(operationData);
            if (params != null) {
                request.getParams().addAll(params);
            }
            HttpEntity<Request<CreateOperationRequest>> entity = new HttpEntity<>(new Request<>(request));
            ResponseEntity<Response<CreateOperationResponse>> response = defaultTemplate().exchange(serviceUrl + "/operation", HttpMethod.POST, entity, new ParameterizedTypeReference<Response<CreateOperationResponse>>() {});
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Calls the operation endpoint via PUT method to update an existing.
     *
     * @param operationId    id of the updated operation
     * @param userId         user id
     * @param authMethod     authentication method
     * @param authStepResult result of the last step
     * @param params         list of generic parameters
     * @return a Response with UpdateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public Response<UpdateOperationResponse> updateOperation(String operationId, String userId, AuthMethod authMethod, AuthStepResult authStepResult, List<KeyValueParameter> params) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateOperationRequest request = new UpdateOperationRequest();
            request.setOperationId(operationId);
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            request.setAuthStepResult(authStepResult);
            if (params != null) {
                request.getParams().addAll(params);
            }
            HttpEntity<Request<UpdateOperationRequest>> entity = new HttpEntity<>(new Request<>(request));
            ResponseEntity<Response<UpdateOperationResponse>> response = defaultTemplate().exchange(serviceUrl + "/operation", HttpMethod.PUT, entity, new ParameterizedTypeReference<Response<UpdateOperationResponse>>() {
            });
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Calls the operation details endpoint via POST method to get operation details.
     *
     * @param id operation id
     * @return a Response with {@link GetOperationDetailResponse} object for OK status or {@link ErrorModel} for ERROR status
     */
    public Response<GetOperationDetailResponse> getOperationDetail(String id) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetOperationDetailRequest request = new GetOperationDetailRequest();
            request.setOperationId(id);
            HttpEntity<Request<GetOperationDetailRequest>> entity = new HttpEntity<>(new Request<>(request));
            ResponseEntity<Response<GetOperationDetailResponse>> response = defaultTemplate().exchange(serviceUrl + "/operation/detail", HttpMethod.POST, entity, new ParameterizedTypeReference<Response<GetOperationDetailResponse>>() {});
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    public Response<List<GetOperationDetailResponse>> getPendingOperations(String userId) throws NextStepServiceException {
        return getPendingOperations(userId, null);
    }

    /**
     * Calls the get pending operations endpoint via METHOD to get a list of pending operations.
     * @param userId user id
     * @param authMethod authentication method
     * @return a Response with list of {@link GetOperationDetailResponse} for OK status
     * @throws NextStepServiceException Exception with {@link ErrorModel} for ERROR status
     */
    public Response<List<GetOperationDetailResponse>> getPendingOperations(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetPendingOperationsRequest request = new GetPendingOperationsRequest();
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            HttpEntity<Request<GetPendingOperationsRequest>> entity = new HttpEntity<>(new Request<>(request));
            ResponseEntity<Response<List<GetOperationDetailResponse>>> response = defaultTemplate().exchange(serviceUrl + "/user/operation/list", HttpMethod.POST, entity, new ParameterizedTypeReference<Response<List<GetOperationDetailResponse>>>() {
            });
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     * @throws NextStepServiceException Exception with {@link ErrorModel} for ERROR status
     */
    public Response<GetAuthMethodsResponse> getAuthMethods() throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            ResponseEntity<Response<GetAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/auth-method", HttpMethod.GET, null, new ParameterizedTypeReference<Response<GetAuthMethodsResponse>>() {
            });
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Get all enabled authentication methods for given user.
     *
     * @param userId User ID
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     * @throws NextStepServiceException Exception with {@link ErrorModel} for ERROR status
     */
    public Response<GetAuthMethodsResponse> getAuthMethodsEnabledForUser(String userId) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            ResponseEntity<Response<GetAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/user/" + userId + "/auth-method", HttpMethod.GET, null, new ParameterizedTypeReference<Response<GetAuthMethodsResponse>>() {
            });
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param userId     User ID
     * @param authMethod Authentication method
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     * @throws NextStepServiceException Exception with {@link ErrorModel} for ERROR status
     */
    public Response<GetAuthMethodsResponse> enableAuthMethodForUser(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            ResponseEntity<Response<GetAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/user/" + userId + "/auth-method/" + authMethod, HttpMethod.POST, null, new ParameterizedTypeReference<Response<GetAuthMethodsResponse>>() {
            });
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param userId     User ID
     * @param authMethod Authentication method
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     * @throws NextStepServiceException Exception with {@link ErrorModel} for ERROR status
     */
    public Response<GetAuthMethodsResponse> disableAuthMethodForUser(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            ResponseEntity<Response<GetAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/user/" + userId + "/auth-method/" + authMethod, HttpMethod.DELETE, null, new ParameterizedTypeReference<Response<GetAuthMethodsResponse>>() {
            });
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }


    /**
     * Handle resource access error (i.e. server not available).
     * @param ex Exception to handle
     * @return Next step service exception
     * @throws NextStepServiceException exception with ErrorModel
     */
    private NextStepServiceException handleResourceAccessError(ResourceAccessException ex) throws NextStepServiceException {
        ErrorModel error = new ErrorModel();
        error.setCode(ErrorModel.Code.ERROR_GENERIC);
        error.setMessage(ex.getMessage());
        return new NextStepServiceException(ex, error);
    }

    /**
     * Handle HTTP error.
     * @param ex Exception to handle
     * @return Next step service exception
     * @throws NextStepServiceException exception with ErrorModel
     */
    private NextStepServiceException handleHttpError(HttpStatusCodeException ex) throws NextStepServiceException {
        try {
            TypeReference<Response<ErrorModel>> typeReference = new TypeReference<Response<ErrorModel>>() {};
            Response<ErrorModel> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
            ErrorModel error = errorResponse.getResponseObject();
            return new NextStepServiceException(ex, error);
        } catch (IOException ex2) {
            // JSON parsing failed
            ErrorModel error = new ErrorModel();
            error.setCode(ErrorModel.Code.ERROR_GENERIC);
            error.setMessage(ex2.getMessage());
            return new NextStepServiceException(ex, error);
        }
    }

}
