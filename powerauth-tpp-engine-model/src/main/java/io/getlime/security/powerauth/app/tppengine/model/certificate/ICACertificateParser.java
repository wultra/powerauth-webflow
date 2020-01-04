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

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import sun.security.x509.AVA;
import sun.security.x509.X500Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to parse PSD2 certificates issued by I.CA certificate authority (Czech Republic).
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ICACertificateParser implements ICertificateParser {

    public static final String psd2   = "0.4.0.19495.2";
    public static final String psp_as = "0.4.0.19495.1.1";
    public static final String psp_pi = "0.4.0.19495.1.2";
    public static final String psp_ai = "0.4.0.19495.1.3";
    public static final String psp_ic = "0.4.0.19495.1.4";

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

        try {
            final byte[] qcStatement = cert.getExtensionValue("1.3.6.1.5.5.7.1.3");
            final ASN1Primitive qcStatementAsn1Primitive = JcaX509ExtensionUtils.parseExtensionValue(qcStatement);

            final DLSequence it = ((DLSequence) qcStatementAsn1Primitive);

            Set<CertInfo.PSD2> psd2Mandates = new HashSet<>();

            for (ASN1Encodable asn1Primitive : it) {
                if (asn1Primitive instanceof DLSequence) {
                    DLSequence sequence = (DLSequence) asn1Primitive;
                    if (sequence.size() == 2) {
                        ASN1ObjectIdentifier id = (ASN1ObjectIdentifier) sequence.getObjectAt(0);
                        DLSequence mandates = (DLSequence) sequence.getObjectAt(1);
                        if (psd2.equals(id.getId())) {
                            for (ASN1Encodable mandate : mandates) {
                                if (mandate instanceof DLSequence) {
                                    for (ASN1Encodable seq: (DLSequence) mandate) {
                                        DLSequence a = (DLSequence)seq;
                                        final ASN1ObjectIdentifier identifier = (ASN1ObjectIdentifier) ((DLSequence) seq).getObjectAt(0);
                                        if (psp_as.equals(identifier.getId())) {
                                            psd2Mandates.add(CertInfo.PSD2.PSP_AS);
                                        }
                                        if (psp_ai.equals(identifier.getId())) {
                                            psd2Mandates.add(CertInfo.PSD2.PSP_AI);
                                        }
                                        if (psp_pi.equals(identifier.getId())) {
                                            psd2Mandates.add(CertInfo.PSD2.PSP_PI);
                                        }
                                        if (psp_ic.equals(identifier.getId())) {
                                            psd2Mandates.add(CertInfo.PSD2.PSP_IC);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            final List<AVA> avaList = ((X500Name) cert.getSubjectDN()).allAvas();
            String country = null;
            String serialNumber = null;
            String commonName = null;
            String psd2License = null;
            String organization = null;
            String street = null;
            String city = null;
            String zipCode = null;
            String region = null;
            for (AVA ava: avaList) {
                final String oid = ava.getObjectIdentifier().toString();
                final String val = ava.getValueString();

                switch (oid) {
                    case "2.5.4.6": {   //    C=CZ => 2.5.4.6
                        country = val;
                        break;
                    }
                    case "2.5.4.3": {   //    CN=cnb.cz => 2.5.4.3
                        commonName = val;
                        break;
                    }
                    case "2.5.4.10": {  //    O=ČESKÁ NÁRODNÍ BANKA => 2.5.4.10
                        organization = val;
                        break;
                    }
                    case "2.5.4.9": {   //    STREET=Na příkopě 864/28 => 2.5.4.9
                        street = val;
                        break;
                    }
                    case "2.5.4.7": {   //    L=Praha 1 => 2.5.4.7
                        city = val;
                        break;
                    }
                    case "2.5.4.17": {  //    OID.2.5.4.17=11000 => 2.5.4.17
                        zipCode = val;
                        break;
                    }
                    case "2.5.4.5": {   //    SERIALNUMBER=48136450 => 2.5.4.5
                        serialNumber = val;
                        break;
                    }
                    case "2.5.4.8": {   //    ST=Hlavní město Praha => 2.5.4.8
                        region = val;
                        break;
                    }
                    case "2.5.4.97": {  //   OID.2.5.4.97=PSDCZ-CNB-48136450 => 2.5.4.97
                        psd2License = val;
                        break;
                    }
                }
            }

            return new CertInfo(serialNumber, commonName, psd2License, organization, street, city, zipCode, region, country, psd2Mandates);
        } catch (IOException e) {
            throw new CertificateException("Unable to extract PSD2 mandates.");
        }

    }

}
