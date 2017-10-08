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
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthMethodChoiceEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccountChoiceEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChangeEntity;
import io.getlime.security.powerauth.lib.dataadapter.model.request.FormDataChangeNotificationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class which handles notifications about changes of operation formData.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping("/api/operation/formData")
public class FormDataChangeController {

    /**
     * Receive a new operation formData change notification.
     *
     * @param request Request with change details.
     * @return Object response.
     */
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse formDataChangedNotification(@RequestBody ObjectRequest<FormDataChangeNotificationRequest> request) {
        FormDataChangeNotificationRequest notification = request.getRequestObject();
        FormDataChangeEntity change = notification.getFormDataChange();
        switch (change.getType()) {
            case BANK_ACCOUNT_CHOICE:
                bankAccountChosen((BankAccountChoiceEntity) change, notification.getUserId(), notification.getOperationId());
                break;
            case AUTH_METHOD_CHOICE:
                authMethodChosen((AuthMethodChoiceEntity) change, notification.getUserId(), notification.getOperationId());
                break;
            default:
                throw new IllegalStateException("Invalid change entity type: " + change.getType());
        }
        return new ObjectResponse();
    }

    private void bankAccountChosen(BankAccountChoiceEntity bankAccountChoice, String userId, String operationId) {
        // Handle bank account choice here (e.g. send notification to bank backend).
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Bank account chosen: " + bankAccountChoice.getBankAccountNumber() + ", user: " + userId + ", operationId: " + operationId);
    }

    private void authMethodChosen(AuthMethodChoiceEntity authMethodChoice, String userId, String operationId) {
        // Handle authorization method choice here (e.g. send notification to bank backend).
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Authorization method chosen: " + authMethodChoice.getChosenAuthMethod() + ", user: " + userId + ", operationId: " + operationId);
    }

}
