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
package io.getlime.security.powerauth.lib.dataadapter.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccountEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.request.BankAccountListRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.BankAccountListResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class which handles retrieving bank account details.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping("/api/auth/account")
public class BankAccountController {

    /**
     * Fetch user bank account details.
     *
     * @param request Request with user ID.
     * @return Response with user details.
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<BankAccountListResponse> fetchBankAccounts(@RequestBody ObjectRequest<BankAccountListRequest> request) {
        BankAccountListRequest bankAccountListRequest = request.getRequestObject();
        String userId = bankAccountListRequest.getUserId();

        // Fetch bank account list for given user here from the bank backend.
        BankAccountListResponse responseObject = new BankAccountListResponse();
        responseObject.setUserId(userId);

        // Replace mock bank account data with real data loaded from the bank backend.
        // In case the bank account selection is disabled, return an empty list.
        List<BankAccountEntity> bankAccounts = new ArrayList<>();

        BankAccountEntity bankAccount1 = new BankAccountEntity();
        bankAccount1.setName("Běžný účet v CZK");
        bankAccount1.setBalance(new BigDecimal("24394.52"));
        bankAccount1.setNumber("12345678/1234");
        bankAccount1.setCurrency("CZK");
        bankAccount1.setUsableForPayment(true);
        bankAccounts.add(bankAccount1);

        BankAccountEntity bankAccount2 = new BankAccountEntity();
        bankAccount2.setName("Spořící účet v CZK");
        bankAccount2.setBalance(new BigDecimal("158121.10"));
        bankAccount2.setNumber("87654321/4321");
        bankAccount2.setCurrency("CZK");
        bankAccount2.setUsableForPayment(true);
        bankAccounts.add(bankAccount2);

        BankAccountEntity bankAccount3 = new BankAccountEntity();
        bankAccount3.setName("Spořící účet v EUR");
        bankAccount3.setBalance(new BigDecimal("1.90"));
        bankAccount3.setNumber("44444444/1111");
        bankAccount3.setCurrency("EUR");
        bankAccount3.setUsableForPayment(false);
        bankAccount3.setUnusableForPaymentReason("operationReview.balanceTooLow");
        bankAccounts.add(bankAccount3);

        responseObject.setBankAccounts(bankAccounts);
        return new ObjectResponse<>(responseObject);
    }


}
