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
package io.getlime.security.powerauth.app.webauth.demo.controller;

import java.math.BigDecimal;
import java.security.Principal;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.webauth.demo.model.PaymentForm;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationAmountDisplayAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationDisplayDetails;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationKeyValueDisplayAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationMessageDisplayAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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
        OperationDisplayDetails details = new OperationDisplayDetails();
        details.setTitle("Confirm Payment");
        details.setMessage("Hello, please confirm payment "
                + paymentForm.getAmount() + " " + paymentForm.getCurrency()
                + " to account " + paymentForm.getAccount() + ".");

        OperationAmountDisplayAttribute amountAttr = new OperationAmountDisplayAttribute();
        amountAttr.setLabel("Amout");
        amountAttr.setAmount(paymentForm.getAmount());
        amountAttr.setCurrency(paymentForm.getCurrency());
        details.getParameters().add(amountAttr);

        OperationKeyValueDisplayAttribute accountAttr = new OperationKeyValueDisplayAttribute();
        accountAttr.setLabel("To Account");
        accountAttr.setValue(paymentForm.getAccount());
        details.getParameters().add(accountAttr);

        OperationKeyValueDisplayAttribute dateAttr = new OperationKeyValueDisplayAttribute();
        dateAttr.setLabel("Due Date");
        dateAttr.setValue("06/29/2017");
        details.getParameters().add(dateAttr);

        OperationMessageDisplayAttribute messageAttr = new OperationMessageDisplayAttribute();
        messageAttr.setLabel("Note");
        messageAttr.setMessage(paymentForm.getNote());
        details.getParameters().add(messageAttr);

        final Response<CreateOperationResponse> payment = client.createOperation("authorize_payment", data, details, null);
        session.setAttribute("operationId", payment.getResponseObject().getOperationId());



        return "redirect:/";
    }

    private ConnectionRepository getConnectionRepository() {
        return connectionRepositoryProvider.get();
    }
}