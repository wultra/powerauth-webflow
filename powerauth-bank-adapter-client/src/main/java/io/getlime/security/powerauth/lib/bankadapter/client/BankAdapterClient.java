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

package io.getlime.security.powerauth.lib.bankadapter.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.bankadapter.model.entity.BankAdapterError;
import io.getlime.security.powerauth.lib.bankadapter.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.bankadapter.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.bankadapter.model.request.CreateSMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.bankadapter.model.request.UserDetailRequest;
import io.getlime.security.powerauth.lib.bankadapter.model.request.VerifySMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.bankadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.bankadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.bankadapter.model.response.UserDetailResponse;
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
 * Authentication services provides services for communication with the Bank Adapter.
 * It uses the RestTemplate class to handle REST API calls. HTTP client is used instead of default client
 * so that error responses contain full response bodies.
 *
 * @author Roman Strobl
 */
public class BankAdapterClient {

    private String serviceUrl;
    private ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public BankAdapterClient() {
    }

    /**
     * Create a new client with provided base URL.
     * @param serviceUrl Base URL.
     */
    public BankAdapterClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a new client with provided base URL and custom object mapper.
     * @param serviceUrl Base URL.
     * @param objectMapper Object mapper.
     */
    public BankAdapterClient(String serviceUrl, ObjectMapper objectMapper) {
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
     * @return a Response with either AuthenticationResponse or BankAdapterError given the result of the operation
     */
    public ObjectResponse<AuthenticationResponse> authenticate(String username, String password) throws BankAdapterClientErrorException {
        try {
            // Exchange authentication request with bank adapter.
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
        } catch (ResourceAccessException ex) { // Bank Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Obtain user details based on user info.
     *
     * @param userId User ID for the user to be obtained.
     * @return A response user with given ID.
     */
    public ObjectResponse<UserDetailResponse> fetchUserDetail(String userId) throws BankAdapterClientErrorException {
        try {
            // Exchange user details with bank adapter.
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
        } catch (ResourceAccessException ex) { // Bank Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Create authorization SMS OTP message.
     *
     * @param userId        User ID.
     * @param operationName Operation name.
     * @param operationData Operation data in JSON format.
     * @param lang          language for i18n.
     * @return Response with generated messageId.
     * @throws BankAdapterClientErrorException Exception thrown when action fails.
     */
    public ObjectResponse<CreateSMSAuthorizationResponse> createAuthorizationSMS(String userId, String operationName, String operationData, String lang) throws BankAdapterClientErrorException {
        try {
            JsonNode operationDataJson = objectMapper.readTree(operationData);
            CreateSMSAuthorizationRequest request = new CreateSMSAuthorizationRequest(userId, operationName, operationDataJson, lang);
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
        } catch (ResourceAccessException ex) { // Bank Adapter service is down
            throw resourceAccessException(ex);
        } catch (IOException ex) {
            throw ioException(ex);
        }
    }

    /**
     * Verify authorization code for previously generated SMS OTP message.
     *
     * @param messageId         Message ID.
     * @param authorizationCode User entered authorization code.
     * @return Empty response returned when action succeeds.
     * @throws BankAdapterClientErrorException Exception is thrown when action fails with error details.
     */
    public ObjectResponse verifyAuthorizationSMS(String messageId, String authorizationCode) throws BankAdapterClientErrorException {
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
        } catch (ResourceAccessException ex) { // Bank Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    private BankAdapterClientErrorException resourceAccessException(ResourceAccessException ex) throws BankAdapterClientErrorException {
        BankAdapterError error = new BankAdapterError(BankAdapterError.Code.ERROR_GENERIC, ex.getMessage());
        return new BankAdapterClientErrorException(ex, error);
    }

    private BankAdapterClientErrorException invalidErrorResponseBodyException(IOException ex) throws BankAdapterClientErrorException {
        // JSON parsing failed
        BankAdapterError error = new BankAdapterError(BankAdapterError.Code.ERROR_GENERIC, ex.getMessage());
        return new BankAdapterClientErrorException(ex, error);
    }

    private BankAdapterClientErrorException ioException(IOException ex) throws BankAdapterClientErrorException {
        BankAdapterError error = new BankAdapterError(BankAdapterError.Code.ERROR_GENERIC, ex.getMessage());
        return new BankAdapterClientErrorException(ex, error);
    }

    private BankAdapterClientErrorException httpStatusException(HttpStatusCodeException ex) throws IOException, BankAdapterClientErrorException {
        TypeReference<ObjectResponse<BankAdapterError>> typeReference = new TypeReference<ObjectResponse<BankAdapterError>>() {
        };
        ObjectResponse<BankAdapterError> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
        BankAdapterError error = errorResponse.getResponseObject();
        if (error.getCode() == null) { // process malformed errors with undefined error code
            error.setCode(BankAdapterError.Code.ERROR_GENERIC);
            error.setMessage(ex.getMessage());
        }
        return new BankAdapterClientErrorException(ex, error);
    }

}
