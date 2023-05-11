/*
 * Copyright 2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.app.webflow.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.webflow.demo.model.AvailableOperation;
import io.getlime.security.powerauth.app.webflow.demo.model.OperationForm;
import io.getlime.security.powerauth.app.webflow.demo.model.PaymentForm;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default demo controller class.
 */
@Controller
public class HomeController {

    private final HttpSession httpSession;

    @Autowired
    public HomeController(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @RequestMapping("/")
    public String home(Model model, OAuth2AuthenticationToken user) throws NextStepServiceException {

        // Fetch operation ID, if any
        String operationId;
        synchronized (httpSession.getServletContext()) {
            operationId = (String) httpSession.getAttribute("operationId");
            httpSession.removeAttribute("operationId");
        }

        // Add attributes
        model.addAttribute("operationId", operationId);
        model.addAttribute("authenticated", user != null);
        if (user != null) {
            model.addAttribute("userName", user.getName());
        }

        PaymentForm paymentForm, paymentFormSca;
        OperationForm loginFormSca;
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
            loginFormSca = (OperationForm) httpSession.getAttribute("loginFormSca");
            if (loginFormSca == null) {
                loginFormSca = new OperationForm();
                loginFormSca.setAppContext(createApplicationContext(Collections.singletonList("aisp")));
            } else {
                httpSession.removeAttribute("loginFormSca");
            }
        }

        model.addAttribute("paymentForm", paymentForm);
        model.addAttribute("paymentFormSca", paymentFormSca);
        model.addAttribute("loginFormSca", loginFormSca);

        ArrayList<AvailableOperation> operations = new ArrayList<>();

        operations.add(new AvailableOperation(AvailableOperation.Type.LOGIN, "Login"));
        operations.add(new AvailableOperation(AvailableOperation.Type.AUTHORIZATION, "Authorization"));

        if (!operations.isEmpty()) {
            operations.stream().filter(x -> x.getType() == AvailableOperation.Type.PAYMENT_SCA).findFirst().orElse(operations.get(0)).setDefault(true);
        }

        model.addAttribute("availableOperations", operations);

        return "home";
    }

    private ApplicationContext getApplicationContext(OperationForm form) throws NextStepServiceException {
        try {
            return new ObjectMapper().readValue(form.getAppContext(), ApplicationContext.class);
        } catch (Exception e) {
            throw new NextStepServiceException("Cannot deserialize ApplicationContext");
        }
    }

    private String createApplicationContext(List<String> requestedScopes) throws NextStepServiceException {
        // Sample specification of ApplicationContext for OAuth 2.1 consent screen
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setId("democlient");
        applicationContext.setName("Demo application");
        applicationContext.setDescription("Web Flow demo application");
        applicationContext.getOriginalScopes().addAll(requestedScopes);
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
        paymentForm.setAppContext(createApplicationContext(Collections.singletonList(isSca ? "pisp" : "oauth")));
        return paymentForm;
    }
}