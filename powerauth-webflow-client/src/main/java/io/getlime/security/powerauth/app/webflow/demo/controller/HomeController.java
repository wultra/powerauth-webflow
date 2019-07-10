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

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.webflow.demo.model.PaymentForm;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.data.OperationDataBuilder;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
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
import java.util.Collections;

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
    public String home(Model model, HttpSession session) {

        // Fetch operation ID, if any
        String operationId;
        synchronized (session.getServletContext()) {
            operationId = (String) session.getAttribute("operationId");
            session.removeAttribute("operationId");
        }

        // Add attributes
        model.addAttribute("connectionMap", getConnectionRepository().findAllConnections());
        model.addAttribute("providerIds", connectionFactoryLocator.registeredProviderIds());
        model.addAttribute("operationId", operationId);

        PaymentForm paymentForm;
        synchronized (session.getServletContext()) {
            paymentForm = (PaymentForm) session.getAttribute("paymentForm");
            if (paymentForm == null) {
                // MOCK PAYMENT
                paymentForm = new PaymentForm();
                paymentForm.setAmount(BigDecimal.valueOf(100));
                paymentForm.setCurrency("CZK");
                paymentForm.setAccount("238400856/0300");
                paymentForm.setNote("Utility Bill Payment - 05/2017");
                paymentForm.setDueDate("2017-06-29");
            } else {
                session.removeAttribute("paymentForm");
            }
        }

        model.addAttribute("paymentForm", paymentForm);

        return "home";
    }

    @RequestMapping("/payment/create")
    public String payment(@ModelAttribute PaymentForm paymentForm, HttpSession session) throws NextStepServiceException {
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

        // Sample party information added to form data.
        // PartyInfo partyInfo = new PartyInfo();
        // partyInfo.setName("Tesco");
        // partyInfo.setLogoUrl("https://itesco.cz/img/logo/logo.svg");
        // partyInfo.setDescription("Objevte více příběhů psaných s chutí");
        // partyInfo.setWebsiteUrl("https://itesco.cz/hello/vse-o-jidle/pribehy-psane-s-chuti/clanek/tomovy-burgery-pro-zapalene-fanousky/15012");
        // formData.addPartyInfo("operation.partyInfo", partyInfo);


        final String operationName = "authorize_payment";
        final GetOperationConfigDetailResponse operationConfig = client.getOperationConfigDetail(operationName).getResponseObject();

        String operationData = new OperationDataBuilder()
                .templateVersion(operationConfig.getTemplateVersion())
                .templateId(operationConfig.getTemplateId())
                .attr1().amount(paymentForm.getAmount(), paymentForm.getCurrency())
                .attr2().accountGeneric(paymentForm.getAccount())
                .attr4().date(paymentForm.getDueDate())
                .attr5().note(paymentForm.getNote())
                .build();

        ApplicationContext applicationContext = createApplicationContext();

        final ObjectResponse<CreateOperationResponse> payment = client.createOperation(operationName, operationData, formData, null, applicationContext);
        synchronized (session.getServletContext()) {
            session.setAttribute("operationId", payment.getResponseObject().getOperationId());
            session.setAttribute("paymentForm", paymentForm);
        }

        return "redirect:/";
    }

    @RequestMapping("/login/sca/create")
    public String loginSca(HttpSession session) throws NextStepServiceException {
        final String operationName = "login_sca";
        final GetOperationConfigDetailResponse operationConfig = client.getOperationConfigDetail(operationName).getResponseObject();

        String operationData = new OperationDataBuilder()
                .templateVersion(operationConfig.getTemplateVersion())
                .templateId(operationConfig.getTemplateId())
                .build();

        OperationFormData formData = new OperationFormData();
        formData.addTitle("login.title");
        formData.addGreeting("login.greeting");
        formData.addSummary("login.summary");

        ApplicationContext applicationContext = createApplicationContext();

        ObjectResponse<CreateOperationResponse> objectResponse = client.createOperation(operationName, operationData, formData, null, applicationContext);
        String operationId = objectResponse.getResponseObject().getOperationId();
        synchronized (session.getServletContext()) {
            session.setAttribute("operationId", operationId);
        }
        return "redirect:/";
    }

    @RequestMapping("/payment/sca/create")
    public String paymentSca(@ModelAttribute PaymentForm paymentForm, HttpSession session) throws NextStepServiceException {
        OperationFormData formData = new OperationFormData();
        formData.addTitle("operation.title");
        formData.addGreeting("operation.greeting");
        formData.addSummary("operation.summary");
        formData.addAmount("operation.amount", paymentForm.getAmount(), "operation.currency", paymentForm.getCurrency());
        formData.addKeyValue("operation.account", paymentForm.getAccount(), ValueFormatType.ACCOUNT);
        formData.addKeyValue("operation.dueDate", paymentForm.getDueDate(), ValueFormatType.DATE);
        formData.addNote("operation.note", paymentForm.getNote(), ValueFormatType.TEXT);

        final String operationName = "authorize_payment_sca";
        final GetOperationConfigDetailResponse operationConfig = client.getOperationConfigDetail(operationName).getResponseObject();

        String operationData = new OperationDataBuilder()
                .templateVersion(operationConfig.getTemplateVersion())
                .templateId(operationConfig.getTemplateId())
                .attr1().amount(paymentForm.getAmount(), paymentForm.getCurrency())
                .attr2().accountGeneric(paymentForm.getAccount())
                .attr4().date(paymentForm.getDueDate())
                .attr5().note(paymentForm.getNote())
                .build();

        ApplicationContext applicationContext = createApplicationContext();

        final ObjectResponse<CreateOperationResponse> payment = client.createOperation(operationName, operationData, formData, null, applicationContext);
        synchronized (session.getServletContext()) {
            session.setAttribute("operationId", payment.getResponseObject().getOperationId());
            session.setAttribute("paymentForm", paymentForm);
        }

        return "redirect:/";
    }

    private ConnectionRepository getConnectionRepository() {
        return connectionRepositoryProvider.get();
    }

    private ApplicationContext createApplicationContext() {
        // Sample specification of ApplicationContext for OAuth 2.0 consent screen
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setId("DEMO");
        applicationContext.setName("Demo application");
        applicationContext.setDescription("Web Flow demo application");
        applicationContext.getExtras().put("_requestedScopes", Collections.singletonList("OAUTH"));
        applicationContext.getExtras().put("applicationOwner", "Wultra");
        return applicationContext;
    }
}