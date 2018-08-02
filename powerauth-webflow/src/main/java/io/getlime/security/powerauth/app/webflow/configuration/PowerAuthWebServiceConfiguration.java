package io.getlime.security.powerauth.app.webflow.configuration;

import io.getlime.push.client.PushServerClient;
import io.getlime.security.powerauth.lib.webflow.authentication.service.SSLConfigurationService;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.apache.ws.security.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

/**
 * Configuration for the PowerAuth 2.0 Server connector.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
@SuppressWarnings("deprecation")
public class PowerAuthWebServiceConfiguration {

    private SSLConfigurationService sslConfigurationService;

    @Value("${powerauth.service.url}")
    private String powerAuthServiceUrl;

    @Value("${powerauth.push.service.url}")
    private String powerAuthPushServiceUrl;

    @Value("${powerauth.service.security.clientToken}")
    private String clientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String clientSecret;

    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    /**
     * Configuration constructor.
     * @param sslConfigurationService SSL configuration service.
     */
    @Autowired
    public PowerAuthWebServiceConfiguration(SSLConfigurationService sslConfigurationService) {
        this.sslConfigurationService = sslConfigurationService;
    }

    /**
     * Initialize security interceptor.
     * Must use DEPRECATED class here, wss4j2 is not yet production ready
     * @return Security interceptor.
     */
    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
        wss4jSecurityInterceptor.setSecurementActions("UsernameToken");
        wss4jSecurityInterceptor.setSecurementUsername(clientToken);
        wss4jSecurityInterceptor.setSecurementPassword(clientSecret);
        wss4jSecurityInterceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
        return wss4jSecurityInterceptor;
    }

    /**
     * Initialize JAXB marschaller.
     * @return JAXB marschaller.
     */
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("io.getlime.powerauth.soap");
        return marshaller;
    }

    /**
     * Initialize PowerAuth 2.0 client.
     * @param marshaller JAXB marschaller.
     * @return PowerAuth 2.0 client.
     */
    @Bean
    public PowerAuthServiceClient powerAuthClient(Jaxb2Marshaller marshaller) {
        PowerAuthServiceClient client = new PowerAuthServiceClient();
        client.setDefaultUri(powerAuthServiceUrl);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        if (!clientToken.isEmpty()) {
            ClientInterceptor interceptor = securityInterceptor();
            client.setInterceptors(new ClientInterceptor[]{interceptor});
        }
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            sslConfigurationService.trustAllCertificates();
        }
        return client;
    }

    /**
     * Initialize PowerAuth 2.0 Push server client.
     * @return Push server client.
     */
    @Bean
    public PushServerClient pushServerClient() {
        PushServerClient client = new PushServerClient();
        client.setServiceBaseUrl(powerAuthPushServiceUrl);
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            sslConfigurationService.trustAllCertificates();
        }
        return client;
    }

}