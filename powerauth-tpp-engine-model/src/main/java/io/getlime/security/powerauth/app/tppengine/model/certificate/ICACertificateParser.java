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

package io.getlime.security.powerauth.app.tppengine.model.certificate;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
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

        // Check for null certificate value
        if (certificatePem == null) {
            throw new CertificateException("Certificate in PEM format not found.");
        }

        // Handle the URL encoded certificates
        if (certificatePem.startsWith("-----BEGIN%20CERTIFICATE-----")) { // certificate is URL encoded by nginx.
            certificatePem = URLDecoder.decode(certificatePem, StandardCharsets.UTF_8);
        }

        // Replace spaces in Apache forwarded certificate by newlines correctly
        certificatePem = certificatePem
                .replaceAll(" ", "\n")
                .replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----")
                .replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");


        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(certificatePem.getBytes(StandardCharsets.UTF_8));
            PEMParser pemParser = new PEMParser(new InputStreamReader(bais));
            JcaX509CertificateConverter x509Converter = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider());
            final X509CertificateHolder x509CertificateHolder = (X509CertificateHolder) pemParser.readObject();
            if (x509CertificateHolder == null) {
                throw new CertificateException("Unable to parse certificate from pem.");
            }
            final X509Certificate cert = x509Converter.getCertificate(x509CertificateHolder);
            final byte[] qcStatement = cert.getExtensionValue("1.3.6.1.5.5.7.1.3");
            if (qcStatement == null) {
                throw new CertificateException("Unable to extract PSD2 mandates.");
            }
            final ASN1Primitive qcStatementAsn1Primitive = JcaX509ExtensionUtils.parseExtensionValue(qcStatement);

            if (qcStatementAsn1Primitive == null) {
                throw new CertificateException("Unable to extract PSD2 mandates from extension value.");
            }

            final DLSequence it = ((DLSequence) qcStatementAsn1Primitive);

            Set<CertInfo.PSD2> psd2Mandates = new HashSet<>();

            for (ASN1Encodable asn1Primitive : it) {
                if (asn1Primitive instanceof final DLSequence sequence) {
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
            final X500Name x500Name = new X500Name(cert.getSubjectX500Principal().toString());
            String country = null;
            String serialNumber = null;
            String commonName = null;
            String psd2License = null;
            String organization = null;
            String street = null;
            String city = null;
            String zipCode = null;
            String region = null;
            String website = null;
            for (RDN rdn: x500Name.getRDNs()) {
                final AttributeTypeAndValue attr = rdn.getFirst();
                final String oid = attr.getType().getId();
                final String val = attr.getValue().toString();

                switch (oid) {
                    case "2.5.4.6" -> {   //    C=CZ => 2.5.4.6
                        country = val;
                    }
                    case "2.5.4.3" -> {   //    CN=cnb.cz => 2.5.4.3
                        commonName = val;
                        website = "https://" + val;
                    }
                    case "2.5.4.10" -> {  //    O=ČESKÁ NÁRODNÍ BANKA => 2.5.4.10
                        organization = val;
                    }
                    case "2.5.4.9" -> {   //    STREET=Na příkopě 864/28 => 2.5.4.9
                        street = val;
                    }
                    case "2.5.4.7" -> {   //    L=Praha 1 => 2.5.4.7
                        city = val;
                    }
                    case "2.5.4.17" -> {  //    OID.2.5.4.17=11000 => 2.5.4.17
                        zipCode = val;
                    }
                    case "2.5.4.5" -> {   //    SERIALNUMBER=48136450 => 2.5.4.5
                        serialNumber = val;
                    }
                    case "2.5.4.8" -> {   //    ST=Hlavní město Praha => 2.5.4.8
                        region = val;
                    }
                    case "2.5.4.97" -> {  //   OID.2.5.4.97=PSDCZ-CNB-48136450 => 2.5.4.97
                        psd2License = val;
                    }
                }
            }

            return new CertInfo(serialNumber, commonName, psd2License, organization, street, city, zipCode, region, country, website, psd2Mandates);
        } catch (Throwable e) { // catch all errors that can occur
            throw new CertificateException("Unable to extract PSD2 mandates.");
        }

    }

}
