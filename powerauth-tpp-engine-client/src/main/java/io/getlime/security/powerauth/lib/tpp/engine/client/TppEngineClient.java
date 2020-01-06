/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.tpp.engine.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.tppengine.model.entity.TppEngineError;
import io.getlime.security.powerauth.app.tppengine.model.request.CreateTppAppRequest;
import io.getlime.security.powerauth.app.tppengine.model.request.GiveConsentRequest;
import io.getlime.security.powerauth.app.tppengine.model.request.RemoveConsentRequest;
import io.getlime.security.powerauth.app.tppengine.model.response.ConsentDetailResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.GiveConsentResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.TppAppDetailResponse;
import io.getlime.security.powerauth.app.tppengine.model.response.UserConsentDetailResponse;
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
import java.util.*;

/**
 * TPP Engine Client provides methods for communication with the TPP registry and consent engine.
 * It uses the RestTemplate class to handle REST API calls. Apache HTTP client is used instead of default client
 * so that error responses contain full response bodies.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppEngineClient {

    private final String serviceUrl;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * Create a new client with provided base URL.
     *
     * @param serviceUrl Base URL.
     */
    public TppEngineClient(String serviceUrl) {
        this(serviceUrl, null);
    }

    /**
     * Create a new client with provided base URL and custom object mapper.
     *
     * @param serviceUrl Base URL.
     * @param objectMapper Object mapper.
     */
    public TppEngineClient(String serviceUrl, ObjectMapper objectMapper) {
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
     *
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
     *
     * @return RestTemplate with default configuration.
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }


    /**
     * Lookup consent details (details of a consent template).
     *
     * @param id Username for user account which is being looked up.
     * @return Response with user details.
     * @throws TppEngineClientException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<ConsentDetailResponse> consentDetail(String id) throws TppEngineClientException {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put("id", id);
            ResponseEntity<ObjectResponse<ConsentDetailResponse>> response = restTemplate.exchange(
                    serviceUrl + "/consent?id={id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ObjectResponse<ConsentDetailResponse>>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Lookup consent status for given user and app.
     *
     * @param userId Username for user account which is being looked up.
     * @param consentId Identifier of a consent.
     * @param clientId Identifier of a TPP app.
     * @return Response with details of consent for given user and TPP app.
     * @throws TppEngineClientException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<UserConsentDetailResponse> consentStatus(String userId, String consentId, String clientId) throws TppEngineClientException {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put("userId", userId);
            params.put("consentId", consentId);
            params.put("clientId", clientId);
            ResponseEntity<ObjectResponse<UserConsentDetailResponse>> response = restTemplate.exchange(
                    serviceUrl + "/user/consent/status?userId={userId}&consentId={consentId}&clientId={clientId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ObjectResponse<UserConsentDetailResponse>>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Give consent according to request parameters.
     *
     * @param request Consent information.
     * @return Information about newly created consent.
     * @throws TppEngineClientException Thrown when client request fails or authentication fails.
     */
    public ObjectResponse<GiveConsentResponse> giveConsent(GiveConsentRequest request) throws TppEngineClientException {
        try {
            HttpEntity<ObjectRequest<GiveConsentRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<GiveConsentResponse>> response = restTemplate.exchange(
                    serviceUrl + "/user/consent",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ObjectResponse<GiveConsentResponse>>() {});
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Reject consent according to request parameters.
     *
     * @param request Consent information.
     * @return Static OK response. In case consent didn't exist, this operation is no-op.
     * @throws TppEngineClientException Thrown when client request fails or authentication fails.
     */
    public Response rejectConsent(RemoveConsentRequest request) throws TppEngineClientException {
        try {
            HttpEntity<ObjectRequest<RemoveConsentRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<Response> response = restTemplate.exchange(
                    serviceUrl + "/user/consent",
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<Response>() {});
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Lookup information about a provided app.
     *
     * @param clientId Identifier of a TPP app.
     * @return Response with details TPP app with given client ID.
     * @throws TppEngineClientException Thrown when client request fails or app does not exist.
     */
    public ObjectResponse<TppAppDetailResponse> fetchAppInfo(String clientId) throws TppEngineClientException {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put("clientId", clientId);
            ResponseEntity<ObjectResponse<TppAppDetailResponse>> response = restTemplate.exchange(
                    serviceUrl + "/tpp/app?clientId={clientId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ObjectResponse<TppAppDetailResponse>>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Lookup information about a provided app.
     *
     * @param clientId Identifier of a TPP app.
     * @param tppLicense TPP license information.
     * @return Response with details TPP app with given client ID.
     * @throws TppEngineClientException Thrown when client request fails or app does not exist.
     */
    public ObjectResponse<TppAppDetailResponse> fetchAppInfoWithLicenseRestriction(String clientId, String tppLicense) throws TppEngineClientException {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("tppLicense", tppLicense);
            ResponseEntity<ObjectResponse<TppAppDetailResponse>> response = restTemplate.exchange(
                    serviceUrl + "/tpp/app?clientId={clientId}&tppLicense={tppLicense}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ObjectResponse<TppAppDetailResponse>>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Fetch list of TPP applications based on the license info.
     *
     * @param tppLicense TPP license information.
     * @return Response with details TPP app with given client ID.
     * @throws TppEngineClientException Thrown when client request fails or app does not exist.
     */
    public ObjectResponse<List<TppAppDetailResponse>> fetchApplicationList(String tppLicense) throws TppEngineClientException {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put("tppLicense", tppLicense);
            ResponseEntity<ObjectResponse<List<TppAppDetailResponse>>> response = restTemplate.exchange(
                    serviceUrl + "/tpp/app/list?tppLicense={tppLicense}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ObjectResponse<List<TppAppDetailResponse>>>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Create an application with provided information.
     *
     * @param request New application information.
     * @return Information about newly created application.
     *
     */
    public ObjectResponse<TppAppDetailResponse> createApplication(CreateTppAppRequest request) throws TppEngineClientException {
        try {
            HttpEntity<ObjectRequest<CreateTppAppRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            ResponseEntity<ObjectResponse<TppAppDetailResponse>> response = restTemplate.exchange(
                    serviceUrl + "/tpp/app",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ObjectResponse<TppAppDetailResponse>>() {});
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Update an application with provided client ID with a new information.
     *
     * @param clientId Client ID of an app to be updated.
     * @param request New application information.
     * @return Information about newly created application.
     *
     */
    public ObjectResponse<TppAppDetailResponse> updateApplication(String clientId, CreateTppAppRequest request) throws TppEngineClientException {
        try {
            HttpEntity<ObjectRequest<CreateTppAppRequest>> entity = new HttpEntity<>(new ObjectRequest<>(request));
            final Map<String, String> params = new HashMap<>();
            params.put("clientId", clientId);
            ResponseEntity<ObjectResponse<TppAppDetailResponse>> response = restTemplate.exchange(
                    serviceUrl + "/tpp/app?clientId={clientId}",
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<ObjectResponse<TppAppDetailResponse>>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Renew the client secret for the application.
     *
     * @param clientId Client ID of an application to be refreshed.
     * @param tppLicense License information of TPP.
     * @return Information about newly created application.
     *
     */
    public ObjectResponse<TppAppDetailResponse> renewClientSecret(String clientId, String tppLicense) throws TppEngineClientException {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("tppLicense", tppLicense);
            ResponseEntity<ObjectResponse<TppAppDetailResponse>> response = restTemplate.exchange(
                    serviceUrl + "/tpp/app/renewSecret?clientId={clientId}&tppLicense={tppLicense}",
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ObjectResponse<TppAppDetailResponse>>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Delete an application with provided information.
     *
     * @param clientId Client ID of the app.
     * @param tppLicense License information of TPP.
     * @return Generic response.
     *
     */
    public Response deleteApplication(String clientId, String tppLicense) throws TppEngineClientException {
        try {
            final Map<String, String> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("tppLicense", tppLicense);
            ResponseEntity<Response> response = restTemplate.exchange(
                    serviceUrl + "/tpp/app?clientId={clientId}&tppLicense={tppLicense}",
                    HttpMethod.DELETE,
                    null,
                    new ParameterizedTypeReference<Response>() {},
                    params
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw httpStatusException(ex);
        } catch (ResourceAccessException ex) { // Data Adapter service is down
            throw resourceAccessException(ex);
        }
    }

    /**
     * Create new TppEngineClientException from ResourceAccessException.
     *
     * @param ex Exception used when a resource access error occurs.
     * @return Data adapter client exception.
     */
    private TppEngineClientException resourceAccessException(ResourceAccessException ex) {
        TppEngineError error = new TppEngineError(TppEngineError.Code.COMMUNICATION_ERROR, ex.getMessage());
        return new TppEngineClientException(ex, error);
    }

    /**
     * Create new {@link TppEngineClientException} from HttpStatusCodeException.
     *
     * @param ex Exception used when an HTTP error occurs.
     * @return Data adapter client exception.
     */
    private TppEngineClientException httpStatusException(HttpStatusCodeException ex) {
        try {
            TypeReference<ObjectResponse<TppEngineError>> typeReference = new TypeReference<ObjectResponse<TppEngineError>>() {
            };
            ObjectResponse<TppEngineError> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
            if (errorResponse == null) {
                TppEngineError error = new TppEngineError(TppEngineError.Code.ERROR_GENERIC, "IO error occurred: " + ex.getMessage());
                return new TppEngineClientException(ex, error);
            }
            TppEngineError error = errorResponse.getResponseObject();
            if (error == null) {
                error = new TppEngineError();
            }
            if (error.getCode() == null) { // process malformed errors with undefined error code
                error.setCode(TppEngineError.Code.ERROR_GENERIC);
                error.setMessage(ex.getMessage());
            }
            return new TppEngineClientException(ex, error);
        } catch (IOException ex2) {
            TppEngineError error;
            if (ex.getStatusCode() != HttpStatus.OK) {
                error = new TppEngineError(TppEngineError.Code.COMMUNICATION_ERROR, "HTTP error occurred: " + ex.getMessage());
                return new TppEngineClientException(ex, error);
            } else {
                error = new TppEngineError(TppEngineError.Code.ERROR_GENERIC, "IO error occurred: " + ex2.getMessage());
                return new TppEngineClientException(ex2, error);
            }
        }
    }

}