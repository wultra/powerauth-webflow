package io.getlime.security.powerauth.lib.webauth.authentication.mtoken.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for push service related values.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class PushServiceConfiguration {

    @Value("${powerauth.push.service.appId}")
    private Long pushServerApplication;

    /**
     * Getter for the push server application ID.
     * @return Application ID.
     */
    public Long getPushServerApplication() {
        return pushServerApplication;
    }
}
