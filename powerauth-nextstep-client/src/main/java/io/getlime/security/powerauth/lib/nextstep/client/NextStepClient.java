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
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyFailedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyFinishedException;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
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
     * @param operationId   operation ID (optional - if null, unique ID is automatically generated)
     * @param operationData operation data
     * @param params        list of generic parameters
     * @return a Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationId, String operationData, List<KeyValueParameter> params) throws NextStepServiceException {
        return createOperation(operationName, operationId, operationData, null, params);
    }

    /**
     * Calls the operation endpoint via POST method to create a new operation.
     *
     * @param operationName operation name
     * @param operationData operation data
     * @param params        list of generic parameters
     * @return a Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationData, List<KeyValueParameter> params) throws NextStepServiceException {
        return createOperation(operationName, null, operationData, null, params);
    }


    /**
     * Calls the operation endpoint via POST method to create a new operation.
     *
     * @param operationName operation name
     * @param operationId   operation ID (optional - if null, unique ID is automatically generated)
     * @param operationData operation data
     * @param formData operation form data, such as title, message and displayable attributes
     * @param params        list of generic parameters
     * @return a Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationId, String operationData, OperationFormData formData, List<KeyValueParameter> params) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            CreateOperationRequest request = new CreateOperationRequest();
            request.setOperationName(operationName);
            request.setOperationId(operationId);
            request.setOperationData(operationData);
            request.setFormData(formData);
            if (params != null) {
                request.getParams().addAll(params);
            }
            HttpEntity<ObjectRequest<CreateOperationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<CreateOperationResponse>> response = defaultTemplate().exchange(serviceUrl + "/operation", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<CreateOperationResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Calls the operation endpoint via POST method to create a new operation.
     *
     * @param operationName operation name
     * @param operationData operation data
     * @param formData operation form data, such as title, message and displayable attributes
     * @param params        list of generic parameters
     * @return a Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public ObjectResponse<CreateOperationResponse> createOperation(String operationName, String operationData, OperationFormData formData, List<KeyValueParameter> params) throws NextStepServiceException {
        return createOperation(operationName, null, operationData, formData, params);
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
    public ObjectResponse<UpdateOperationResponse> updateOperation(String operationId, String userId, AuthMethod authMethod, AuthStepResult authStepResult, String authStepResultDescription, List<KeyValueParameter> params) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateOperationRequest request = new UpdateOperationRequest();
            request.setOperationId(operationId);
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            request.setAuthStepResult(authStepResult);
            request.setAuthStepResultDescription(authStepResultDescription);
            if (params != null) {
                request.getParams().addAll(params);
            }
            HttpEntity<ObjectRequest<UpdateOperationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UpdateOperationResponse>> response = defaultTemplate().exchange(serviceUrl + "/operation", HttpMethod.PUT, entity, new ParameterizedTypeReference<ObjectResponse<UpdateOperationResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    public ObjectResponse updateOperationFormData(String operationId, OperationFormData formData) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            UpdateFormDataRequest request = new UpdateFormDataRequest();
            request.setOperationId(operationId);
            request.setFormData(formData);
            HttpEntity<ObjectRequest<UpdateFormDataRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = defaultTemplate().exchange(serviceUrl + "/operation/formData", HttpMethod.PUT, entity, new ParameterizedTypeReference<ObjectResponse>() {
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
     * @param id operation id
     * @return a Response with {@link GetOperationDetailResponse} object for OK status or
     * {@link io.getlime.core.rest.model.base.entity.Error} for ERROR status
     */
    public ObjectResponse<GetOperationDetailResponse> getOperationDetail(String id) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetOperationDetailRequest request = new GetOperationDetailRequest();
            request.setOperationId(id);
            HttpEntity<ObjectRequest<GetOperationDetailRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetOperationDetailResponse>> response = defaultTemplate().exchange(serviceUrl + "/operation/detail", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetOperationDetailResponse>>() {});
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            // Next Step service is down
            throw handleResourceAccessError(ex);
        }
    }

    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(String userId) throws NextStepServiceException {
        return getPendingOperations(userId, null);
    }

    /**
     * Calls the get pending operations endpoint via METHOD to get a list of pending operations.
     * @param userId user id
     * @param authMethod authentication method
     * @return a Response with list of {@link GetOperationDetailResponse} for OK status
     * @throws NextStepServiceException Exception with {@link Error} for ERROR status
     */
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetPendingOperationsRequest request = new GetPendingOperationsRequest();
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            HttpEntity<ObjectRequest<GetPendingOperationsRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<List<GetOperationDetailResponse>>> response = defaultTemplate().exchange(serviceUrl + "/user/operation/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<List<GetOperationDetailResponse>>>() {
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
     * @throws NextStepServiceException Exception with {@link Error} for ERROR status
     */
    public ObjectResponse<GetAuthMethodsResponse> getAuthMethods() throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetAuthMethodsRequest request = new GetAuthMethodsRequest();
            HttpEntity<ObjectRequest<GetAuthMethodsRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/auth-method/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetAuthMethodsResponse>>() {
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
     * @param userId User ID
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Exception with {@link Error} for ERROR status
     */
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsEnabledForUser(String userId) throws NextStepServiceException {
        try {
            // Exchange next step request with NextStep server.
            GetUserAuthMethodsRequest request = new GetUserAuthMethodsRequest();
            request.setUserId(userId);
            HttpEntity<ObjectRequest<GetUserAuthMethodsRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GetUserAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/user/auth-method/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetUserAuthMethodsResponse>>() {
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
     * @param userId     User ID
     * @param authMethod Authentication method
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Exception with {@link Error} for ERROR status
     */
    public ObjectResponse<GetAuthMethodsResponse> enableAuthMethodForUser(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            HttpEntity<ObjectRequest<UpdateAuthMethodRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            // Exchange next step request with NextStep server.
            ResponseEntity<ObjectResponse<GetAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/user/auth-method", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<GetAuthMethodsResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
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
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodsResponse.
     * @throws NextStepServiceException Exception with {@link Error} for ERROR status
     */
    public ObjectResponse<GetAuthMethodsResponse> disableAuthMethodForUser(String userId, AuthMethod authMethod) throws NextStepServiceException {
        try {
            UpdateAuthMethodRequest request = new UpdateAuthMethodRequest();
            request.setUserId(userId);
            request.setAuthMethod(authMethod);
            HttpEntity<ObjectRequest<UpdateAuthMethodRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            // Exchange next step request with NextStep server.
            ResponseEntity<ObjectResponse<GetAuthMethodsResponse>> response = defaultTemplate().exchange(serviceUrl + "/user/auth-method", HttpMethod.DELETE, entity, new ParameterizedTypeReference<ObjectResponse<GetAuthMethodsResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
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
        Error error = new Error(Error.Code.ERROR_GENERIC, ex.getMessage());
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
            TypeReference<ObjectResponse<Error>> typeReference = new TypeReference<ObjectResponse<Error>>() {};
            ObjectResponse<Error> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
            Error error = errorResponse.getResponseObject();
            switch (error.getCode()) {
                case OperationAlreadyFinishedException.CODE:
                    throw new OperationAlreadyFinishedException(error.getMessage());
                case OperationAlreadyFailedException.CODE:
                    throw new OperationAlreadyFailedException(error.getMessage());
                default:
                    return new NextStepServiceException(ex, error);
            }
        } catch (IOException ex2) {
            // JSON parsing failed
            Error error = new Error(Error.Code.ERROR_GENERIC, ex2.getMessage());
            return new NextStepServiceException(ex, error);
        }
    }

}
