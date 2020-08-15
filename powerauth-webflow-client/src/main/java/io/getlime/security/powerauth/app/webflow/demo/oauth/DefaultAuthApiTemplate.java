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

package io.getlime.security.powerauth.app.webflow.demo.oauth;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Default template used to access resources needed for OAuth 2.0 dance.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class DefaultAuthApiTemplate extends OAuth2Template {

    public DefaultAuthApiTemplate(String clientId, String clientSecret, String authorizeUrl, String accessTokenUrl) {
        super(clientId, clientSecret, authorizeUrl, accessTokenUrl);
    }

    @Override
    protected RestTemplate createRestTemplate() {
        RestTemplate restTemplate = super.createRestTemplate();
        try {
            final HttpComponentsClientHttpRequestFactory requestFactory = configureSsl();
            if (requestFactory != null) {
                restTemplate.setRequestFactory(requestFactory);
            }
        } catch (IOException | UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            //
        }
        return restTemplate;
    }

    private HttpComponentsClientHttpRequestFactory configureSsl() throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // Prepare keystore and truststore configurations
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

        // Configure keystore
        File keyStoreResource = ResourceUtils.getFile("keystore.jks");
        if (keyStoreResource.exists()) {
            final String keyAlias = "key";
            final char[] keyStorePassword = "changeme".toCharArray();
            final char[] keyStorePrivateKeyPassword = "aaaa".toCharArray();
            sslContextBuilder.loadKeyMaterial(keyStoreResource, keyStorePassword, keyStorePrivateKeyPassword, (map, socket) -> keyAlias);
        }

        // Prepare request factory
        SSLContext sslContext = sslContextBuilder.build();
        if (sslContext != null) {
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            return new HttpComponentsClientHttpRequestFactory(httpClient);
        }

        return null;
    }

}
