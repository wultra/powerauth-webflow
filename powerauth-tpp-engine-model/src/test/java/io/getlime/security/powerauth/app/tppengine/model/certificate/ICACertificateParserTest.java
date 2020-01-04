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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Petr Dvorak, petr@wultra.com
 */
class ICACertificateParserTest {

    @Test
    public void testCertificateParser() throws CertificateException {

        final String certificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIIJjCCBg6gAwIBAgIJA7fTH4NdPar6MA0GCSqGSIb3DQEBCwUAMH8xCzAJBgNV\n" +
                "BAYTAkNaMSgwJgYDVQQDDB9JLkNBIFRFU1QgU1NMIEVWIENBL1JTQSAxMC8yMDE3\n" +
                "MS0wKwYDVQQKDCRQcnZuw60gY2VydGlmaWthxI1uw60gYXV0b3JpdGEsIGEucy4x\n" +
                "FzAVBgNVBGEMDk5UUkNaLTI2NDM5Mzk1MB4XDTE5MTIwMjEwNDgwMVoXDTIwMTIw\n" +
                "MTEwNDgwMVowggEEMQswCQYDVQQGEwJDWjEPMA0GA1UEAwwGY25iLmN6MSAwHgYD\n" +
                "VQQKDBfEjEVTS8OBIE7DgVJPRE7DjSBCQU5LQTEdMBsGA1UECQwUTmEgcMWZw61r\n" +
                "b3DEmyA4NjQvMjgxEDAOBgNVBAcMB1ByYWhhIDExDjAMBgNVBBEMBTExMDAwMREw\n" +
                "DwYDVQQFEwg0ODEzNjQ1MDEdMBsGA1UEDwwUUHJpdmF0ZSBPcmdhbml6YXRpb24x\n" +
                "EzARBgsrBgEEAYI3PAIBAxMCQ1oxHTAbBgNVBAgMFEhsYXZuw60gbcSbc3RvIFBy\n" +
                "YWhhMRswGQYDVQRhDBJQU0RDWi1DTkItNDgxMzY0NTAwggEiMA0GCSqGSIb3DQEB\n" +
                "AQUAA4IBDwAwggEKAoIBAQC5A6VAB2sORt9dLxc2w86vINN+3N/vq9F9LGHn7xC4\n" +
                "4apnCRdGqeRFvdDZBZPKfYOpw1cvfk3YTAtEeh2MbGQCgdTqrl0LKBILEPKi60lT\n" +
                "rcEFtIBFxC34NhuHeUDifU9pul3y1SIGq1kYgU3zeF0IJBOEfJ5Ez9kIQ/pbjx+h\n" +
                "41VMQh0esqKu9hEMQr5QOJlUP1uILX76pMfyKgyGHlP4Dy587yMI/dSp7E2S97+n\n" +
                "1/D/zW/3fB3fC2x4NYJx8ufrwhCG/etvWk917iclR39f5GU9mu8a5pBDgGwxuNCW\n" +
                "QLnB9aDIuqOK7miQtzeXlIKR4VcwWLCrkHyrjy2KtzPhAgMBAAGjggMcMIIDGDAR\n" +
                "BgNVHREECjAIggZjbmIuY3owCQYDVR0TBAIwADCB5gYDVR0gBIHeMIHbMIHNBg0r\n" +
                "BgEEAYG4SAoDKAEBMIG7MB0GCCsGAQUFBwIBFhFodHRwOi8vd3d3LmljYS5jejCB\n" +
                "mQYIKwYBBQUHAgIwgYwagYlUZW50byBURVNUIGNlcnRpZmlrYXQgYnlsIHZ5ZGFu\n" +
                "IHYgc291bGFkdSBzIG5hcml6ZW5pbSBFVSBjLiBubm4vUlJSVC5UaGlzIGlzIGEg\n" +
                "VEVTVCBjZXJ0aWZpY2F0ZSBhY2NvcmRpbmcgdG8gUmVndWxhdGlvbiAoRVUpIE5v\n" +
                "IG5ubi9SUlJSLjAJBgcEAIvsQAEEMDMGA1UdHwQsMCowKKAmoCSGImh0dHA6Ly90\n" +
                "ZXN0cS5pY2EuY3ovdHFjdzE3X3JzYS5jcmwwagYIKwYBBQUHAQEEXjBcMC4GCCsG\n" +
                "AQUFBzAChiJodHRwOi8vdGVzdHEuaWNhLmN6L3RxY3cxN19yc2EuY2VyMCoGCCsG\n" +
                "AQUFBzABhh5odHRwOi8vdG9jc3AuaWNhLmN6L3RxY3cxN19yc2EwDgYDVR0PAQH/\n" +
                "BAQDAgWgMIH+BggrBgEFBQcBAwSB8TCB7jAIBgYEAI5GAQEwEwYGBACORgEGMAkG\n" +
                "BwQAjkYBBgMwVgYGBACORgEFMEwwJBYeaHR0cDovL3Rlc3RxLmljYS5jei9wZHNf\n" +
                "Y3MucGRmEwJjczAkFh5odHRwOi8vdGVzdHEuaWNhLmN6L3Bkc19lbi5wZGYTAmVu\n" +
                "MHUGBgQAgZgnAjBrMEwwEQYHBACBmCcBAQwGUFNQX0FTMBEGBwQAgZgnAQIMBlBT\n" +
                "UF9QSTARBgcEAIGYJwEDDAZQU1BfQUkwEQYHBACBmCcBBAwGUFNQX0lDDBNDemVj\n" +
                "aCBOYXRpb25hbCBCYW5rDAZDWi1DTkIwHwYDVR0jBBgwFoAUOv/ngSfM0sonGeca\n" +
                "odAaO8awn6owHQYDVR0OBBYEFIe6wOqAu0xteqo19vBNC1OjVvHnMB0GA1UdJQQW\n" +
                "MBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAgEAfA3xRbrm\n" +
                "61of+TQUMxpkYSLgfTUKzZ6bJqc6ir1r4NPb4WNrAZkJSaJnIFSvej4m6z0Nit0o\n" +
                "eHeGxJDQEwWCaQFa3E9lJS/33oZQQGn0iMsgN8rj70FXbGGRE1ZcvyhhioKEmA7f\n" +
                "AbbkRlgxigrRp3cY12M7m3SfuD0Rr9fAJ30vvi1UuUBiJCUIznjbWezF2gNyd1KX\n" +
                "hroKcoqMxl5260m5DSAWwoUvwc7MxjlHyCEx28RXv2/lWij2P9hnyN8WdjnO1Py9\n" +
                "1RrJEJg9BJmfEdOfzVvtCjqAME77EqLB8wysktDe0T6BE7Ef96j/QKEFLId2kVtv\n" +
                "U9iJ6xaZwyo5Jh68cC0/tZGMJ4cTx3OES4VttRNzIcneZ8y+gtoPs4X5Ob/uqc5s\n" +
                "QrFMf+AclRFimNdAz0DN6Kv3kUS8kZtKn+XN7+Y1gkMHmbT6WSgfWB6BQUbbxG+a\n" +
                "Wj3TY+MPQ/SuAJ42hv7iiWUwapcXTyI560n5KFKKiyXHtgu+jipCAR74VBIf4or9\n" +
                "fO3E0tLGMlFvwLe2vfiAnBuiAZ1baM9a2vQWBcB/7SahqrBtKGpwGkJg6TAkYVIN\n" +
                "EruSUWJnKZlRB/wtGJ6Z/b8DI+18RGmpy4YlF9ujYTiice2GyVXD2HndNBVhqq2o\n" +
                "QYANhYtS0EAXe5o3NF2ZxkQ2fiABEPO7/RU=\n" +
                "-----END CERTIFICATE-----";

        ICACertificateParser parser = new ICACertificateParser();
        final CertInfo parse = parser.parse(certificate);

        // Check basic certificate info
        Assertions.assertEquals("48136450", parse.getSerialNumber());
        Assertions.assertEquals("cnb.cz", parse.getCommonName());
        Assertions.assertEquals("PSDCZ-CNB-48136450", parse.getPsd2License());
        Assertions.assertEquals("ČESKÁ NÁRODNÍ BANKA", parse.getOrganization());
        Assertions.assertEquals("Na příkopě 864/28", parse.getStreet());
        Assertions.assertEquals("Praha 1", parse.getCity());
        Assertions.assertEquals("Hlavní město Praha", parse.getRegion());
        Assertions.assertEquals("11000", parse.getZipCode());
        Assertions.assertEquals("CZ", parse.getCountry());
        Assertions.assertEquals("Na příkopě 864/28\nPraha 1\n11000\nHlavní město Praha\nCZ", parse.getAddressUnstructured());

        Set<CertInfo.PSD2> expected = new HashSet<>();
        expected.add(CertInfo.PSD2.PSP_AS);
        expected.add(CertInfo.PSD2.PSP_AI);
        expected.add(CertInfo.PSD2.PSP_IC);
        expected.add(CertInfo.PSD2.PSP_PI);
        Assertions.assertEquals(expected, parse.getPsd2Mandates());

    }

}