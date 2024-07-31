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
package io.getlime.security.powerauth.app.webflow.configuration;

import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.interceptor.WebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration of WebSockets with a simple message broker.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnProperty(name = "powerauth.webflow.websockets.enabled", havingValue = "true")
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    public static final String MESSAGE_PREFIX = "/topic";

    private final WebFlowServicesConfiguration configuration;

    @Autowired
    public WebSocketConfiguration(WebFlowServicesConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Stomp endpoint registration for Web Sockets.
     * @param registry Stomp endpoint registry.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        WebSocketHandshakeInterceptor interceptor = new WebSocketHandshakeInterceptor(configuration.isAfsIpAddressDetectionEnabled(), configuration.isAfsIpv4Forced());
        registry.addEndpoint("/websocket").addInterceptors(interceptor).setAllowedOriginPatterns("*").withSockJS();
    }

    /**
     * Message broker configuration.
     * @param registry Message broker registry.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(MESSAGE_PREFIX);
        registry.setApplicationDestinationPrefixes("/app");
    }
}