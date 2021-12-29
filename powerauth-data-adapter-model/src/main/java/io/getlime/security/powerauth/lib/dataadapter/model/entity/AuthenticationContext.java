/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PasswordProtectionType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsAuthorizationResult;

/**
 * Authentication context stores context data related to user authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthenticationContext {

    /**
     * Password protection defines how password is protected during transfer.
     */
    private PasswordProtectionType passwordProtection;

    /**
     * Encryption cipher transformation in case password is encrypted.
     *
     * See: https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
     */
    private String cipherTransformation;

    /**
     * Result of previous SMS authorization code verification (optional).
     */
    private SmsAuthorizationResult smsAuthorizationResult;

    /**
     * Default constructor.
     */
    public AuthenticationContext() {
    }

    /**
     * Constructor with authentication context details.
     * @param passwordProtection Password protection type.
     * @param cipherTransformation Encryption cipher transformation.
     */
    public AuthenticationContext(PasswordProtectionType passwordProtection, String cipherTransformation) {
        this.passwordProtection = passwordProtection;
        this.cipherTransformation = cipherTransformation;
    }

    /**
     * Constructor with authentication context details and SMS authorization result.
     * @param passwordProtection Password protection type.
     * @param cipherTransformation Encryption cipher transformation.
     * @param smsAuthorizationResult SMS verification result.
     */
    public AuthenticationContext(PasswordProtectionType passwordProtection, String cipherTransformation, SmsAuthorizationResult smsAuthorizationResult) {
        this.passwordProtection = passwordProtection;
        this.cipherTransformation = cipherTransformation;
        this.smsAuthorizationResult = smsAuthorizationResult;
    }

    /**
     * Get SMS password protection type.
     * @return SMS password protection type.
     */
    public PasswordProtectionType getPasswordProtection() {
        return passwordProtection;
    }

    /**
     * Set SMS password protection type.
     * @param passwordProtection SMS password protection type.
     */
    public void setPasswordProtection(PasswordProtectionType passwordProtection) {
        this.passwordProtection = passwordProtection;
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
     * Get SMS verification result (used optionally in case of SMS authorization verification with password).
     * @return SMS verification result.
     */
    public SmsAuthorizationResult getSmsAuthorizationResult() {
        return smsAuthorizationResult;
    }

    /**
     * Set SMS verification result (used optionally in case of SMS authorization verification with password).
     * @param smsAuthorizationResult SMS verification result.
     */
    public void setSmsAuthorizationResult(SmsAuthorizationResult smsAuthorizationResult) {
        this.smsAuthorizationResult = smsAuthorizationResult;
    }
}
