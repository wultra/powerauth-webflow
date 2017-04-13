package io.getlime.security.powerauth.app.webauth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.webauth.configuration.WebAuthServerConfiguration;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ErrorModel;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * This service handles communication with the Next Step server.
 *
 * @author Roman Strobl
 */
@Service
public class NextStepService {

    private WebAuthServerConfiguration webAuthConfig;
    private ObjectMapper objectMapper;

    @Autowired
    public NextStepService(WebAuthServerConfiguration webAuthConfig, ObjectMapper objectMapper) {
        this.webAuthConfig = webAuthConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Calls the operation endpoint via POST method to create a new operation.
     *
     * @param operationName operation name
     * @param operationData operation data
     * @param params        list of generic parameters
     * @return a Response with CreateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public Response<?> createOperation(String operationName, String operationData, List<KeyValueParameter> params) {
        String nextStepServiceUrl = webAuthConfig.getNextstepServiceUrl();
        RestTemplate template = new RestTemplate();
        // java.net request factory throws exceptions -> response body is lost for errors, we use httpclient instead
        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        CreateOperationRequest request = new CreateOperationRequest();
        request.setOperationName(operationName);
        request.setOperationData(operationData);
        for (KeyValueParameter param : params) {
            request.getParams().add(param);
        }
        HttpEntity<Request<CreateOperationRequest>> entity = new HttpEntity<>(new Request<>(request));
        try {
            ResponseEntity<Response<CreateOperationResponse>> response = template.exchange(nextStepServiceUrl + "/operation",
                    HttpMethod.POST, entity, new ParameterizedTypeReference<Response<CreateOperationResponse>>() {
                    });
            CreateOperationResponse respObj = response.getBody().getResponseObject();
            System.out.println("Response from Next Step Server: " + respObj.getResult() + ", " + respObj.getResultDescription());
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            return generateErrorResponse(ex);
        }
    }

    /**
     * Calls the operation endpoint via PUT method to update an existing.
     *
     * @param operationId    id of the updated operation
     * @param userId         user id
     * @param authMethod     authentication method
     * @param authStepResult result of the last step
     * @param params         list of generic parameters
     * @return a Response with UpdateOperationResponse object for OK status or ErrorModel for ERROR status
     */
    public Response<?> updateOperation(String operationId, String userId, AuthMethod authMethod,
                                       AuthStepResult authStepResult, List<KeyValueParameter> params) {
        String nextStepServiceUrl = webAuthConfig.getNextstepServiceUrl();
        RestTemplate template = new RestTemplate();
        // java.net request factory throws exceptions -> response body is lost for errors, we use httpclient instead
        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        UpdateOperationRequest request = new UpdateOperationRequest();
        request.setOperationId(operationId);
        request.setUserId(userId);
        request.setAuthMethod(authMethod);
        request.setAuthStepResult(authStepResult);
        for (KeyValueParameter param : params) {
            request.getParams().add(param);
        }
        HttpEntity<Request<UpdateOperationRequest>> entity = new HttpEntity<>(new Request<>(request));
        try {
            ResponseEntity<Response<UpdateOperationResponse>> response = template.exchange(nextStepServiceUrl + "/operation",
                    HttpMethod.PUT, entity, new ParameterizedTypeReference<Response<UpdateOperationResponse>>() {
                    });
            UpdateOperationResponse respObj = response.getBody().getResponseObject();
            System.out.println("Response from Next Step Server: " + respObj.getResult() + ", " + respObj.getResultDescription());
            return new Response<>(Response.Status.OK, response.getBody().getResponseObject());
        } catch (HttpStatusCodeException ex) {
            return generateErrorResponse(ex);
        }
    }

    /**
     * Error handling for failed requests to the Next Step server.
     *
     * @param ex Exception based on a non-200 HTTP status
     * @return ErrorModel with description of the error
     */
    private Response<ErrorModel> generateErrorResponse(HttpStatusCodeException ex) {
        String responseString = ex.getResponseBodyAsString();
        try {
            // handles regular errors
            ErrorModel error = objectMapper.readValue(responseString, ErrorModel.class);
            System.out.println("Response from Next Step Server was invalid, exception: " + ex.toString());
            if (error.getCode() == null) {
                error.setCode("ERR_GENERIC");
                error.setMessage("Next step service error: " + ex.toString());
            }
            return new Response<>(Response.Status.ERROR, error);
        } catch (IOException ex2) {
            // should never be reached - fatal error
            System.out.println("Response from Next Step Server was invalid, exception: " + ex2.toString());
            ErrorModel error = new ErrorModel();
            error.setCode("ERR_GENERIC");
            error.setMessage("Next step service error: " + ex2.toString());
            return new Response<>(Response.Status.ERROR, error);
        }
    }

}
