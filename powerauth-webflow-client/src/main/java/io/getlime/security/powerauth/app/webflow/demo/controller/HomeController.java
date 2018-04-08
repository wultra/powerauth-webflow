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
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;
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
        paymentForm.setDueDate("2017-06-29");
        model.addAttribute("paymentForm", paymentForm);

        return "home";
    }

    @RequestMapping("/payment/create")
    public String payment(Principal currentUser, @ModelAttribute PaymentForm paymentForm, HttpSession session) throws JsonProcessingException, NextStepServiceException {
        String data = new ObjectMapper().writeValueAsString(paymentForm);
        OperationFormData formData = new OperationFormData();
        formData.addTitle("operation.title");
        formData.addGreeting("operation.greeting");
        formData.addSummary("operation.summary");
        formData.addAmount("operation.amount", paymentForm.getAmount(), "operation.currency", paymentForm.getCurrency());
        formData.addKeyValue("operation.account", paymentForm.getAccount(), ValueFormatType.ACCOUNT);
        formData.addKeyValue("operation.dueDate", paymentForm.getDueDate(), ValueFormatType.DATE);
        formData.addNote("operation.note", paymentForm.getNote(), ValueFormatType.TEXT);

        // Sample operation configuration for bank account choice select.
        // OperationFormFieldConfig bankAccountConfig = new OperationFormFieldConfig();
        // bankAccountConfig.setId("operation.bankAccountChoice");
        // bankAccountConfig.setEnabled(false);
        // bankAccountConfig.setDefaultValue("CZ4043210000000087654321");
        // formData.getConfig().add(bankAccountConfig);

        // Sample banners displayed above the operation details.
        // formData.addBanner(BannerType.BANNER_ERROR, "banner.error");
        // formData.addBanner(BannerType.BANNER_WARNING, "banner.warning");
        // formData.addBanner(BannerType.BANNER_INFO, "banner.info");

        final ObjectResponse<CreateOperationResponse> payment = client.createOperation("authorize_payment", data, formData, null);
        session.setAttribute("operationId", payment.getResponseObject().getOperationId());

        return "redirect:/";
    }

    private ConnectionRepository getConnectionRepository() {
        return connectionRepositoryProvider.get();
    }
}