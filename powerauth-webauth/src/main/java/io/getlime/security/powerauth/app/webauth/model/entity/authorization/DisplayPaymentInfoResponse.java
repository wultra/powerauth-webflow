/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webauth.model.entity.authorization;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;

import java.math.BigDecimal;

/**
 * Models a display payment info response sent to the client.
 *
 * TODO - needs to be generalized to operations instead of payments.
 *
 * @author Roman Strobl
 */
public class DisplayPaymentInfoResponse extends WebSocketJsonMessage {

    private String operationId;
    private BigDecimal amount;
    private String currency;

    /**
     * Empty constructor.
     */
    public DisplayPaymentInfoResponse() {
    }

    /**
     * Constructor with all parameters for convenience.
     * @param sessionId websocket session id
     * @param operationId operation id
     * @param amount amount in this payment
     * @param currency currency of this payment
     */
    public DisplayPaymentInfoResponse(String sessionId, String operationId, BigDecimal amount, String currency) {
        this.action = WebAuthAction.DISPLAY_PAYMENT_INFO;
        this.sessionId = sessionId;
        this.operationId = operationId;
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * Gets the operation id.
     * @return operation id
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the amount for the payment.
     * @return payment amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Gets the currency of this payment.
     * @return currency of the payment
     */
    public String getCurrency() {
        return currency;
    }
}
