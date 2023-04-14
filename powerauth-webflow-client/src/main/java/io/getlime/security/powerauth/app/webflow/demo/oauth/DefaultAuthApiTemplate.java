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

package io.getlime.security.powerauth.app.webflow.demo.oauth;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthApiTemplate.class);

    public DefaultAuthApiTemplate(String clientId, String clientSecret, String authorizeUrl, String accessTokenUrl) {
        super(clientId, clientSecret, authorizeUrl, accessTokenUrl);
    }

    @Override
    protected RestTemplate createRestTemplate() {
        final RestTemplate restTemplate = super.createRestTemplate();
        try {
            final HttpComponentsClientHttpRequestFactory requestFactory = configureSsl();
            if (requestFactory != null) {
                restTemplate.setRequestFactory(requestFactory);
            }
        } catch (IOException | UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return restTemplate;
    }

    private HttpComponentsClientHttpRequestFactory configureSsl() throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // Prepare keystore and truststore configurations
        final SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

        // Configure keystore
        final File keyStoreResource = ResourceUtils.getFile("keystore.jks");
        if (keyStoreResource.exists()) {
            final String keyAlias = "key";
            final char[] keyStorePassword = "changeme".toCharArray();
            final char[] keyStorePrivateKeyPassword = "aaaa".toCharArray();
            sslContextBuilder.loadKeyMaterial(keyStoreResource, keyStorePassword, keyStorePrivateKeyPassword, (map, socket) -> keyAlias);
        }

        // Prepare request factory
        final SSLContext sslContext = sslContextBuilder.build();
        if (sslContext != null) {
            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            final PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(socketFactory).build();
            final HttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
            return new HttpComponentsClientHttpRequestFactory(httpClient);
        }

        return null;
    }

}
