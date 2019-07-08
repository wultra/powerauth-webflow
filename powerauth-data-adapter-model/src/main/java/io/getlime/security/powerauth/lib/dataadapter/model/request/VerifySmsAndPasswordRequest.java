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
package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthenticationType;

/**
 * Request for SMS OTP message and password verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifySmsAndPasswordRequest {

    /**
     * SMS message ID.
     */
    private String messageId;

    /**
     * SMS OTP authorization code.
     */
    private String authorizationCode;

    /**
     * User ID for this authentication request.
     */
    private String userId;

    /**
     * Password for this authentication request.
     */
    private String password;

    /**
     * Organization ID for this authentication request.
     */
    private String organizationId;

    /**
     * Authentication type which defines how username and password fields are used.
     */
    private AuthenticationType authenticationType;

    /**
     * Encryption cipher transformation in case password is encrypted.
     *
     * See: https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
     */
    private String cipherTransformation;

    /**
     * Operation context.
     */
    private OperationContext operationContext;

    /**
     * Default constructor.
     */
    public VerifySmsAndPasswordRequest() {
    }

    /**
     * Constructor with all details.
     * @param messageId Message ID.
     * @param authorizationCode Authorization code from user.
     * @param userId User ID for this authentication request.
     * @param password Password for this authentication request.
     * @param organizationId Organization ID for this authentication request.
     * @param authenticationType Authentication type.
     * @param cipherTransformation Cipher transformation used in case password is encrypted.
     * @param operationContext Operation context.
     */
    public VerifySmsAndPasswordRequest(String messageId, String authorizationCode, String userId, String password, String organizationId, AuthenticationType authenticationType, String cipherTransformation, OperationContext operationContext) {
        this.messageId = messageId;
        this.authorizationCode = authorizationCode;
        this.userId = userId;
        this.password = password;
        this.organizationId = organizationId;
        this.authenticationType = authenticationType;
        this.cipherTransformation = cipherTransformation;
        this.operationContext = operationContext;
    }

    /**
     * Get message ID.
     * @return Message ID.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Set message ID.
     * @param messageId Message ID.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Get authorization code.
     * @return Authorization code.
     */
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    /**
     * Set authorization code.
     * @param authorizationCode Authorization code.
     */
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    /**
     * Set the user ID.
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the password. The password can be encrypted, in this case the type specifies encryption method and
     * cipherTransformation specifies the algorithm, mode and padding.
     * @param password Password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the password. The password can be encrypted, in this case the authenticationType specifies encryption method and
     * cipherTransformation specifies the algorithm, mode and padding.
     * @return Password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get organization ID.
     * @return Organization ID.
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set organization ID.
     * @param organizationId Organization ID.
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Set the authentication type.
     * @param authenticationType Authentication type.
     */
    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    /**
     * Get the authentication type.
     * @return Authentication type.
     */
    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    /**
     * Get encryption cipher transformation for encrypted requests (e.g. AES/CBC/PKCS7Padding).
     * @return Encryption cipher transformation.
     */
    public String getCipherTransformation() {
        return cipherTransformation;
    }

    /**
     * Set encryption cipher transformation for encrypted requests (e.g. AES/CBC/PKCS7Padding).
     * @param cipherTransformation Encryption cipher transformation.
     */
    public void setCipherTransformation(String cipherTransformation) {
        this.cipherTransformation = cipherTransformation;
    }

    /**
     * Get operation context.
     * @return Operation context.
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context.
     * @param operationContext Operation context.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }
}
