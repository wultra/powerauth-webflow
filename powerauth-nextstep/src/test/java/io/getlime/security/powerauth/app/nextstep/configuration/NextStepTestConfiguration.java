/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.configuration;

import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialCategory;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationRequestType;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetApplicationListResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.security.Security;

/**
 * Configure Next Step service for tests.
 */
@Service
public class NextStepTestConfiguration {

    public void configure(NextStepClient nextStepClient) throws NextStepClientException {
        GetApplicationListResponse appList = nextStepClient.getApplicationList(false).getResponseObject();
        if (!appList.getApplications().isEmpty()) {
            // Next Step is already initialized
            return;
        }

        // Enable BC provider
        Security.addProvider(new BouncyCastleProvider());

        // Configure authentication methods
        CreateAuthMethodRequest requestInit = new CreateAuthMethodRequest();
        requestInit.setAuthMethod(AuthMethod.INIT);
        requestInit.setOrderNumber(1L);
        requestInit.setCheckAuthFails(false);
        requestInit.setCheckUserPrefs(false);
        requestInit.setHasUserInterface(false);
        requestInit.setHasMobileToken(false);
        nextStepClient.createAuthMethod(requestInit);
        CreateAuthMethodRequest requestSmsKey = new CreateAuthMethodRequest();
        requestSmsKey.setAuthMethod(AuthMethod.SMS_KEY);
        requestSmsKey.setOrderNumber(2L);
        requestSmsKey.setCheckAuthFails(true);
        requestSmsKey.setMaxAuthFails(5);
        requestSmsKey.setCheckUserPrefs(false);
        requestSmsKey.setHasUserInterface(false);
        requestSmsKey.setHasMobileToken(false);
        nextStepClient.createAuthMethod(requestSmsKey);

        // Configure operations
        CreateOperationConfigRequest requestAuthOtp = new CreateOperationConfigRequest();
        requestAuthOtp.setOperationName("auth_otp");
        requestAuthOtp.setMobileTokenEnabled(false);
        requestAuthOtp.setMobileTokenMode("{}");
        requestAuthOtp.setTemplateVersion("A");
        requestAuthOtp.setTemplateId(1);
        nextStepClient.createOperationConfig(requestAuthOtp);

        // Configure organizations
        nextStepClient.createOrganization("RETAIL", null, true, 1);
        nextStepClient.createOrganization("SME", null, true, 2);

        // Configure steps
        CreateStepDefinitionRequest step1 = new CreateStepDefinitionRequest();
        step1.setStepDefinitionId(1);
        step1.setOperationName("auth_otp");
        step1.setOperationRequestType(OperationRequestType.CREATE);
        step1.setResponsePriority(1);
        step1.setResponseAuthMethod(AuthMethod.SMS_KEY);
        step1.setResponseResult(AuthResult.CONTINUE);
        nextStepClient.createStepDefinition(step1);
        CreateStepDefinitionRequest step2 = new CreateStepDefinitionRequest();
        step2.setStepDefinitionId(2);
        step2.setOperationName("auth_otp");
        step2.setOperationRequestType(OperationRequestType.UPDATE);
        step2.setRequestAuthMethod(AuthMethod.INIT);
        step2.setResponsePriority(1);
        step2.setRequestAuthStepResult(AuthStepResult.CANCELED);
        step2.setResponseAuthMethod(AuthMethod.INIT);
        step2.setResponseResult(AuthResult.FAILED);
        nextStepClient.createStepDefinition(step2);
        CreateStepDefinitionRequest step3 = new CreateStepDefinitionRequest();
        step3.setStepDefinitionId(3);
        step3.setOperationName("auth_otp");
        step3.setOperationRequestType(OperationRequestType.UPDATE);
        step3.setRequestAuthMethod(AuthMethod.SMS_KEY);
        step3.setResponsePriority(1);
        step3.setRequestAuthStepResult(AuthStepResult.CANCELED);
        step3.setResponseResult(AuthResult.FAILED);
        nextStepClient.createStepDefinition(step3);
        CreateStepDefinitionRequest step4 = new CreateStepDefinitionRequest();
        step4.setStepDefinitionId(4);
        step4.setOperationName("auth_otp");
        step4.setOperationRequestType(OperationRequestType.UPDATE);
        step4.setRequestAuthMethod(AuthMethod.SMS_KEY);
        step4.setResponsePriority(1);
        step4.setRequestAuthStepResult(AuthStepResult.CONFIRMED);
        step4.setResponseResult(AuthResult.DONE);
        nextStepClient.createStepDefinition(step4);
        CreateStepDefinitionRequest step5 = new CreateStepDefinitionRequest();
        step5.setStepDefinitionId(5);
        step5.setOperationName("auth_otp");
        step5.setOperationRequestType(OperationRequestType.UPDATE);
        step5.setRequestAuthMethod(AuthMethod.SMS_KEY);
        step5.setResponsePriority(1);
        step5.setRequestAuthStepResult(AuthStepResult.AUTH_FAILED);
        step5.setResponseAuthMethod(AuthMethod.SMS_KEY);
        step5.setResponseResult(AuthResult.CONTINUE);
        nextStepClient.createStepDefinition(step5);
        CreateStepDefinitionRequest step6 = new CreateStepDefinitionRequest();
        step6.setStepDefinitionId(6);
        step6.setOperationName("auth_otp");
        step6.setOperationRequestType(OperationRequestType.UPDATE);
        step6.setRequestAuthMethod(AuthMethod.SMS_KEY);
        step6.setResponsePriority(1);
        step6.setRequestAuthStepResult(AuthStepResult.AUTH_METHOD_FAILED);
        step6.setResponseResult(AuthResult.FAILED);
        nextStepClient.createStepDefinition(step6);

        // Configure Next Step application
        nextStepClient.createApplication("TEST_APP", "Test application", "RETAIL");

        // Create credential policy
        CreateCredentialPolicyRequest credentialPolicyRequest = new CreateCredentialPolicyRequest();
        credentialPolicyRequest.setCredentialPolicyName("TEST_CREDENTIAL_POLICY");
        credentialPolicyRequest.setLimitSoft(3);
        credentialPolicyRequest.setLimitHard(5);
        credentialPolicyRequest.setUsernameLengthMin(8);
        credentialPolicyRequest.setUsernameLengthMax(30);
        credentialPolicyRequest.setUsernameGenAlgorithm("DEFAULT");
        credentialPolicyRequest.setCredentialLengthMin(8);
        credentialPolicyRequest.setCredentialLengthMin(30);
        credentialPolicyRequest.setCredentialGenAlgorithm("DEFAULT");
        nextStepClient.createCredentialPolicy(credentialPolicyRequest);

        // Create credential definition
        CreateCredentialDefinitionRequest credentialDefinitionRequest = new CreateCredentialDefinitionRequest();
        credentialDefinitionRequest.setCredentialDefinitionName("TEST_CREDENTIAL");
        credentialDefinitionRequest.setApplicationName("TEST_APP");
        credentialDefinitionRequest.setCredentialPolicyName("TEST_CREDENTIAL_POLICY");
        credentialDefinitionRequest.setCategory(CredentialCategory.PASSWORD);
        nextStepClient.createCredentialDefinition(credentialDefinitionRequest);

        // Create OTP policy
        CreateOtpPolicyRequest otpPolicyRequest = new CreateOtpPolicyRequest();
        otpPolicyRequest.setOtpPolicyName("TEST_OTP_POLICY");
        otpPolicyRequest.setAttemptLimit(3);
        otpPolicyRequest.setLength(8);
        otpPolicyRequest.setGenAlgorithm("OTP_DATA_DIGEST");
        nextStepClient.createOtpPolicy(otpPolicyRequest);

        // Create OTP definition
        CreateOtpDefinitionRequest otpDefinitionRequest = new CreateOtpDefinitionRequest();
        otpDefinitionRequest.setOtpDefinitionName("TEST_OTP");
        otpDefinitionRequest.setApplicationName("TEST_APP");
        otpDefinitionRequest.setOtpPolicyName("TEST_OTP_POLICY");
        nextStepClient.createOtpDefinition(otpDefinitionRequest);

        // Create user identity with credentials
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUserId("test_user_1");
        CreateUserRequest.NewCredential credential = new CreateUserRequest.NewCredential();
        credential.setCredentialName("TEST_CREDENTIAL");
        credential.setCredentialType(CredentialType.PERMANENT);
        credential.setUsername("testuser");
        credential.setCredentialValue("s3cret");
        createUserRequest.getCredentials().add(credential);
        nextStepClient.createUser(createUserRequest);
    }

}
