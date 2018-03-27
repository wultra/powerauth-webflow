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
package io.getlime.security.powerauth.lib.mtoken.model.entity.attributes;

import java.math.BigDecimal;

/**
 * Attribute representing a financial amount item, with attributes for amount
 * and currency, that can be rendered on a mobile application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AmountAttribute extends Attribute {

    private BigDecimal amount;
    private String currency;

    public AmountAttribute() {
        super();
        this.setType(Type.AMOUNT);
    }

    public AmountAttribute(String id, String label, BigDecimal amount, String currency) {
        this();
        this.id = id;
        this.label = label;
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
