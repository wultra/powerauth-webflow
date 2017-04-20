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
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
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
            ResponseEntity<Response<UpdateOperationResponse>> response = defaultTemplate().exchange(serviceUrl + "/operation", HttpMethod.PUT, entity, new ParameterizedTypeReference<Response<UpdateOperationResponse>>() {});
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw handleHttpError(ex);
        } catch (ResourceAccessException ex) {
            throw handleResourceAccessError(ex);
        }
    }

    /**
     * Handle resource access error (i.e. server not available).
     * @param ex
     * @return
     * @throws NextStepServiceException
     */
    private NextStepServiceException handleResourceAccessError(ResourceAccessException ex) throws NextStepServiceException {
        ErrorModel error = new ErrorModel();
        error.setCode(ErrorModel.Code.ERROR_GENERIC);
        error.setMessage(ex.getMessage());
        return new NextStepServiceException(ex, error);
    }

    /**
     * Handle HTTP error.
     * @param ex
     * @return
     * @throws NextStepServiceException
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
