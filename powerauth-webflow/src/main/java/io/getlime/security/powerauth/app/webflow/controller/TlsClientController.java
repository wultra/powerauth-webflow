/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.webflow.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.webflow.exception.TlsClientAuthenticationException;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.HttpSessionAttributeNames;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.CertificateVerificationRepository;
import io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity.CertificateVerificationEntity;
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.x500.X500Principal;
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
    private final AuthenticationManagementService authenticationManagementService;
    private final NextStepClient nextStepClient;
    private final CertificateVerificationRepository certificateVerificationRepository;

    /**
     * TLS client controller constructor.
     * @param httpServletRequest HTTP servlet request.
     * @param httpSession HTTP session.
     * @param authenticationManagementService Authentication management service.
     * @param nextStepClient Next step client.
     * @param certificateVerificationRepository Certificate verification repository.
     */
    @Autowired
    public TlsClientController(HttpServletRequest httpServletRequest, HttpSession httpSession, AuthenticationManagementService authenticationManagementService, NextStepClient nextStepClient, CertificateVerificationRepository certificateVerificationRepository) {
        this.httpServletRequest = httpServletRequest;
        this.httpSession = httpSession;
        this.authenticationManagementService = authenticationManagementService;
        this.nextStepClient = nextStepClient;
        this.certificateVerificationRepository = certificateVerificationRepository;
    }

    @RequestMapping(value = "login", method = { RequestMethod.GET, RequestMethod.POST })
    public void verifyTlsCertificateForLogin() throws TlsClientAuthenticationException {
        logger.info("Received verifyTlsCertificateForLogin request");
        verifyTlsCertificateImpl();
        logger.info("The verifyTlsCertificateForLogin request succeeded");
    }

    @RequestMapping(value = "approve", method = { RequestMethod.GET, RequestMethod.POST })
    public void verifyTlsCertificateForApprove() throws TlsClientAuthenticationException {
        logger.info("Received verifyTlsCertificateForApprove request");
        verifyTlsCertificateImpl();
        logger.info("The verifyTlsCertificateForApprove request succeeded");
    }

    private void verifyTlsCertificateImpl() throws TlsClientAuthenticationException {
        String certificate = httpServletRequest.getHeader("X-Client-Certificate");
        if (certificate == null || certificate.isEmpty()) {
            logger.warn("Verification of TLS certificate failed because certificate is missing");
            throw new InsufficientAuthenticationException("Missing client certificate");
        }

        // Extract operation ID
        UserOperationAuthentication authentication = authenticationManagementService.getPendingUserAuthentication();
        if (authentication == null) {
            logger.warn("Verification of TLS certificate failed because user authentication object is missing");
            throw new InsufficientAuthenticationException("Missing user authentication object");
        }
        String operationId = authentication.getOperationId();
        if (operationId == null) {
            logger.warn("Verification of TLS certificate failed because operation ID is missing");
            throw new InsufficientAuthenticationException("Missing operation ID");
        }

        // Get chosen authentication method for operation
        AuthMethod authMethod;
        GetOperationDetailResponse operation;
        try {
            ObjectResponse<GetOperationDetailResponse> operationResponse = nextStepClient.getOperationDetail(operationId);
            operation = operationResponse.getResponseObject();
            authMethod = operation.getChosenAuthMethod();
        } catch (NextStepClientException ex) {
            logger.error(ex.getMessage(), ex);
            throw new InsufficientAuthenticationException("Could not retrieve operation");
        }

        certificate = certificate.replace(" ", "\n");
        certificate = certificate.replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----");
        certificate = certificate.replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");

        try {
            // Parse certificate and extract its details
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream is = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
            X509Certificate clientCertificate = (X509Certificate) factory.generateCertificate(is);
            X500Principal issuerPrincipal = clientCertificate.getIssuerX500Principal();
            X500Principal subjectPrincipal = clientCertificate.getSubjectX500Principal();

            String issuer = issuerPrincipal.getName();
            String subject = subjectPrincipal.getName();
            String serialNumber = clientCertificate.getSerialNumber().toString();

            // Paranoid check that certificate is valid, this should be already checked by Apache
            clientCertificate.checkValidity();

            // Store information about verified certificate into database
            CertificateVerificationEntity certEntity = new CertificateVerificationEntity(operationId, authMethod, issuer, subject, serialNumber);
            certEntity.setOperationData(operation.getOperationData());
            certificateVerificationRepository.save(certEntity);

            // Save client TLS certificate in HTTP session
            synchronized (httpSession.getServletContext()) {
                httpSession.setAttribute(HttpSessionAttributeNames.CLIENT_CERTIFICATE, certificate);
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            throw new TlsClientAuthenticationException("Invalid client certificate");
        }
    }

}
