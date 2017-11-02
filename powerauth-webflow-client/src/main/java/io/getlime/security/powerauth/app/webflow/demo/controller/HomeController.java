/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.app.webflow.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.webflow.demo.model.PaymentForm;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationAmountAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationKeyValueAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationMessageAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.security.Principal;

/**
 * Default demo controller class.
 */
@Controller
public class HomeController {

    private final Provider<ConnectionRepository> connectionRepositoryProvider;
    private final ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    private NextStepClient client;

    @Inject
    public HomeController(Provider<ConnectionRepository> connectionRepositoryProvider, ConnectionFactoryLocator connectionFactoryLocator) {
        this.connectionRepositoryProvider = connectionRepositoryProvider;
        this.connectionFactoryLocator = connectionFactoryLocator;
    }

    @RequestMapping("/")
    public String home(Principal currentUser, Model model, HttpSession session) {

        // Fetch operation ID, if any
        String operationId = (String) session.getAttribute("operationId");
        session.removeAttribute("operationId");

        // Add attributes
        model.addAttribute("connectionMap", getConnectionRepository().findAllConnections());
        model.addAttribute("providerIds", connectionFactoryLocator.registeredProviderIds());
        model.addAttribute("operationId", operationId);

        // MOCK PAYMENT
        final PaymentForm paymentForm = new PaymentForm();
        paymentForm.setAmount(BigDecimal.valueOf(100));
        paymentForm.setCurrency("CZK");
        paymentForm.setAccount("238400856/0300");
        paymentForm.setNote("Utility Bill Payment - 05/2017");
        model.addAttribute("paymentForm", paymentForm);

        return "home";
    }

    @RequestMapping("/payment/create")
    public String payment(Principal currentUser, @ModelAttribute PaymentForm paymentForm, HttpSession session) throws JsonProcessingException, NextStepServiceException {
        String data = new ObjectMapper().writeValueAsString(paymentForm);
        OperationFormData formData = new OperationFormData();
        formData.setTitle("Confirm Payment");
        formData.setMessage("Hello, please confirm payment "
                + paymentForm.getAmount() + " " + paymentForm.getCurrency()
                + " to account " + paymentForm.getAccount() + ".");

        OperationAmountAttribute amountAttr = new OperationAmountAttribute();
        amountAttr.setLabel("Amount");
        amountAttr.setAmount(paymentForm.getAmount());
        amountAttr.setCurrency(paymentForm.getCurrency());
        formData.getParameters().add(amountAttr);

        OperationKeyValueAttribute accountAttr = new OperationKeyValueAttribute();
        accountAttr.setLabel("To Account");
        accountAttr.setValue(paymentForm.getAccount());
        formData.getParameters().add(accountAttr);

        OperationKeyValueAttribute dateAttr = new OperationKeyValueAttribute();
        dateAttr.setLabel("Due Date");
        dateAttr.setValue("06/29/2017");
        formData.getParameters().add(dateAttr);

        OperationMessageAttribute messageAttr = new OperationMessageAttribute();
        messageAttr.setLabel("Note");
        messageAttr.setMessage(paymentForm.getNote());
        formData.getParameters().add(messageAttr);

        final ObjectResponse<CreateOperationResponse> payment = client.createOperation("authorize_payment", data, formData, null);
        session.setAttribute("operationId", payment.getResponseObject().getOperationId());

        return "redirect:/";
    }

    private ConnectionRepository getConnectionRepository() {
        return connectionRepositoryProvider.get();
    }
}