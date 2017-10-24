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
import io.getlime.security.powerauth.lib.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.lib.dataadapter.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccount;
import io.getlime.security.powerauth.lib.dataadapter.model.request.BankAccountListRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.BankAccountListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller class which handles retrieving bank account details.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping("/api/auth/account")
public class BankAccountController {

    private DataAdapter dataAdapter;

    @Autowired
    public BankAccountController(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }

    /**
     * Fetch user bank account details.
     *
     * @param request Request with user ID.
     * @return Response with user details.
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<BankAccountListResponse> fetchBankAccounts(@RequestBody ObjectRequest<BankAccountListRequest> request) throws MethodArgumentNotValidException, UserNotFoundException {
        BankAccountListRequest bankAccountListRequest = request.getRequestObject();
        String userId = bankAccountListRequest.getUserId();
        List<BankAccount> bankAccounts = dataAdapter.fetchBankAccounts(userId);
        BankAccountListResponse response = new BankAccountListResponse(userId, bankAccounts);
        return new ObjectResponse<>(response);
    }


}
