/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for configuring SSL parameters for HTTPS connections.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class SSLConfigurationService {

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
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE,
                    "Error occurred while setting SSL socket factory",
                    e
            );
        }
    }
}
