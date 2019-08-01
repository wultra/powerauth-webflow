/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsDeliveryResult;

/**
 * Response after creating SMS OTP authorization message.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateSmsAuthorizationResponse {

    /**
     * Message ID.
     */
    private String messageId;

    /**
     * Result of SMS delivery.
     */
    private SmsDeliveryResult smsDeliveryResult;

    /**
     * Error message key.
     */
    private String errorMessage;

    /**
     * Default constructor.
     */
    public CreateSmsAuthorizationResponse() {
    }

    /**
     * Constructor with message ID.
     * @param messageId Message ID.
     */
    public CreateSmsAuthorizationResponse(String messageId) {
        this.messageId = messageId;
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
     * Get result of SMS message delivery.
     * @return Result of SMS message delivery.
     */
    public SmsDeliveryResult getSmsDeliveryResult() {
        return smsDeliveryResult;
    }

    /**
     * Set result of SMS message delivery.
     * @param smsDeliveryResult Result of SMS message delivery.
     */
    public void setSmsDeliveryResult(SmsDeliveryResult smsDeliveryResult) {
        this.smsDeliveryResult = smsDeliveryResult;
    }

    /**
     * Get error message key used in case SMS message could not be delivered.
     * @return Error message key.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message key used in case SMS message could not be delivered.
     * @param errorMessage Error message key.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
