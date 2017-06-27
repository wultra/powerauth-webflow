package io.getlime.security.powerauth.app.webauth.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Security configuration for WebSockets.
 *
 * @author Roman Strobl
 */
@Configuration
public class WebSocketSecurityConfiguration extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    /**
     * Same origin check is disabled, otherwise the WebServices do not work in the mobile token UI.
     * TODO - investigate how to overcome this problem
     *
     * @return same origin check disabled
     */
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
