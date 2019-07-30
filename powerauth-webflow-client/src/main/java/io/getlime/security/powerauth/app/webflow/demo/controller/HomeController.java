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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.webflow.demo.model.AvailableOperation;
import io.getlime.security.powerauth.app.webflow.demo.model.PaymentForm;
import io.getlime.security.powerauth.app.webflow.demo.model.ScaForm;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.data.OperationDataBuilder;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default demo controller class.
 */
@Controller
public class HomeController {

    private final Provider<ConnectionRepository> connectionRepositoryProvider;
    private final ConnectionFactoryLocator connectionFactoryLocator;
    private final NextStepClient client;
    private final HttpSession httpSession;

    private final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    public HomeController(Provider<ConnectionRepository> connectionRepositoryProvider, ConnectionFactoryLocator connectionFactoryLocator, NextStepClient client, HttpSession httpSession) {
        this.connectionRepositoryProvider = connectionRepositoryProvider;
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.client = client;
        this.httpSession = httpSession;
    }

    @RequestMapping("/")
    public String home(Model model) throws NextStepServiceException {

        // Fetch operation ID, if any
        String operationId;
        synchronized (httpSession.getServletContext()) {
            operationId = (String) httpSession.getAttribute("operationId");
            httpSession.removeAttribute("operationId");
        }

        // Add attributes
        model.addAttribute("connectionMap", getConnectionRepository().findAllConnections());
        model.addAttribute("providerIds", connectionFactoryLocator.registeredProviderIds());
        model.addAttribute("operationId", operationId);

        PaymentForm paymentForm, paymentFormSca;
        ScaForm loginFormSca;
        synchronized (httpSession.getServletContext()) {
            paymentForm = (PaymentForm) httpSession.getAttribute("paymentForm");
            if (paymentForm == null) {
                paymentForm = createDemoPaymentForm(false);
            } else {
                httpSession.removeAttribute("paymentForm");
            }
            paymentFormSca = (PaymentForm) httpSession.getAttribute("paymentFormSca");
            if (paymentFormSca == null) {
                paymentFormSca = createDemoPaymentForm(true);
            } else {
                httpSession.removeAttribute("paymentFormSca");
            }
            loginFormSca = (ScaForm) httpSession.getAttribute("loginFormSca");
            if (loginFormSca == null) {
                loginFormSca = new ScaForm();
                loginFormSca.setAppContext(createApplicationContext(Collections.singletonList("AISP")));
            } else {
                httpSession.removeAttribute("loginFormSca");
            }
        }

        model.addAttribute("paymentForm", paymentForm);
        model.addAttribute("paymentFormSca", paymentFormSca);
        model.addAttribute("loginFormSca", loginFormSca);

        ArrayList<AvailableOperation> operations = new ArrayList<>();

        for (GetOperationConfigDetailResponse config : client.getOperationConfigList().getResponseObject().getOperationConfigs()) {
            String name;
            AvailableOperation.Type type;
            switch (config.getOperationName()) {
                case "login_sca":
                    name = "Login SCA";
                    type = AvailableOperation.Type.LOGIN_SCA;
                    break;
                case "authorize_payment_sca":
                    name = "Payment SCA";
                    type = AvailableOperation.Type.PAYMENT_SCA;
                    break;
                case "login":
                    name = "Login";
                    type = AvailableOperation.Type.LOGIN;
                    break;
                case "authorize_payment":
                    name = "Payment";
                    type = AvailableOperation.Type.PAYMENT;
                    break;
                default:
                    // Allow other operation names, client can have additional operations in Next Step which are
                    // not supported by Web Flow.
                    continue;
            }
            operations.add(new AvailableOperation(type, name));
        }

        operations.add(new AvailableOperation(AvailableOperation.Type.AUTHORIZATION, "Authorization"));

        if (!operations.isEmpty()) {
            operations.stream().filter(x -> x.getType() == AvailableOperation.Type.PAYMENT_SCA).findFirst().orElse(operations.get(0)).setDefault(true);
        }

        model.addAttribute("availableOperations", operations);

        return "home";
    }

    @RequestMapping("/payment/create")
    public String payment(@ModelAttribute PaymentForm paymentForm) throws NextStepServiceException {
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

        ApplicationContext applicationContext = getApplicationContext(paymentForm);
        final ObjectResponse<CreateOperationResponse> payment = client.createOperation(operationName, operationData, formData, null, applicationContext);
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute("operationId", payment.getResponseObject().getOperationId());
            httpSession.setAttribute("paymentForm", paymentForm);
        }

        return "redirect:/";
    }

    @RequestMapping("/login/sca/create")
    public String loginSca(ScaForm form) throws NextStepServiceException {
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

        ApplicationContext applicationContext = getApplicationContext(form);
        ObjectResponse<CreateOperationResponse> objectResponse = client.createOperation(operationName, operationData, formData, null, applicationContext);
        String operationId = objectResponse.getResponseObject().getOperationId();
        synchronized (httpSession.getServletContext()) {
            httpSession.setAttribute("operationId", operationId);
            httpSession.setAttribute("loginFormSca", form);
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

        ApplicationContext applicationContext = getApplicationContext(paymentForm);
        final ObjectResponse<CreateOperationResponse> payment = client.createOperation(operationName, operationData, formData, null, applicationContext);
        synchronized (session.getServletContext()) {
            session.setAttribute("operationId", payment.getResponseObject().getOperationId());
            session.setAttribute("paymentFormSca", paymentForm);
        }

        return "redirect:/";
    }

    private ConnectionRepository getConnectionRepository() {
        return connectionRepositoryProvider.get();
    }

    private ApplicationContext getApplicationContext(ScaForm form) throws NextStepServiceException {
        try {
            return new ObjectMapper().readValue(form.getAppContext(), ApplicationContext.class);
        } catch (Exception e) {
            throw new NextStepServiceException("Cannot deserialize ApplicationContext");
        }
    }

    private String createApplicationContext(List<String> requestedScopes) throws NextStepServiceException {
        // Sample specification of ApplicationContext for OAuth 2.0 consent screen
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setId("DEMO");
        applicationContext.setName("Demo application");
        applicationContext.setDescription("Web Flow demo application");
        applicationContext.getExtras().put("_requestedScopes", requestedScopes);
        applicationContext.getExtras().put("applicationOwner", "Wultra");

        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(applicationContext);
        } catch (Exception e) {
            throw new NextStepServiceException("Cannot serialize Application Context");
        }
    }

    private PaymentForm createDemoPaymentForm(boolean isSca) throws NextStepServiceException {
        PaymentForm paymentForm = new PaymentForm();
        paymentForm.setAmount(BigDecimal.valueOf(100));
        paymentForm.setCurrency("CZK");
        paymentForm.setAccount("238400856/0300");
        paymentForm.setNote("Utility Bill Payment - 05/2019");
        paymentForm.setDueDate("2019-06-29");
        paymentForm.setAppContext(createApplicationContext(Collections.singletonList(isSca ? "PISP" : "OAUTH")));
        return paymentForm;
    }
}