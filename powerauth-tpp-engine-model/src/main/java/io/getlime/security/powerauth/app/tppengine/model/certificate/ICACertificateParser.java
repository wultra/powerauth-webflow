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

package io.getlime.security.powerauth.app.tppengine.model.certificate;

import sun.security.x509.AVA;
import sun.security.x509.X500Name;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Class to parse PSD2 certificates issued by I.CA certificate authority (Czech Republic).
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ICACertificateParser implements ICertificateParser {

    /**
     * Parse certificate in PEM format and return structured information about organization.
     *
     * @param certificatePem Certificate in PEM format.
     * @return Structured certificate information.
     * @throws CertificateException In case certificate cannot be parsed (or in rare case X.509 is not supported).
     */
    public CertInfo parse(String certificatePem) throws CertificateException {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final ByteArrayInputStream bais = new ByteArrayInputStream(certificatePem.getBytes(StandardCharsets.UTF_8));
        X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);
        final List<AVA> avaList = ((X500Name) cert.getSubjectDN()).allAvas();
        return new CertInfo(avaList);
    }

}
