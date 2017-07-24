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

package io.getlime.security.powerauth.lib.credentials.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.lib.credentials.model.entity.CredentialStoreError;
import io.getlime.security.powerauth.lib.credentials.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.credentials.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.credentials.model.request.UserDetailRequest;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.credentials.model.response.UserDetailResponse;
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
 * Authentication services provides services for communication with the Credential server.
 * It uses the RestTemplate class to handle REST API calls. HTTP client is used instead of default client
 * so that error responses contain full response bodies.
 *
 * @author Roman Strobl
 */
public class CredentialStoreClient {

    private String serviceUrl;
    private ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public CredentialStoreClient() {
    }

    /**
     * Create a new client with provided base URL.
     * @param serviceUrl Base URL.
     */
    public CredentialStoreClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Create a new client with provided base URL and custom object mapper.
     * @param serviceUrl Base URL.
     * @param objectMapper Object mapper.
     */
    public CredentialStoreClient(String serviceUrl, ObjectMapper objectMapper) {
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
     * @return a Response with either AuthenticationResponse or CredentialStoreError given the result of the operation
     */
    public ObjectResponse<AuthenticationResponse> authenticate(String username, String password) throws CredentialStoreClientErrorException {
        try {
            // Exchange authentication request with credential server.
            AuthenticationRequest request = new AuthenticationRequest(username, password, AuthenticationType.BASIC);
            HttpEntity<ObjectRequest<AuthenticationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<AuthenticationResponse>> response = defaultTemplate().exchange(serviceUrl + "/authenticate", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<AuthenticationResponse>>() {});
            return new ObjectResponse<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);
            }
        } catch (ResourceAccessException ex) { // Credential service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Obtain user details based on user info.
     *
     * @param userId User ID for the user to be obtained.
     * @return A response user with given ID.
     */
    public ObjectResponse<UserDetailResponse> fetchUserDetail(String userId) throws CredentialStoreClientErrorException {
        try {
            // Exchange user details with credential server.
            UserDetailRequest request = new UserDetailRequest(userId);
            HttpEntity<ObjectRequest<UserDetailRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UserDetailResponse>> response = defaultTemplate().exchange(serviceUrl + "/userInfo", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<UserDetailResponse>>() {});
            return new ObjectResponse<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                throw httpStatusException(ex);
            } catch (IOException ex2) { // JSON parsing failed
                throw invalidErrorResponseBodyException(ex2);

            }
        } catch (ResourceAccessException ex) { // Credential service is down
            throw resourceAccessException(ex);
        }
    }

    private CredentialStoreClientErrorException resourceAccessException(ResourceAccessException ex) throws CredentialStoreClientErrorException {
        CredentialStoreError error = new CredentialStoreError(CredentialStoreError.Code.ERROR_GENERIC, ex.getMessage());
        return new CredentialStoreClientErrorException(ex, error);
    }

    private CredentialStoreClientErrorException invalidErrorResponseBodyException(IOException ex) throws CredentialStoreClientErrorException {
        // JSON parsing failed
        CredentialStoreError error = new CredentialStoreError(CredentialStoreError.Code.ERROR_GENERIC, ex.getMessage());
        return new CredentialStoreClientErrorException(ex, error);
    }

    private CredentialStoreClientErrorException httpStatusException(HttpStatusCodeException ex) throws IOException, CredentialStoreClientErrorException {
        TypeReference<ObjectResponse<CredentialStoreError>> typeReference = new TypeReference<ObjectResponse<CredentialStoreError>>() {};
        ObjectResponse<CredentialStoreError> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
        CredentialStoreError error = errorResponse.getResponseObject();
        if (error.getCode() == null) { // process malformed errors with undefined error code
            error.setCode(CredentialStoreError.Code.ERROR_GENERIC);
            error.setMessage(ex.getMessage());
        }
        return new CredentialStoreClientErrorException(ex, error);
    }

}
