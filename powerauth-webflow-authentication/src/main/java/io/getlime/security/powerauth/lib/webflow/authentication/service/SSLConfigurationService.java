/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Service for configuring SSL parameters for HTTPS connections.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class SSLConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(SSLConfigurationService.class);

    /**
     * Whether trusting invalid SSL certificates is already enabled.
     */
    private boolean trustAllCertificatesEnabled = false;

    /**
     * Activate trust in all SSL certificates including invalid ones for non-production use.
     */
    public void trustAllCertificates() {
        if (trustAllCertificatesEnabled) {
            // trusting invalid SSL certificates is already set up, no need to set it up twice
            return;
        }

        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

        }};

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            trustAllCertificatesEnabled = true;
        } catch (Exception e) {
            logger.error(
                    "Error occurred while setting SSL socket factory",
                    e
            );
        }
    }
}
