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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsDeliveryResult;

/**
 * Response after sending SMS OTP authorization message.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class SendAuthorizationSmsResponse {

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
    public SendAuthorizationSmsResponse() {
    }

    /**
     * Constructor with message ID.
     * @param messageId Message ID.
     */
    public SendAuthorizationSmsResponse(String messageId) {
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
