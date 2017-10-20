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

package io.getlime.security.powerauth.lib.dataadapter.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.DataAdapterError;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.dataadapter.model.request.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.BankAccountListResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Authentication services provides services for communication with the Data Adapter.
 * It uses the RestTemplate class to handle REST API calls. HTTP client is used instead of default client
 * so that error responses contain full response bodies.
 *
 * @author Roman Strobl
 */
public class DataAdapterClient {

    private String serviceUrl;
    private ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public DataAdapterClient() {
    }

    /**
     * Create a new client with provided base URL.
     * @param serviceUrl Base URL.
     */
    public DataAdapterClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a new client with provided base URL and custom object mapper.
     * @param serviceUrl Base URL.
     * @param objectMapper Object mapper.
     */
    public DataAdapterClient(String serviceUrl, ObjectMapper objectMapper) {
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
     * Perform authentication with provided username and password.
     *
     * @param username username for user who is being authenticated
     * @param password password as a string
     * @return a Response with either AuthenticationResponse or DataAdapterError given the result of the operation
     */
    public ObjectResponse<AuthenticationResponse> authenticateUser(String username, String password) throws DataAdapterClientErrorException {
        try {
            // Exchange authentication request with data adapter.
            AuthenticationRequest request = new AuthenticationRequest(username, password, AuthenticationType.BASIC);
            HttpEntity<ObjectRequest<AuthenticationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<AuthenticationResponse>> response = defaultTemplate().exchange(serviceUrl + "/api/auth/user/authenticate", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<AuthenticationResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);
            }
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Obtain user details based on user info.
     *
     * @param userId User ID for the user to be obtained.
     * @return A response user with given ID.
     */
    public ObjectResponse<UserDetailResponse> fetchUserDetail(String userId) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            UserDetailRequest request = new UserDetailRequest(userId);
            HttpEntity<ObjectRequest<UserDetailRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UserDetailResponse>> response = defaultTemplate().exchange(serviceUrl + "/api/auth/user/info", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<UserDetailResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);

            }
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Create authorization SMS OTP message.
     *
     * @param operationId   Operation ID.
     * @param userId        User ID.
     * @param operationName Operation name.
     * @param formData      Operation form data.
     * @param lang          language for i18n.
     * @return Response with generated messageId.
     * @throws DataAdapterClientErrorException Exception thrown when action fails.
     */
    public ObjectResponse<CreateSMSAuthorizationResponse> createAuthorizationSMS(String operationId, String userId, String operationName, OperationFormData formData, String lang) throws DataAdapterClientErrorException {
        try {
            CreateSMSAuthorizationRequest request = new CreateSMSAuthorizationRequest(operationId, userId, operationName, formData, lang);
            HttpEntity<ObjectRequest<CreateSMSAuthorizationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<CreateSMSAuthorizationResponse>> response = defaultTemplate().exchange(
                    serviceUrl + "/api/auth/sms/create", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<CreateSMSAuthorizationResponse>>() {
                    });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);
            }
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Verify authorization code for previously generated SMS OTP message.
     *
     * @param messageId         Message ID.
     * @param authorizationCode User entered authorization code.
     * @return Empty response returned when action succeeds.
     * @throws DataAdapterClientErrorException Exception is thrown when action fails with error details.
     */
    public ObjectResponse verifyAuthorizationSMS(String messageId, String authorizationCode) throws DataAdapterClientErrorException {
        try {
            VerifySMSAuthorizationRequest request = new VerifySMSAuthorizationRequest(messageId, authorizationCode);
            HttpEntity<ObjectRequest<VerifySMSAuthorizationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            defaultTemplate().exchange(serviceUrl + "/api/auth/sms/verify", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse();
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);
            }
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Obtain bank account list for given user.
     *
     * @param userId User ID of the user for this request.
     * @return A list of bank accounts for given user.
     */
    public ObjectResponse<BankAccountListResponse> fetchBankAccounts(String userId) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            BankAccountListRequest request = new BankAccountListRequest(userId);
            HttpEntity<ObjectRequest<BankAccountListRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<BankAccountListResponse>> response = defaultTemplate().exchange(serviceUrl + "/api/auth/account/list", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<BankAccountListResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);

            }
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Send a notification about formData change.
     *
     * @param formDataChange Operation formData change.
     * @return Object response.
     */
    public ObjectResponse formDataChangedNotification(FormDataChange formDataChange, String userId, String operationId) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            FormDataChangeNotificationRequest request = new FormDataChangeNotificationRequest();
            request.setUserId(userId);
            request.setOperationId(operationId);
            request.setFormDataChange(formDataChange);
            HttpEntity<ObjectRequest<FormDataChangeNotificationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = defaultTemplate().exchange(serviceUrl + "/api/operation/formData/change", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);

            }
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Send a notification about operation change.
     *
     * @param operationChange Operation change.
     * @return Object response.
     */
    public ObjectResponse operationChangedNotification(OperationChange operationChange, String userId, String operationId) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            OperationChangeNotificationRequest request = new OperationChangeNotificationRequest();
            request.setUserId(userId);
            request.setOperationId(operationId);
            request.setOperationChange(operationChange);
            HttpEntity<ObjectRequest<OperationChangeNotificationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = defaultTemplate().exchange(serviceUrl + "/api/operation/change", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);

            }
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    private DataAdapterClientErrorException resourceAccessException(ResourceAccessException ex) throws DataAdapterClientErrorException {
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.ERROR_GENERIC, ex.getMessage());
        return new DataAdapterClientErrorException(ex, error);
    }

    private DataAdapterClientErrorException invalidErrorResponseBodyException(IOException ex) throws DataAdapterClientErrorException {
        // JSON parsing failed
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.ERROR_GENERIC, ex.getMessage());
        return new DataAdapterClientErrorException(ex, error);
    }

    private DataAdapterClientErrorException httpStatusException(HttpStatusCodeException ex) throws IOException, DataAdapterClientErrorException {
        TypeReference<ObjectResponse<DataAdapterError>> typeReference = new TypeReference<ObjectResponse<DataAdapterError>>() {
        };
        ObjectResponse<DataAdapterError> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
        DataAdapterError error = errorResponse.getResponseObject();
        if (error.getCode() == null) { // process malformed errors with undefined error code
            error.setCode(DataAdapterError.Code.ERROR_GENERIC);
            error.setMessage(ex.getMessage());
        }
        return new DataAdapterClientErrorException(ex, error);
    }

}
