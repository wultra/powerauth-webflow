/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

package io.getlime.security.powerauth.app.webauth.demo.configuration;

import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Demo application configuration.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
public class WebAuthServiceConfiguration {

    @Value("${powerauth.webauth.service.url}")
    private String webAuthServiceUrl;

    @Value("${powerauth.webauth.service.oauth.authorizeUrl}")
    private String webAuthOAuthAuthorizeUrl;

    @Value("${powerauth.webauth.service.oauth.tokenUrl}")
    private String webAuthOAuthTokenUrl;

    @Value("${powerauth.webauth.service.oauth.clientId}")
    private String clientId;

    @Value("${powerauth.webauth.service.oauth.clientSecret}")
    private String clientSecret;

    @Value("${powerauth.nextstep.service.url}")
    private String nextstepServiceUrl;

    /**
     * Whether invalid SSL certificates should be accepted.
     */
    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    public String getWebAuthServiceUrl() {
        return webAuthServiceUrl;
    }

    public String getWebAuthOAuthAuthorizeUrl() {
        return webAuthOAuthAuthorizeUrl;
    }

    public String getWebAuthOAuthTokenUrl() {
        return webAuthOAuthTokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Default Next Step service client.
     * @return Next Step service client.
     */
    @Bean
    public NextStepClient defaultNextStepClient() {
        NextStepClient client = new NextStepClient(nextstepServiceUrl);
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            trustAllCertificates();
        }
        return client;
    }

    /**
     * Activate trust in all SSL certificates including invalid ones for non-production use.
     */
    private void trustAllCertificates() {
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
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE,
                    "Error occurred while setting SSL socket factory",
                    e
            );
        }
    }

}
