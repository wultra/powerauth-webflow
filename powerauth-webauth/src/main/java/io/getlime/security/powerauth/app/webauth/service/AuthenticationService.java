package io.getlime.security.powerauth.app.webauth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.webauth.configuration.WebAuthServerConfiguration;
import io.getlime.security.powerauth.lib.credentials.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @author Roman Strobl
 */
@Service
public class AuthenticationService {

    private WebAuthServerConfiguration webAuthConfig;
    private ObjectMapper objectMapper;

    @Autowired
    public AuthenticationService(WebAuthServerConfiguration webAuthConfig, ObjectMapper objectMapper) {
        this.webAuthConfig = webAuthConfig;
        this.objectMapper = objectMapper;
    }

    public AuthenticationResponse authenticate(String username, char[] password) {
        String credentialsServiceUrl = webAuthConfig.getCredentialServerServiceUrl();
        RestTemplate template = new RestTemplate();
        // java.net request factory throws exceptions -> response body is lost for errors, we use httpclient instead
        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        AuthenticationRequest request = new AuthenticationRequest(username, String.valueOf(password), AuthenticationType.BASIC);
        HttpEntity<AuthenticationRequest> entity = new HttpEntity<>(request);
        try {
            ResponseEntity<AuthenticationResponseSuccess> response = template.exchange(credentialsServiceUrl + "/authenticate",
                    HttpMethod.POST, entity, AuthenticationResponseSuccess.class);
            System.out.println("Response from Credential Server: "+response.getBody());
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            String responseString = ex.getResponseBodyAsString();
            try {
                // handles regular authentication errors
                AuthenticationResponse error = objectMapper.readValue(responseString, AuthenticationResponseError.class);
                System.out.println("Response from Credential Server: "+error);
                return error;
            } catch (IOException ex2) {
                // should never be reached - fatal error
                System.out.println("Response from Credential Server was invalid, exception: "+ex2.toString());
                ErrorResponse fatalErrorResponse = new ErrorResponse(ErrorResponse.ResponseCode.INTERNAL_SERVER_ERROR, ex2.toString());
                return new AuthenticationResponseError(HttpStatus.INTERNAL_SERVER_ERROR, fatalErrorResponse);
            }
        }
    }

}
