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
package io.getlime.security.powerauth.lib.dataadapter.repository.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores SMS OTP authorization messages and related data.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Entity
@Table(name = "da_sms_authorization")
public class SMSAuthorizationEntity implements Serializable {

    private static final long serialVersionUID = 6432269422572862762L;

    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "operation_name")
    private String operationName;

    @Column(name = "authorization_code")
    private String authorizationCode;

    @Column(name = "salt")
    private byte[] salt;

    @Column(name = "message_text")
    private String messageText;

    @Column(name = "verify_request_count")
    private int verifyRequestCount;

    @Column(name = "verified")
    private boolean verified;

    @Column(name = "timestamp_created")
    private Date timestampCreated;

    @Column(name = "timestamp_verified")
    private Date timestampVerified;

    @Column(name = "timestamp_expires")
    private Date timestampExpires;

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
     * Get operation ID.
     * @return Operation ID.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set operation ID.
     * @param operationId Operation ID.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID.
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get operation name.
     * @return Operation name.
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set operation name.
     * @param operationName Operation name.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Get OTP authorization code.
     * @return OTP authorization code.
     */
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    /**
     * Set OTP authorization code.
     * @param authorizationCode OPT authorization code.
     */
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    /**
     * Get salt bytes used when generating OTP.
     * @return Salt bytes.
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Set salt bytes used when generating OTP.
     * @param salt Salt bytes.
     */
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    /**
     * Get localized message text.
     * @return Message text.
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Set localized message text.
     * @param messageText Message text.
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /**
     * Get verification request count.
     * @return Verification request count.
     */
    public int getVerifyRequestCount() {
        return verifyRequestCount;
    }

    /**
     * Set verification request count.
     * @param verifyRequestCount Verification request count.
     */
    public void setVerifyRequestCount(int verifyRequestCount) {
        this.verifyRequestCount = verifyRequestCount;
    }

    /**
     * Whether SMS OTP authorization code is verified.
     * @return Whether authorization code is verified.
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * Set whether SMS OTP authorization code is verified.
     * @param verified Whether authorization code is verified.
     */
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Get timestamp when authorization SMS was created.
     * @return Timestamp when authorization SMS was created.
     */
    public Date getTimestampCreated() {
        return timestampCreated;
    }

    /**
     * Set timestamp when authorization SMS was created.
     * @param timestampCreated Timestamp when authorization SMS was created.
     */
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * Get timestamp when authorization SMS was verified.
     * @return Timestamp when authorization SMS was verified.
     */
    public Date getTimestampVerified() {
        return timestampVerified;
    }

    /**
     * Set timestamp when authorization SMS was verified.
     * @param timestampVerified Timestamp when authorization SMS was verified.
     */
    public void setTimestampVerified(Date timestampVerified) {
        this.timestampVerified = timestampVerified;
    }

    /**
     * Get timestamp when authorization SMS expires.
     * @return Timestamp when authorization SMS expires.
     */
    public Date getTimestampExpires() {
        return timestampExpires;
    }

    /**
     * Set timestamp when authorization SMS expires.
     * @param timestampExpires Timestamp when authorization SMS expires.
     */
    public void setTimestampExpires(Date timestampExpires) {
        this.timestampExpires = timestampExpires;
    }

    /**
     * Is the SMS OTP expired?
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return new Date().after(timestampExpires);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SMSAuthorizationEntity that = (SMSAuthorizationEntity) o;

        return messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        return messageId.hashCode();
    }
}
