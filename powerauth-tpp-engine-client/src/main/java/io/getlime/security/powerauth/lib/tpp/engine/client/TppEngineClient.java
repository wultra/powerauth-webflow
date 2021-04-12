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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.core.rest.client.base.RestClientException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;

/**
 * TPP Engine Client provides methods for communication with the TPP registry and consent engine.
 * It uses the Rest Client to handle REST API calls.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class TppEngineClient {

    private static final Logger logger = LoggerFactory.getLogger(TppEngineClient.class);

    private final RestClient restClient;

    /**
     * Create a new client with provided base URL.
     *
     * @param serviceUrl Base service URL.
     * @throws TppEngineClientException Thrown when REST client initialization fails.
     */
    public TppEngineClient(String serviceUrl) throws TppEngineClientException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RestClientConfiguration config = new RestClientConfiguration();
            config.setBaseUrl(serviceUrl);
            config.setObjectMapper(objectMapper);
            restClient = new DefaultRestClient(config);
        } catch (RestClientException ex) {
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "Rest client initialization failed."));
        }
    }

    /**
     * Create a new client with detailed configuration of REST client.
     *
     * @param restClientConfiguration REST service client configuration.
     * @throws TppEngineClientException Thrown when REST client initialization fails.
     */
    public TppEngineClient(RestClientConfiguration restClientConfiguration) throws TppEngineClientException {
        try {
            restClient = new DefaultRestClient(restClientConfiguration);
        } catch (RestClientException ex) {
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "Rest client initialization failed."));
        }
    }

    /**
     * Lookup consent details (details of a consent template).
     *
     * @param id Username for user account which is being looked up.
     * @return Response with user details.
     * @throws TppEngineClientException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<ConsentDetailResponse> consentDetail(String id) throws TppEngineClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("id", Collections.singletonList(id));
        return getObjectImpl("/consent", params, ConsentDetailResponse.class);
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("userId", Collections.singletonList(userId));
        params.put("consentId", Collections.singletonList(consentId));
        params.put("clientId", Collections.singletonList(clientId));
        return getObjectImpl("/consent", params, UserConsentDetailResponse.class);
    }

    /**
     * Give consent according to request parameters.
     *
     * @param request Consent information.
     * @return Information about newly created consent.
     * @throws TppEngineClientException Thrown when client request fails or authentication fails.
     */
    public ObjectResponse<GiveConsentResponse> giveConsent(GiveConsentRequest request) throws TppEngineClientException {
        return postObjectImpl("/user/consent", new ObjectRequest<>(request), GiveConsentResponse.class);
    }

    /**
     * Reject consent according to request parameters.
     *
     * @param request Consent information.
     * @return Static OK response. In case consent didn't exist, this operation is no-op.
     * @throws TppEngineClientException Thrown when client request fails or authentication fails.
     */
    public Response rejectConsent(RemoveConsentRequest request) throws TppEngineClientException {
        return postObjectImpl("/user/consent/delete", new ObjectRequest<>(request));
    }

    /**
     * Lookup information about a provided app.
     *
     * @param clientId Identifier of a TPP app.
     * @return Response with details TPP app with given client ID.
     * @throws TppEngineClientException Thrown when client request fails or app does not exist.
     */
    public ObjectResponse<TppAppDetailResponse> fetchAppInfo(String clientId) throws TppEngineClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("clientId", Collections.singletonList(clientId));
        return getObjectImpl("/tpp/app", params, TppAppDetailResponse.class);
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
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("clientId", Collections.singletonList(clientId));
        params.put("tppLicense", Collections.singletonList(tppLicense));
        return getObjectImpl("/tpp/app", params, TppAppDetailResponse.class);
    }

    /**
     * Fetch list of TPP applications based on the license info.
     *
     * @param tppLicense TPP license information.
     * @return Response with details TPP app with given client ID.
     * @throws TppEngineClientException Thrown when client request fails or app does not exist.
     */
    public ObjectResponse<List<TppAppDetailResponse>> fetchApplicationList(String tppLicense) throws TppEngineClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("tppLicense", Collections.singletonList(tppLicense));
        return getImpl("/tpp/app/list", params, new ParameterizedTypeReference<ObjectResponse<List<TppAppDetailResponse>>>(){});
    }

    /**
     * Create an application with provided information.
     *
     * @param request New application information.
     * @return Information about newly created application.
     * @throws TppEngineClientException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<TppAppDetailResponse> createApplication(CreateTppAppRequest request) throws TppEngineClientException {
        return postObjectImpl("/tpp/app", new ObjectRequest<>(request), TppAppDetailResponse.class);
    }

    /**
     * Update an application with provided client ID with a new information.
     *
     * @param clientId Client ID of an app to be updated.
     * @param request New application information.
     * @return Information about newly created application.
     * @throws TppEngineClientException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<TppAppDetailResponse> updateApplication(String clientId, CreateTppAppRequest request) throws TppEngineClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("clientId", Collections.singletonList(clientId));
        return putObjectImpl("/tpp/app", new ObjectRequest<>(request), params, TppAppDetailResponse.class);
    }

    /**
     * Renew the client secret for the application.
     *
     * @param clientId Client ID of an application to be refreshed.
     * @param tppLicense License information of TPP.
     * @return Information about newly created application.
     * @throws TppEngineClientException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<TppAppDetailResponse> renewClientSecret(String clientId, String tppLicense) throws TppEngineClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("clientId", Collections.singletonList(clientId));
        params.put("tppLicense", Collections.singletonList(tppLicense));
        return putObjectImpl("/tpp/app/renewSecret", null, params, TppAppDetailResponse.class);
    }

    /**
     * Delete an application with provided information.
     *
     * @param clientId Client ID of the app.
     * @param tppLicense License information of TPP.
     * @return Generic response.
     * @throws TppEngineClientException Thrown when client request fails or user does not exist.
     */
    public Response deleteApplication(String clientId, String tppLicense) throws TppEngineClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("clientId", Collections.singletonList(clientId));
        params.put("tppLicense", Collections.singletonList(tppLicense));
        return deleteObjectImpl("/tpp/app", params);
    }

    // Generic HTTP client methods

    /**
     * Prepare a generic GET response.
     *
     * @param path Resource path.
     * @param typeReference Type reference.
     * @param queryParams Query parameters.
     * @return Object obtained after processing the response JSON.
     * @throws TppEngineClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> T getImpl(String path, MultiValueMap<String, String> queryParams, ParameterizedTypeReference<T> typeReference) throws TppEngineClientException {
        try {
            return restClient.get(path, queryParams, null, typeReference).getBody();
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Prepare GET object response.
     *
     * @param path Resource path.
     * @param queryParams Query parameters.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws TppEngineClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> getObjectImpl(String path, MultiValueMap<String, String> queryParams, Class<T> responseType) throws TppEngineClientException {
        try {
            return restClient.getObject(path, queryParams, null, responseType);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Prepare POST object response. Uses default {@link Response} type reference for response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @return Object obtained after processing the response JSON.
     * @throws TppEngineClientException In case of network, response / JSON processing, or other IO error.
     */
    private Response postObjectImpl(String path, ObjectRequest<?> request) throws TppEngineClientException {
        try {
            return restClient.postObject(path, request);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Prepare POST object response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws TppEngineClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> postObjectImpl(String path, ObjectRequest<?> request, Class<T> responseType) throws TppEngineClientException {
        try {
            return restClient.postObject(path, request, responseType);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Prepare PUT object response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @param queryParams Query parameters.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws TppEngineClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> putObjectImpl(String path, ObjectRequest<?> request, MultiValueMap<String, String> queryParams, Class<T> responseType) throws TppEngineClientException {
        try {
            return restClient.putObject(path, request, queryParams, null, responseType);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "HTTP PUT request failed."));
        }
    }

    /**
     * Prepare a generic DELETE response.
     *
     * @param path Resource path.
     * @param queryParams Query parameters.
     * @return Object obtained after processing the response JSON.
     * @throws TppEngineClientException In case of network, response / JSON processing, or other IO error.
     */
    private Response deleteObjectImpl(String path, MultiValueMap<String, String> queryParams) throws TppEngineClientException {
        try {
            return restClient.deleteObject(path, queryParams, null);
        } catch (RestClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new TppEngineClientException(ex, new TppEngineError(resolveErrorCode(ex), "HTTP POST request failed."));
        }
    }

    /**
     * Resolve error code based on HTTP status code from REST client exception.
     */
    private String resolveErrorCode(RestClientException ex) {
        if (ex.getStatusCode() == null) {
            // REST client errors, response not received
            return TppEngineError.Code.ERROR_GENERIC;
        }
        if (ex.getStatusCode().is4xxClientError()) {
            // Errors caused by invalid TPP engine client requests
            return TppEngineError.Code.TPP_ENGINE_CLIENT_ERROR;
        }
        if (ex.getStatusCode().is5xxServerError()) {
            // Internal errors in TPP engine
            return TppEngineError.Code.REMOTE_ERROR;
        }
        // Other errors during communication
        return TppEngineError.Code.COMMUNICATION_ERROR;
    }

}