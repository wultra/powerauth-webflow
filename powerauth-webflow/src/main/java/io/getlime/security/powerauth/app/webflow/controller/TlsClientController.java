/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.webflow.controller;

import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Controller which verifies TLS client certificate.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("tls/client")
public class TlsClientController {

    private static final Logger logger = LoggerFactory.getLogger(TlsClientController.class);

    private final HttpServletRequest httpServletRequest;
    private final HttpSession httpSession;

    @Autowired
    public TlsClientController(HttpServletRequest httpServletRequest, HttpSession httpSession) {
        this.httpServletRequest = httpServletRequest;
        this.httpSession = httpSession;
    }

    @RequestMapping(value = "login", method = { RequestMethod.GET, RequestMethod.POST })
    public void verifyTlsCertificate() {
        String certificate = httpServletRequest.getHeader("X-Client-Certificate");
        verifyTlsCertificateImpl(certificate);
    }

    private void verifyTlsCertificateImpl(String certificate) {
        if (certificate == null || certificate.isEmpty()) {
            throw new InsufficientAuthenticationException("Missing client certificate");
        }

        certificate = certificate.replace(" ", "\n");
        certificate = certificate.replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----");
        certificate = certificate.replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");

        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream is = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
            X509Certificate clientCertificate = (X509Certificate) factory.generateCertificate(is);
            clientCertificate.checkValidity();
            // Save parsed TLS client certificate in HTTP session
            synchronized (httpSession.getServletContext()) {
                httpSession.setAttribute(HttpSessionAttributeNames.CLIENT_CERTIFICATE, certificate);
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            throw new InsufficientAuthenticationException("Invalid client certificate");
        }
    }

}
