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
import io.getlime.security.powerauth.lib.dataadapter.model.entity.*;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.request.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.*;
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
import java.util.List;
import java.util.Map;

/**
 * Data Adapter Client provides methods for communication with the Data Adapter.
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
     * Lookup user account.
     *
     * @param username Username for user account which is being looked up.
     * @param organizationId Organization ID for which the user ID is assigned to.
     * @param operationContext Operation context.
     * @return Response with user details.
     * @throws DataAdapterClientErrorException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<UserDetailResponse> lookupUser(String username, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            // Exchange authentication request with data adapter.
            UserLookupRequest request = new UserLookupRequest(username, organizationId, operationContext);
            HttpEntity<ObjectRequest<UserLookupRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UserDetailResponse>> response = restTemplate.exchange(serviceUrl + "/api/auth/user/lookup", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<UserDetailResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Perform authentication with provided username and password.
     *
     * @param userId User ID of user who is being authenticated.
     * @param organizationId Organization ID.
     * @param password Password for this authentication request, optionally encrypted.
     * @param authenticationContext Authentication context.
     * @param operationContext Operation context.
     * @return Authentication response is returned in case of successful authentication.
     * @throws DataAdapterClientErrorException Thrown when client request fails or authentication fails.
     */
    public ObjectResponse<UserAuthenticationResponse> authenticateUser(String userId, String organizationId, String password, AuthenticationContext authenticationContext, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            // Exchange authentication request with data adapter.
            UserAuthenticationRequest request = new UserAuthenticationRequest(userId, organizationId, password, authenticationContext, operationContext);
            HttpEntity<ObjectRequest<UserAuthenticationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<UserAuthenticationResponse>> response = restTemplate.exchange(serviceUrl + "/api/auth/user/authenticate", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<UserAuthenticationResponse>>() {
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
     * @throws DataAdapterClientErrorException Thrown when client request fails or user does not exist.
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
     * @param accountStatus    User account status.
     * @param operationContext Operation context.
     * @param lang             Language for i18n.
     * @param resend           Whether SMS is being resent.
     * @return Response with generated messageId.
     * @throws DataAdapterClientErrorException Thrown when client request fails or SMS could not be delivered.
     */
    public ObjectResponse<CreateSmsAuthorizationResponse> createAuthorizationSms(String userId, String organizationId, AccountStatus accountStatus, OperationContext operationContext, String lang, boolean resend) throws DataAdapterClientErrorException {
        try {
            CreateSmsAuthorizationRequest request = new CreateSmsAuthorizationRequest(userId, organizationId, accountStatus, lang, operationContext, resend);
            HttpEntity<ObjectRequest<CreateSmsAuthorizationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<CreateSmsAuthorizationResponse>> response = restTemplate.exchange(
                    serviceUrl + "/api/auth/sms/create", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<CreateSmsAuthorizationResponse>>() {
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
     * @param userId            User ID.
     * @param organizationId    Organization ID.
     * @param accountStatus     User account status.
     * @param operationContext  Operation context.
     * @return Empty response returned when action succeeds.
     * @throws DataAdapterClientErrorException Thrown when client request fails or SMS code authorization fails.
     */
    public ObjectResponse<VerifySmsAuthorizationResponse> verifyAuthorizationSms(String messageId, String authorizationCode, String userId, String organizationId, AccountStatus accountStatus, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            VerifySmsAuthorizationRequest request = new VerifySmsAuthorizationRequest(messageId, authorizationCode, userId, organizationId, accountStatus, operationContext);
            HttpEntity<ObjectRequest<VerifySmsAuthorizationRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<VerifySmsAuthorizationResponse>> response = restTemplate.exchange(serviceUrl + "/api/auth/sms/verify", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<VerifySmsAuthorizationResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Verify OTP authorization code for previously generated SMS message together with user password.
     *
     * @param messageId Message ID.
     * @param authorizationCode User entered authorization code.
     * @param userId User ID for this authentication request.
     * @param organizationId Organization ID for this authentication request.
     * @param password Password for this authentication request, optionally encrypted.
     * @param accountStatus Current user account status.
     * @param authenticationContext Authentication context.
     * @param operationContext Operation context.
     * @return Empty response returned when action succeeds.
     * @throws DataAdapterClientErrorException Thrown when client request fails or authentication/authorization fails.
     */
    public ObjectResponse<VerifySmsAndPasswordResponse> verifyAuthorizationSmsAndPassword(String messageId, String authorizationCode, String userId, String organizationId, AccountStatus accountStatus, String password, AuthenticationContext authenticationContext, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            VerifySmsAndPasswordRequest request = new VerifySmsAndPasswordRequest(messageId, authorizationCode, userId, organizationId, accountStatus, password, authenticationContext, operationContext);
            HttpEntity<ObjectRequest<VerifySmsAndPasswordRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<VerifySmsAndPasswordResponse>> response = restTemplate.exchange(serviceUrl + "/api/auth/sms/password/verify", HttpMethod.POST, entity, new ParameterizedTypeReference<ObjectResponse<VerifySmsAndPasswordResponse>>() {
            });
            return new ObjectResponse<>(response.getBody().getResponseObject());
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
            HttpEntity<ObjectRequest<DecorateOperationFormDataRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
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
     * Initialize OAuth 2.0 consent form.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @return Response with information whether consent form should be displayed
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<InitConsentFormResponse> initConsentForm(String userId, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        try {
            InitConsentFormRequest request = new InitConsentFormRequest(userId, organizationId, operationContext);
            HttpEntity<ObjectRequest<InitConsentFormRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<InitConsentFormResponse>> response = restTemplate.exchange(
                    serviceUrl + "/api/auth/consent/init", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<InitConsentFormResponse>>() {
                    });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Create OAuth 2.0 consent form.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param lang Language of the text in the consent form.
     * @return Consent form with text and options to select by the user.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<CreateConsentFormResponse> createConsentForm(String userId, String organizationId, OperationContext operationContext, String lang) throws DataAdapterClientErrorException {
        try {
            CreateConsentFormRequest request = new CreateConsentFormRequest(userId, organizationId, lang, operationContext);
            HttpEntity<ObjectRequest<CreateConsentFormRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<CreateConsentFormResponse>> response = restTemplate.exchange(
                    serviceUrl + "/api/auth/consent/create", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<CreateConsentFormResponse>>() {
                    });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Validate options selected by the user in the OAuth 2.0 consent form.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param lang Language of the text in the consent form.
     * @param options Consent options selected by the user.
     * @return Consent form validation result.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<ValidateConsentFormResponse> validateConsentForm(String userId, String organizationId, OperationContext operationContext, String lang, List<ConsentOption> options) throws DataAdapterClientErrorException {
        try {
            ValidateConsentFormRequest request = new ValidateConsentFormRequest(userId, organizationId, operationContext, lang, options);
            HttpEntity<ObjectRequest<ValidateConsentFormRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<ValidateConsentFormResponse>> response = restTemplate.exchange(
                    serviceUrl + "/api/auth/consent/validate", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<ValidateConsentFormResponse>>() {
                    });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Save options selected by the user in the OAuth 2.0 consent form.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param options Consent options selected by the user.
     * @return Response with indication whether consent form was successfully saved.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<SaveConsentFormResponse> saveConsentForm(String userId, String organizationId, OperationContext operationContext, List<ConsentOption> options) throws DataAdapterClientErrorException {
        try {
            SaveConsentFormRequest request = new SaveConsentFormRequest(userId, organizationId, operationContext, options);
            HttpEntity<ObjectRequest<SaveConsentFormRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<SaveConsentFormResponse>> response = restTemplate.exchange(
                    serviceUrl + "/api/auth/consent/save", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<SaveConsentFormResponse>>() {
                    });
            return new ObjectResponse<>(response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Execute an anti-fraud system action with information about current step and retrieve response which can override
     * authentication instruments used in current authentication step.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param afsRequestParameters Request parameters for AFS.
     * @param extras Extra parameters for AFS.
     * @return Response with indication whether consent form was successfully saved.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<AfsResponse> executeAfsAction(String userId, String organizationId, OperationContext operationContext, AfsRequestParameters afsRequestParameters, Map<String, Object> extras) throws DataAdapterClientErrorException {
        try {
            AfsRequest request = new AfsRequest(userId, organizationId, operationContext, afsRequestParameters, extras);
            HttpEntity<ObjectRequest<AfsRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<AfsResponse>> response = restTemplate.exchange(
                    serviceUrl + "/api/afs/action/execute", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ObjectResponse<AfsResponse>>() {
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
            if (error == null) {
                error = new DataAdapterError();
            }
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
