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
package io.getlime.security.powerauth.lib.bankadapter.repository.model.entity;

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
@Table(name = "ba_sms_authorization")
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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public int getVerifyRequestCount() {
        return verifyRequestCount;
    }

    public void setVerifyRequestCount(int verifyRequestCount) {
        this.verifyRequestCount = verifyRequestCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public Date getTimestampVerified() {
        return timestampVerified;
    }

    public void setTimestampVerified(Date timestampVerified) {
        this.timestampVerified = timestampVerified;
    }

    public Date getTimestampExpires() {
        return timestampExpires;
    }

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
