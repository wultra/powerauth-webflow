package io.getlime.security.powerauth.app.webauth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.webauth.configuration.WebAuthServerConfiguration;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
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

import java.util.List;

/**
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

    public CreateOperationResponse createOperation(String operationName, String operationData, List<KeyValueParameter> params) {
        String nextStepServiceUrl = webAuthConfig.getNextstepServiceUrl();
        RestTemplate template = new RestTemplate();
        // java.net request factory throws exceptions -> response body is lost for errors, we use httpclient instead
        template.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        CreateOperationRequest request = new CreateOperationRequest();
        request.setOperationName(operationName);
        request.setOperationData(operationData);
        for (KeyValueParameter param: params) {
            request.getParams().add(param);
        }
        HttpEntity<Request<CreateOperationRequest>> entity = new HttpEntity<>(new Request<>(request));
        try {
            ResponseEntity<Response<CreateOperationResponse>> response = template.exchange(nextStepServiceUrl + "/operation",
                    HttpMethod.POST, entity, new ParameterizedTypeReference<Response<CreateOperationResponse>>() {});
            CreateOperationResponse respObj = response.getBody().getResponseObject();
            System.out.println("Response from Next Step Server: "+respObj.getResult()+", "+respObj.getResultDescription());
            return response.getBody().getResponseObject();
        } catch (HttpStatusCodeException ex) {
            ex.printStackTrace();
            // TODO - exception handling
            return null;
        }
    }

    public UpdateOperationResponse updateOperation(String operationId, String userId, AuthMethod authMethod,
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
        for (KeyValueParameter param: params) {
            request.getParams().add(param);
        }
        HttpEntity<Request<UpdateOperationRequest>> entity = new HttpEntity<>(new Request<>(request));
        try {
            ResponseEntity<Response<UpdateOperationResponse>> response = template.exchange(nextStepServiceUrl + "/operation",
                    HttpMethod.PUT, entity, new ParameterizedTypeReference<Response<UpdateOperationResponse>>() {});
            UpdateOperationResponse respObj = response.getBody().getResponseObject();
            System.out.println("Response from Next Step Server: "+respObj.getResult()+", "+respObj.getResultDescription());
            return response.getBody().getResponseObject();
        } catch (HttpStatusCodeException ex) {
            ex.printStackTrace();
            // TODO - exception handling
            return null;
        }

    }

}
