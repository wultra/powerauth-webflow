/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.getlime.security.powerauth.lib.dataadapter.client;

import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.*;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.request.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.*;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Data Adapter Client provides methods for communication with the Data Adapter.
 * It uses the RestClient class to handle REST API calls.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DataAdapterClient {

    private static final Logger logger = LoggerFactory.getLogger(DataAdapterClient.class);

    private final RestClient restClient;

    /**
     * Create a new client with provided base URL.
     * @param serviceBaseUrl REST service base URL.
     * @throws DataAdapterClientErrorException Thrown when REST client initialization fails.
     */
    public DataAdapterClient(String serviceBaseUrl) throws DataAdapterClientErrorException {
        try {
            restClient = new DefaultRestClient(serviceBaseUrl);
        } catch (RestClientException ex) {
            DataAdapterClientErrorException ex2 = new DataAdapterClientErrorException(ex, new DataAdapterError(resolveErrorCode(ex), "Rest client initialization failed."));
            logError(ex2);
            throw ex2;
        }
    }

    /**
     * Create a new client with provided base URL.
     * @param restClientConfiguration REST service client configuration.
     * @throws DataAdapterClientErrorException Thrown when REST client initialization fails.
     */
    public DataAdapterClient(RestClientConfiguration restClientConfiguration) throws DataAdapterClientErrorException {
        try {
            restClient = new DefaultRestClient(restClientConfiguration);
        } catch (RestClientException ex) {
            DataAdapterClientErrorException ex2 = new DataAdapterClientErrorException(ex, new DataAdapterError(resolveErrorCode(ex), "Rest client initialization failed."));
            logError(ex2);
            throw ex2;
        }
    }

    /**
     * Lookup user account.
     *
     * @param username Username for user account which is being looked up.
     * @param organizationId Organization ID for which the user ID is assigned to.
     * @param clientCertificate Client TLS certificate.
     * @param operationContext Operation context.
     * @return Response with user details.
     * @throws DataAdapterClientErrorException Thrown when client request fails or user does not exist.
     */
    public ObjectResponse<UserDetailResponse> lookupUser(String username, String organizationId, String clientCertificate, OperationContext operationContext) throws DataAdapterClientErrorException {
        UserLookupRequest request = new UserLookupRequest(username, organizationId, clientCertificate, operationContext);
        return postObjectImpl("/api/auth/user/lookup", new ObjectRequest<>(request), UserDetailResponse.class);
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
        UserAuthenticationRequest request = new UserAuthenticationRequest(userId, organizationId, password, authenticationContext, operationContext);
        return postObjectImpl("/api/auth/user/authenticate", new ObjectRequest<>(request), UserAuthenticationResponse.class);
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
        UserDetailRequest request = new UserDetailRequest(userId, organizationId);
        return postObjectImpl("/api/auth/user/info", new ObjectRequest<>(request), UserDetailResponse.class);
    }

    /**
     * Initialize an authentication method.
     *
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param authMethod Authentication method.
     * @param operationContext Operation context.
     * @return Response with user details.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<InitAuthMethodResponse> initAuthMethod(String userId, String organizationId, AuthMethod authMethod, OperationContext operationContext) throws DataAdapterClientErrorException {
        InitAuthMethodRequest request = new InitAuthMethodRequest(userId, organizationId, authMethod, operationContext);
        return postObjectImpl("/api/auth/method/init", new ObjectRequest<>(request), InitAuthMethodResponse.class);
    }

    /**
     * Create and send authorization SMS message with new OTP authorization code.
     *
     * @param userId           User ID.
     * @param organizationId   Organization ID.
     * @param accountStatus    User account status.
     * @param authMethod       Authentication method.
     * @param operationContext Operation context.
     * @param lang             Language for i18n.
     * @param resend           Whether SMS is being resent.
     * @return Response with generated messageId.
     * @throws DataAdapterClientErrorException Thrown when client request fails or SMS could not be delivered.
     */
    public ObjectResponse<CreateSmsAuthorizationResponse> createAndSendAuthorizationSms(String userId, String organizationId, AccountStatus accountStatus, AuthMethod authMethod, OperationContext operationContext, String lang, boolean resend) throws DataAdapterClientErrorException {
        CreateSmsAuthorizationRequest request = new CreateSmsAuthorizationRequest(userId, organizationId, accountStatus, lang, authMethod, operationContext, resend);
        return postObjectImpl("/api/auth/sms/create", new ObjectRequest<>(request), CreateSmsAuthorizationResponse.class);
    }

    /**
     * Send authorization SMS message with existing OTP authorization code.
     *
     * @param userId            User ID.
     * @param organizationId    Organization ID.
     * @param accountStatus     User account status.
     * @param authMethod        Authentication method.
     * @param operationContext  Operation context.
     * @param messageId         Message ID.
     * @param authorizationCode SMS OTP authorization code.
     * @param lang              Language for i18n.
     * @param resend            Whether SMS is being resent.
     * @return Response with generated messageId.
     * @throws DataAdapterClientErrorException Thrown when client request fails or SMS could not be delivered.
     */
    public ObjectResponse<SendAuthorizationSmsResponse> sendAuthorizationSms(String userId, String organizationId, AccountStatus accountStatus, AuthMethod authMethod, OperationContext operationContext, String messageId, String authorizationCode, String lang, boolean resend) throws DataAdapterClientErrorException {
        SendAuthorizationSmsRequest request = new SendAuthorizationSmsRequest(userId, organizationId, accountStatus, authMethod, operationContext, messageId, authorizationCode, lang, resend);
        return postObjectImpl("/api/auth/sms/send", new ObjectRequest<>(request), SendAuthorizationSmsResponse.class);
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
        VerifySmsAuthorizationRequest request = new VerifySmsAuthorizationRequest(messageId, authorizationCode, userId, organizationId, accountStatus, operationContext);
        return postObjectImpl("/api/auth/sms/verify", new ObjectRequest<>(request), VerifySmsAuthorizationResponse.class);
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
        VerifySmsAndPasswordRequest request = new VerifySmsAndPasswordRequest(messageId, authorizationCode, userId, organizationId, accountStatus, password, authenticationContext, operationContext);
        return postObjectImpl("/api/auth/sms/password/verify", new ObjectRequest<>(request), VerifySmsAndPasswordResponse.class);
    }

    /**
     * Create a new login operation from the OAuth 2.0 login context.
     *
     * @param clientId OAuth 2.0 Client ID.
     * @param scopes OAuth 2.0 Scopes.
     * @return Information about a new operation.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<CreateImplicitLoginOperationResponse> createImplicitLoginOperation(String clientId, String[] scopes) throws DataAdapterClientErrorException {
        CreateImplicitLoginOperationRequest request = new CreateImplicitLoginOperationRequest(clientId, scopes);
        return postObjectImpl("/api/operation/create", new ObjectRequest<>(request), CreateImplicitLoginOperationResponse.class);
    }

    /**
     * Get the operation mapping from Next Step operation to PowerAuth operation.
     *
     * @param userId User ID of the user for this request.
     * @param organizationId Organization ID for this request.
     * @param authMethod Authentication method.
     * @param operationContext Operation context.
     * @return Operation mapping from Next Step operation to PowerAuth operation.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<GetPAOperationMappingResponse> getPAOperationMapping(String userId, String organizationId, AuthMethod authMethod, OperationContext operationContext) throws DataAdapterClientErrorException {
        GetPAOperationMappingRequest request = new GetPAOperationMappingRequest();
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setAuthMethod(authMethod);
        request.setOperationContext(operationContext);
        return postObjectImpl("/api/operation/mapping", new ObjectRequest<>(request), GetPAOperationMappingResponse.class);
    }

    /**
     * Verify client TLS certificate or message signed using qualified certificate.
     *
     * @param userId User ID for this authentication request.
     * @param organizationId Organization ID for this authentication request.
     * @param certificate Certificate in PEM format for client TLS authentication.
     * @param signedMessage Signed message created using qualified certificate, including the certificate.
     * @param authMethod Authentication method.
     * @param accountStatus Current user account status.
     * @param operationContext Operation context.
     * @return Empty response returned when action succeeds.
     * @throws DataAdapterClientErrorException Thrown when client request fails or authentication/authorization fails.
     */
    public ObjectResponse<VerifyCertificateResponse> verifyCertificate(String userId, String organizationId, String certificate, String signedMessage, AuthMethod authMethod, AccountStatus accountStatus, OperationContext operationContext) throws DataAdapterClientErrorException {
        VerifyCertificateRequest request = new VerifyCertificateRequest(userId, organizationId, certificate, signedMessage, authMethod, accountStatus, operationContext);
        return postObjectImpl("/api/auth/certificate/verify", new ObjectRequest<>(request), VerifyCertificateResponse.class);
    }

    /**
     * Decorate operation form data.
     *
     * @param userId User ID of the user for this request.
     * @param organizationId Organization ID for this request.
     * @param operationContext Operation context.
     * @param authMethod Authentication method.
     * @return Decorated operation form data.
     * @throws DataAdapterClientErrorException Thrown when client request fails.
     */
    public ObjectResponse<DecorateOperationFormDataResponse> decorateOperationFormData(String userId, String organizationId, AuthMethod authMethod, OperationContext operationContext) throws DataAdapterClientErrorException {
        DecorateOperationFormDataRequest request = new DecorateOperationFormDataRequest(userId, organizationId, authMethod, operationContext);
        return postObjectImpl("/api/operation/formdata/decorate", new ObjectRequest<>(request), DecorateOperationFormDataResponse.class);
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
    public Response formDataChangedNotification(FormDataChange formDataChange, String userId, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        FormDataChangeNotificationRequest request = new FormDataChangeNotificationRequest();
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setOperationContext(operationContext);
        request.setFormDataChange(formDataChange);
        return postObjectImpl("/api/operation/formdata/change", new ObjectRequest<>(request));
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
    public Response operationChangedNotification(OperationChange operationChange, String userId, String organizationId, OperationContext operationContext) throws DataAdapterClientErrorException {
        OperationChangeNotificationRequest request = new OperationChangeNotificationRequest();
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setOperationContext(operationContext);
        request.setOperationChange(operationChange);
        return postObjectImpl("/api/operation/change", new ObjectRequest<>(request));
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
        InitConsentFormRequest request = new InitConsentFormRequest(userId, organizationId, operationContext);
        return postObjectImpl("/api/auth/consent/init", new ObjectRequest<>(request), InitConsentFormResponse.class);
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
        CreateConsentFormRequest request = new CreateConsentFormRequest(userId, organizationId, lang, operationContext);
        return postObjectImpl("/api/auth/consent/create", new ObjectRequest<>(request), CreateConsentFormResponse.class);
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
        ValidateConsentFormRequest request = new ValidateConsentFormRequest(userId, organizationId, operationContext, lang, options);
        return postObjectImpl("/api/auth/consent/validate", new ObjectRequest<>(request), ValidateConsentFormResponse.class);
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
        SaveConsentFormRequest request = new SaveConsentFormRequest(userId, organizationId, operationContext, options);
        return postObjectImpl("/api/auth/consent/save", new ObjectRequest<>(request), SaveConsentFormResponse.class);
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
        AfsRequest request = new AfsRequest(userId, organizationId, operationContext, afsRequestParameters, extras);
        return postObjectImpl("/api/afs/action/execute", new ObjectRequest<>(request), AfsResponse.class);
    }

    // Generic HTTP client methods

    /**
     * Prepare POST object response. Uses default {@link Response} type reference for response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @return Object obtained after processing the response JSON.
     * @throws DataAdapterClientErrorException In case of network, response / JSON processing, or other IO error.
     */
    private Response postObjectImpl(String path, ObjectRequest<?> request) throws DataAdapterClientErrorException {
        try {
            return restClient.postObject(path, request);
        } catch (RestClientException ex) {
            DataAdapterClientErrorException ex2 = new DataAdapterClientErrorException(ex, new DataAdapterError(resolveErrorCode(ex), "HTTP POST request failed."));
            logError(ex2);
            throw ex2;
        }
    }

    /**
     * Prepare POST object response.
     *
     * @param path Resource path.
     * @param request Request body.
     * @param responseType Response type.
     * @return Object obtained after processing the response JSON.
     * @throws DataAdapterClientErrorException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> postObjectImpl(String path, ObjectRequest<?> request, Class<T> responseType) throws DataAdapterClientErrorException {
        try {
            return restClient.postObject(path, request, responseType);
        } catch (RestClientException ex) {
            DataAdapterClientErrorException ex2 = new DataAdapterClientErrorException(ex, new DataAdapterError(resolveErrorCode(ex), "HTTP POST request failed."));
            logError(ex2);
            throw ex2;
        }
    }

    /**
     * Log REST client exception details.
     * @param ex REST client exception.
     */
    private void logError(DataAdapterClientErrorException ex) {
        Error error = ex.getError();
        if (error != null) {
            logger.warn("Data Adapter REST API call failed with error code: {}", error.getCode());
        } else {
            logger.warn(ex.getMessage(), ex);
        }
    }

    /**
     * Resolve error code based on HTTP status code from REST client exception.
     */
    private String resolveErrorCode(RestClientException ex) {
        if (ex.getStatusCode() == null) {
            // REST client errors, response not received
            return DataAdapterError.Code.ERROR_GENERIC;
        }
        if (ex.getStatusCode().is4xxClientError()) {
            // Errors caused by invalid Next Step client requests
            return DataAdapterError.Code.DATA_ADAPTER_CLIENT_ERROR;
        }
        if (ex.getStatusCode().is5xxServerError()) {
            // Internal errors in Next Step server
            return DataAdapterError.Code.REMOTE_ERROR;
        }
        // Other errors during communication
        return DataAdapterError.Code.COMMUNICATION_ERROR;
    }

}
