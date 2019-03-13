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

package io.getlime.security.powerauth.lib.dataadapter.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.DataAdapterError;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.dataadapter.model.request.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.DecorateOperationFormDataResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Authentication services provides services for communication with the Data Adapter.
 * It uses the RestTemplate class to handle REST API calls. HTTP client is used instead of default client
 * so that error responses contain full response bodies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DataAdapterClient {

    private final String serviceUrl;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * Create a new client with provided base URL.
     * @param serviceUrl Base URL.
     */
    public DataAdapterClient(String serviceUrl) {
        this(serviceUrl, null);
    }

    /**
     * Create a new client with provided base URL and custom object mapper.
     * @param serviceUrl Base URL.
     * @param objectMapper Object mapper.
     */
    public DataAdapterClient(String serviceUrl, ObjectMapper objectMapper) {
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
     * Perform authentication with provided username and password.
     *
     * @param username Username for user who is being authenticated.
     * @param password Password as a string.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return a Response with either AuthenticationResponse or DataAdapterError given the result of the operation.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<AuthenticationResponse> authenticateUser(String username, String password, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            // Exchange authentication request with data adapter.
            AuthenticationRequest request = new AuthenticationRequest(username, password, organizationId, AuthenticationType.BASIC, operationContext);
            HttpEntity<ObjectRequest<AuthenticationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<AuthenticationResponse>> response = restTemplate.exchange(serviceUrl + "/api/auth/user/authenticate", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<AuthenticationResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Obtain user details for given user ID.
     *
     * @param userId User ID for the user to be obtained.
     * @param organizationId Organization ID.
     * @return A response with user details.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<UserDetailResponse> fetchUserDetail(String userId, String organizationId) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            UserDetailRequest request = new UserDetailRequest(userId, organizationId);
            HttpEntity<ObjectRequest<UserDetailRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UserDetailResponse>> response = restTemplate.exchange(serviceUrl + "/api/auth/user/info", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<UserDetailResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Create authorization SMS message with OTP authorization code.
     *
     * @param userId           User ID.
     * @param organizationId   Organization ID.
     * @param operationContext Operation context.
     * @param lang             Language for i18n.
     * @return Response with generated messageId.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<CreateSMSAuthorizationResponse> createAuthorizationSMS(String userId, String organizationId, OperationContext operationContext, String lang) throws DataAdapterClientErrorException {
        try {
            CreateSMSAuthorizationRequest request = new CreateSMSAuthorizationRequest(userId, organizationId, lang, operationContext);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept-Language", LocaleContextHolder.getLocale().getLanguage());
            HttpEntity<ObjectRequest<CreateSMSAuthorizationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request), headers);
            ResponseEntity<ObjectResponse<CreateSMSAuthorizationResponse>> response = restTemplate.exchange(
                    serviceUrl + "/api/auth/sms/create", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<CreateSMSAuthorizationResponse>>() {
                    });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Verify OTP authorization code for previously generated SMS message.
     *
     * @param messageId         Message ID.
     * @param authorizationCode User entered authorization code.
     * @param operationContext  Operation context.
     * @return Empty response returned when action succeeds.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public Response verifyAuthorizationSMS(String messageId, String authorizationCode, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            VerifySMSAuthorizationRequest request = new VerifySMSAuthorizationRequest(messageId, authorizationCode, operationContext);
            HttpEntity<ObjectRequest<VerifySMSAuthorizationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            restTemplate.exchange(serviceUrl + "/api/auth/sms/verify", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new Response();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Decorate operation form data.
     *
     * @param userId User ID of the user for this request.
     * @param organizationId Organization ID for this request.
     * @param operationContext Operation context.
     * @return Decorated operation form data.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<DecorateOperationFormDataResponse> decorateOperationFormData(String userId, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            DecorateOperationFormDataRequest request = new DecorateOperationFormDataRequest(userId, organizationId, operationContext);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept-Language", LocaleContextHolder.getLocale().getLanguage());
            HttpEntity<ObjectRequest<DecorateOperationFormDataRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request), headers);
            ResponseEntity<ObjectResponse<DecorateOperationFormDataResponse>> response = restTemplate.exchange(serviceUrl + "/api/operation/formdata/decorate", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<DecorateOperationFormDataResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Send a notification about form data change.
     *
     * @param formDataChange Operation form data change.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return Object response.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse formDataChangedNotification(FormDataChange formDataChange, String userId, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            FormDataChangeNotificationRequest request = new FormDataChangeNotificationRequest();
            request.setUserId(userId);
            request.setOrganizationId(organizationId);
            request.setOperationContext(operationContext);
            request.setFormDataChange(formDataChange);
            HttpEntity<ObjectRequest<FormDataChangeNotificationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = restTemplate.exchange(serviceUrl + "/api/operation/formdata/change", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Send a notification about operation change.
     *
     * @param operationChange Operation change.
     * @return Object response.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse operationChangedNotification(OperationChange operationChange, String userId, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            // Exchange user details with data adapter.
            OperationChangeNotificationRequest request = new OperationChangeNotificationRequest();
            request.setUserId(userId);
            request.setOrganizationId(organizationId);
            request.setOperationContext(operationContext);
            request.setOperationChange(operationChange);
            HttpEntity<ObjectRequest<OperationChangeNotificationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse> response = restTemplate.exchange(serviceUrl + "/api/operation/change", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Create new DataAdapterClientErrorException from ResourceAccessException.
     * @param ex Exception used when a resource access error occurs.
     * @return Data adapter client exception.
     */
    private DataAdapterClientErrorException resourceAccessException(ResourceAccessException ex) {
        DataAdapterError error = new DataAdapterError(DataAdapterError.Code.COMMUNICATION_ERROR, ex.getMessage());
        return new DataAdapterClientErrorException(ex, error);
    }

    /**
     * Create new DataAdapterClientErrorException from HttpStatusCodeException.
     * @param ex Exception used when an HTTP error occurs.
     * @return Data adapter client exception.
     */
    private DataAdapterClientErrorException httpStatusException(HttpStatusCodeException ex) {
        try {
            TypeReference<ObjectResponse<DataAdapterError>> typeReference = new TypeReference<ObjectResponse<DataAdapterError>>() {
            };
            ObjectResponse<DataAdapterError> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
            DataAdapterError error = errorResponse.getResponseObject();
            if (error.getCode() == null) { // process malformed errors with undefined error code
                error.setCode(DataAdapterError.Code.ERROR_GENERIC);
                error.setMessage(ex.getMessage());
            }
            return new DataAdapterClientErrorException(ex, error);
        } catch (IOException ex2) {
            DataAdapterError error;
            if (ex.getStatusCode() != HttpStatus.OK) {
                error = new DataAdapterError(DataAdapterError.Code.COMMUNICATION_ERROR, "HTTP error occurred: " + ex.getMessage());
                return new DataAdapterClientErrorException(ex, error);
            } else {
                error = new DataAdapterError(DataAdapterError.Code.ERROR_GENERIC, "IO error occurred: " + ex2.getMessage());
                return new DataAdapterClientErrorException(ex2, error);
            }
        }
    }

}
