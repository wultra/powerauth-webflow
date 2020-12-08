package io.getlime.security.powerauth.app.webflow.configuration;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClient;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClientConfiguration;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.security.powerauth.lib.webflow.authentication.service.SSLConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the PowerAuth 2.0 Server connector.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class PowerAuthWebServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PowerAuthWebServiceConfiguration.class);

    private SSLConfigurationService sslConfigurationService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.push.service.url}")
    private String powerAuthPushServiceUrl;

    @Value("${powerauth.service.security.clientToken}")
    private String clientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String clientSecret;

    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    /**
     * Configuration of the total Unirest parallel connections.
     */
    @Value("${powerauth.unirest.concurrency.total:500}")
    private int unirestConcurrencyTotal;

    /**
     * Configuration of the per-route Unirest parallel connections.
     */
    @Value("${powerauth.unirest.concurrency.perRoute:50}")
    private int unirestConcurrencyPerRoute;

    /**
     * Configuration constructor.
     * @param sslConfigurationService SSL configuration service.
     */
    @Autowired
    public PowerAuthWebServiceConfiguration(SSLConfigurationService sslConfigurationService) {
        this.sslConfigurationService = sslConfigurationService;
    }

    /**
     * Initialize PowerAuth REST client.
     * @return PowerAuth REST client.
     */
    @Bean
    public PowerAuthClient powerAuthClient() {
        PowerAuthRestClientConfiguration config = new PowerAuthRestClientConfiguration();
        config.setPowerAuthClientToken(clientToken);
        config.setPowerAuthClientSecret(clientSecret);
        config.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        try {
            return new PowerAuthRestClient(powerAuthRestUrl, config);
        } catch (PowerAuthClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Initialize PowerAuth 2.0 Push server client.
     * @return Push server client.
     */
    @Bean
    public PushServerClient pushServerClient() {
        try {
            PushServerClient client = new PushServerClient(powerAuthPushServiceUrl);
            // whether invalid SSL certificates should be accepted
            if (acceptInvalidSslCertificate) {
                sslConfigurationService.trustAllCertificates();
            }
            return client;
        } catch (PushServerClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

}