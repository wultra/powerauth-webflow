package io.getlime.security.powerauth.lib.credentials.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.lib.credentials.model.entity.ErrorModel;
import io.getlime.security.powerauth.lib.credentials.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.credentials.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
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
     * @return a Response with either AuthenticationResponse or ErrorModel given the result of the operation
     */
    public Response<AuthenticationResponse> authenticate(String username, String password) throws CredentialStoreClientErrorException {
        try {
            // Exchange authentication request with credential server.
            AuthenticationRequest request = new AuthenticationRequest(username, password, AuthenticationType.BASIC);
            HttpEntity<Request<AuthenticationRequest>> entity = new HttpEntity<>(new Request<>(request));
            ResponseEntity<Response<AuthenticationResponse>> response = defaultTemplate().exchange(serviceUrl + "/authenticate", HttpMethod.POST, entity, new ParameterizedTypeReference<Response<AuthenticationResponse>>() {});
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            try {
                TypeReference<Response<ErrorModel>> typeReference = new TypeReference<Response<ErrorModel>>() {};
                Response<ErrorModel> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(), typeReference);
                ErrorModel error = errorResponse.getResponseObject();
                if (error.getCode() == null) { // process malformed errors with undefined error code
                    error.setCode(ErrorModel.ResponseCode.ERR_GENERIC);
                    error.setMessage(ex.getMessage());
                }
                throw new CredentialStoreClientErrorException(ex, error);
            } catch (IOException ex2) {
                // JSON parsing failed
                ErrorModel error = new ErrorModel();
                error.setCode(ErrorModel.ResponseCode.ERR_GENERIC);
                error.setMessage(ex2.getMessage());
                throw new CredentialStoreClientErrorException(ex, error);
            }
        } catch (ResourceAccessException ex) {
            // Credential service is down
            ErrorModel error = new ErrorModel();
            error.setCode(ErrorModel.ResponseCode.ERR_GENERIC);
            error.setMessage(ex.getMessage());
            throw new CredentialStoreClientErrorException(ex, error);
        }
    }

}
