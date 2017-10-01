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

package io.getlime.security.powerauth.lib.bankadapter.model.response;

import io.getlime.security.powerauth.lib.bankadapter.model.entity.BankAccountEntity;

import java.util.List;

/**
 * Response with list of user bank accounts.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class BankAccountListResponse {

    private String userId;
    private List<BankAccountEntity> bankAccounts;

    /**
     * Default constructor.
     */
    public BankAccountListResponse() {
    }

    /**
     * Constructor with user ID and bank account list.
     * @param userId User ID.
     * @param bankAccounts Bank account list.
     */
    public BankAccountListResponse(String userId, List<BankAccountEntity> bankAccounts) {
        this.userId = userId;
        this.bankAccounts = bankAccounts;
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
     * Get list of bank accounts.
     * @return List of bank accounts.
     */
    public List<BankAccountEntity> getBankAccounts() {
        return bankAccounts;
    }

    /**
     * Set list of bank accounts.
     * @param bankAccounts List of bank accounts.
     */
    public void setBankAccounts(List<BankAccountEntity> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }
}