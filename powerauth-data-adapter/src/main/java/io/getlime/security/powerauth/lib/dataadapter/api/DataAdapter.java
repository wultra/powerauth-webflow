/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.api;

import io.getlime.security.powerauth.lib.dataadapter.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.model.request.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.BankAccountListResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * DataAdapter interface defines methods which should be implemented for integration of Web Flow with 3rd parties.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public interface DataAdapter {

    /**
     * Authenticate user using provided credentials.
     *
     * @param authenticationRequest Authentication request.
     * @return Authentication response.
     * @throws MethodArgumentNotValidException Thrown when input validation fails.
     * @throws AuthenticationFailedException Thrown when authentication fails.
     */
    AuthenticationResponse authenticateUser(AuthenticationRequest authenticationRequest) throws MethodArgumentNotValidException, AuthenticationFailedException;

    /**
     * Fetch user detail for given user.
     * @param userDetailRequest Request with user ID.
     * @return Response with user details.
     * @throws MethodArgumentNotValidException Thrown when input validation fails.
     */
    UserDetailResponse fetchUserDetail(UserDetailRequest userDetailRequest) throws MethodArgumentNotValidException;

    /**
     * Fetch bank account details for given user.
     * @param bankAccountListRequest Request with user ID.
     * @return Response with bank account details.
     * @throws MethodArgumentNotValidException Thrown when input validation fails.
     */
    BankAccountListResponse fetchBankAccounts(BankAccountListRequest bankAccountListRequest) throws MethodArgumentNotValidException;

    /**
     * Send notification about formData change.
     * @param notificationRequest Notification request.
     */
    void formDataChangedNotification(FormDataChangeNotificationRequest notificationRequest);

    /**
     * Send notification about operation change.
     * @param notificationRequest Notification request.
     */
    void operationChangedNotification(OperationChangeNotificationRequest notificationRequest);

    /**
     * Create an authorization SMS with generated OTP.
     * @param createSMSRequest Create SMS request.
     * @return SMS OTP response.
     * @throws MethodArgumentNotValidException Thrown when input validation fails.
     * @throws SMSAuthorizationFailedException Thrown when message could not be created.
     */
    CreateSMSAuthorizationResponse createAuthorizationSMS(CreateSMSAuthorizationRequest createSMSRequest) throws MethodArgumentNotValidException, SMSAuthorizationFailedException;

    /**
     * Verify an authorization SMS with generated OTP.
     * @param verifySMSRequest Verify SMS OTP request.
     * @throws SMSAuthorizationFailedException Thrown when OTP validation fails.
     */
    void verifyAuthorizationSMS(VerifySMSAuthorizationRequest verifySMSRequest) throws SMSAuthorizationFailedException;

}
