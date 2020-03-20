package io.getlime.security.powerauth.app.webflow.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.push.client.PushServerClient;
import io.getlime.security.powerauth.lib.webflow.authentication.service.SSLConfigurationService;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

import javax.annotation.PostConstruct;

/**
 * Configuration for the PowerAuth 2.0 Server connector.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class PowerAuthWebServiceConfiguration {

    private SSLConfigurationService sslConfigurationService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

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

    @PostConstruct
    public void postConstruct() {
        // Configure Unirest properties
        Unirest.config()
                .concurrency(unirestConcurrencyTotal, unirestConcurrencyPerRoute)
                .setObjectMapper(new ObjectMapper() {

            public String writeValue(Object value) {
                try {
                    return mapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return mapper.readValue(value, valueType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Initialize security interceptor.
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
     * Initialize JAXB marshaller.
     * @return JAXB marshaller.
     */
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths("io.getlime.powerauth.soap.v2", "io.getlime.powerauth.soap.v3");
        return marshaller;
    }

    /**
     * Initialize PowerAuth 2.0 client.
     * @param marshaller JAXB marshaller.
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
        PushServerClient client = new PushServerClient(powerAuthPushServiceUrl);
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            sslConfigurationService.trustAllCertificates();
        }
        return client;
    }

}