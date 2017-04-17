package io.getlime.security.powerauth.app.webauth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.webauth.configuration.WebAuthServerConfiguration;
import io.getlime.security.powerauth.lib.credentials.model.entity.ErrorModel;
import io.getlime.security.powerauth.lib.credentials.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.credentials.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
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
@Service
public class AuthenticationService {

    /**
     * Provides access to Web Auth configuration.
     */
    private WebAuthServerConfiguration webAuthConfig;
    /**
     * Used for converting error responses to objects.
     */
    private ObjectMapper objectMapper;

    @Autowired
    public AuthenticationService(WebAuthServerConfiguration webAuthConfig, ObjectMapper objectMapper) {
        this.webAuthConfig = webAuthConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Perform authentication with provided username and password.
     *
     * @param username username for user who is being authenticated
     * @param password password as chararray to avoid having Strings with passwords in Java memory
     * @return a Response with either AuthenticationResponse or ErrorModel given the result of the operation
     */
    public Response<?> authenticate(String username, char[] password) {
        String credentialsServiceUrl = webAuthConfig.getCredentialServerServiceUrl();
        RestTemplate template = new RestTemplate();
        // java.net request factory throws exceptions -> response body is lost for errors, we use httpclient instead
        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        AuthenticationRequest request = new AuthenticationRequest(username, String.valueOf(password), AuthenticationType.BASIC);
        HttpEntity<Request<AuthenticationRequest>> entity = new HttpEntity<>(new Request<>(request));
        try {
            ResponseEntity<Response<AuthenticationResponse>> response = template.exchange(credentialsServiceUrl + "/authenticate",
                    HttpMethod.POST, entity, new ParameterizedTypeReference<Response<AuthenticationResponse>>() {
                    });
            System.out.println("Response from Credential Server - successfully authenticated user: " + response.getBody().getResponseObject().getUserId());
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            String responseString = ex.getResponseBodyAsString();
            try {
                // handles regular authentication errors
                ErrorModel error = objectMapper.readValue(responseString, ErrorModel.class);
                System.err.println("Response from Credential Server was invalid, exception: " + ex.toString());
                if (error.getCode() == null) {
                    error.setCode(ErrorModel.ResponseCode.ERROR_GENERIC);
                    error.setMessage("Credential service error: " + ex.toString());
                }
                return new Response<>(Response.Status.ERROR, error);
            } catch (IOException ex2) {
                // should never be reached - fatal error
                System.err.println("Response from Credential Server was invalid, exception: " + ex2.toString());
                ErrorModel error = new ErrorModel();
                error.setCode(ErrorModel.ResponseCode.ERROR_GENERIC);
                error.setMessage("Credential service error: " + ex2.toString());
                return new Response<>(Response.Status.ERROR, error);
            }
        } catch (ResourceAccessException ex) {
            // Credential service is down
            System.err.println("Credential Server is not available, exception: " + ex.toString());
            ErrorModel error = new ErrorModel();
            error.setCode(ErrorModel.ResponseCode.ERROR_GENERIC);
            error.setMessage("Next step service is not available.");
            return new Response<>(Response.Status.ERROR, error);
        }
    }

}
