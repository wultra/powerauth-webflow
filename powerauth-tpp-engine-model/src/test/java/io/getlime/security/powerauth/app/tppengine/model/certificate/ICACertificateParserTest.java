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
    void testCertificateParser() throws CertificateException {

        final String certificate = """
                -----BEGIN CERTIFICATE-----
                MIIIJjCCBg6gAwIBAgIJA7fTH4NdPar6MA0GCSqGSIb3DQEBCwUAMH8xCzAJBgNV
                BAYTAkNaMSgwJgYDVQQDDB9JLkNBIFRFU1QgU1NMIEVWIENBL1JTQSAxMC8yMDE3
                MS0wKwYDVQQKDCRQcnZuw60gY2VydGlmaWthxI1uw60gYXV0b3JpdGEsIGEucy4x
                FzAVBgNVBGEMDk5UUkNaLTI2NDM5Mzk1MB4XDTE5MTIwMjEwNDgwMVoXDTIwMTIw
                MTEwNDgwMVowggEEMQswCQYDVQQGEwJDWjEPMA0GA1UEAwwGY25iLmN6MSAwHgYD
                VQQKDBfEjEVTS8OBIE7DgVJPRE7DjSBCQU5LQTEdMBsGA1UECQwUTmEgcMWZw61r
                b3DEmyA4NjQvMjgxEDAOBgNVBAcMB1ByYWhhIDExDjAMBgNVBBEMBTExMDAwMREw
                DwYDVQQFEwg0ODEzNjQ1MDEdMBsGA1UEDwwUUHJpdmF0ZSBPcmdhbml6YXRpb24x
                EzARBgsrBgEEAYI3PAIBAxMCQ1oxHTAbBgNVBAgMFEhsYXZuw60gbcSbc3RvIFBy
                YWhhMRswGQYDVQRhDBJQU0RDWi1DTkItNDgxMzY0NTAwggEiMA0GCSqGSIb3DQEB
                AQUAA4IBDwAwggEKAoIBAQC5A6VAB2sORt9dLxc2w86vINN+3N/vq9F9LGHn7xC4
                4apnCRdGqeRFvdDZBZPKfYOpw1cvfk3YTAtEeh2MbGQCgdTqrl0LKBILEPKi60lT
                rcEFtIBFxC34NhuHeUDifU9pul3y1SIGq1kYgU3zeF0IJBOEfJ5Ez9kIQ/pbjx+h
                41VMQh0esqKu9hEMQr5QOJlUP1uILX76pMfyKgyGHlP4Dy587yMI/dSp7E2S97+n
                1/D/zW/3fB3fC2x4NYJx8ufrwhCG/etvWk917iclR39f5GU9mu8a5pBDgGwxuNCW
                QLnB9aDIuqOK7miQtzeXlIKR4VcwWLCrkHyrjy2KtzPhAgMBAAGjggMcMIIDGDAR
                BgNVHREECjAIggZjbmIuY3owCQYDVR0TBAIwADCB5gYDVR0gBIHeMIHbMIHNBg0r
                BgEEAYG4SAoDKAEBMIG7MB0GCCsGAQUFBwIBFhFodHRwOi8vd3d3LmljYS5jejCB
                mQYIKwYBBQUHAgIwgYwagYlUZW50byBURVNUIGNlcnRpZmlrYXQgYnlsIHZ5ZGFu
                IHYgc291bGFkdSBzIG5hcml6ZW5pbSBFVSBjLiBubm4vUlJSVC5UaGlzIGlzIGEg
                VEVTVCBjZXJ0aWZpY2F0ZSBhY2NvcmRpbmcgdG8gUmVndWxhdGlvbiAoRVUpIE5v
                IG5ubi9SUlJSLjAJBgcEAIvsQAEEMDMGA1UdHwQsMCowKKAmoCSGImh0dHA6Ly90
                ZXN0cS5pY2EuY3ovdHFjdzE3X3JzYS5jcmwwagYIKwYBBQUHAQEEXjBcMC4GCCsG
                AQUFBzAChiJodHRwOi8vdGVzdHEuaWNhLmN6L3RxY3cxN19yc2EuY2VyMCoGCCsG
                AQUFBzABhh5odHRwOi8vdG9jc3AuaWNhLmN6L3RxY3cxN19yc2EwDgYDVR0PAQH/
                BAQDAgWgMIH+BggrBgEFBQcBAwSB8TCB7jAIBgYEAI5GAQEwEwYGBACORgEGMAkG
                BwQAjkYBBgMwVgYGBACORgEFMEwwJBYeaHR0cDovL3Rlc3RxLmljYS5jei9wZHNf
                Y3MucGRmEwJjczAkFh5odHRwOi8vdGVzdHEuaWNhLmN6L3Bkc19lbi5wZGYTAmVu
                MHUGBgQAgZgnAjBrMEwwEQYHBACBmCcBAQwGUFNQX0FTMBEGBwQAgZgnAQIMBlBT
                UF9QSTARBgcEAIGYJwEDDAZQU1BfQUkwEQYHBACBmCcBBAwGUFNQX0lDDBNDemVj
                aCBOYXRpb25hbCBCYW5rDAZDWi1DTkIwHwYDVR0jBBgwFoAUOv/ngSfM0sonGeca
                odAaO8awn6owHQYDVR0OBBYEFIe6wOqAu0xteqo19vBNC1OjVvHnMB0GA1UdJQQW
                MBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAgEAfA3xRbrm
                61of+TQUMxpkYSLgfTUKzZ6bJqc6ir1r4NPb4WNrAZkJSaJnIFSvej4m6z0Nit0o
                eHeGxJDQEwWCaQFa3E9lJS/33oZQQGn0iMsgN8rj70FXbGGRE1ZcvyhhioKEmA7f
                AbbkRlgxigrRp3cY12M7m3SfuD0Rr9fAJ30vvi1UuUBiJCUIznjbWezF2gNyd1KX
                hroKcoqMxl5260m5DSAWwoUvwc7MxjlHyCEx28RXv2/lWij2P9hnyN8WdjnO1Py9
                1RrJEJg9BJmfEdOfzVvtCjqAME77EqLB8wysktDe0T6BE7Ef96j/QKEFLId2kVtv
                U9iJ6xaZwyo5Jh68cC0/tZGMJ4cTx3OES4VttRNzIcneZ8y+gtoPs4X5Ob/uqc5s
                QrFMf+AclRFimNdAz0DN6Kv3kUS8kZtKn+XN7+Y1gkMHmbT6WSgfWB6BQUbbxG+a
                Wj3TY+MPQ/SuAJ42hv7iiWUwapcXTyI560n5KFKKiyXHtgu+jipCAR74VBIf4or9
                fO3E0tLGMlFvwLe2vfiAnBuiAZ1baM9a2vQWBcB/7SahqrBtKGpwGkJg6TAkYVIN
                EruSUWJnKZlRB/wtGJ6Z/b8DI+18RGmpy4YlF9ujYTiice2GyVXD2HndNBVhqq2o
                QYANhYtS0EAXe5o3NF2ZxkQ2fiABEPO7/RU=
                -----END CERTIFICATE-----""";

        ICACertificateParser parser = new ICACertificateParser();
        final CertInfo parse = parser.parse(certificate);

        // Check basic certificate info
        Assertions.assertEquals("48136450", parse.serialNumber());
        Assertions.assertEquals("cnb.cz", parse.commonName());
        Assertions.assertEquals("PSDCZ-CNB-48136450", parse.psd2License());
        Assertions.assertEquals("ČESKÁ NÁRODNÍ BANKA", parse.organization());
        Assertions.assertEquals("Na příkopě 864/28", parse.street());
        Assertions.assertEquals("Praha 1", parse.city());
        Assertions.assertEquals("Hlavní město Praha", parse.region());
        Assertions.assertEquals("11000", parse.zipCode());
        Assertions.assertEquals("CZ", parse.country());
        Assertions.assertEquals("https://cnb.cz", parse.website());
        Assertions.assertEquals("Na příkopě 864/28\nPraha 1\n11000\nHlavní město Praha\nCZ", parse.getAddressUnstructured());

        Set<CertInfo.PSD2> expected = new HashSet<>();
        expected.add(CertInfo.PSD2.PSP_AS);
        expected.add(CertInfo.PSD2.PSP_AI);
        expected.add(CertInfo.PSD2.PSP_IC);
        expected.add(CertInfo.PSD2.PSP_PI);
        Assertions.assertEquals(expected, parse.psd2Mandates());

    }

}