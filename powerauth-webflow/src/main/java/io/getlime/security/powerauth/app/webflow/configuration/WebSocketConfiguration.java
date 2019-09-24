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
package io.getlime.security.powerauth.app.webflow.configuration;

import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import io.getlime.security.powerauth.lib.webflow.authentication.interceptor.WebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration of WebSockets with a simple message broker.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
@EnableWebSocketMessageBroker
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
        WebSocketHandshakeInterceptor interceptor = new WebSocketHandshakeInterceptor(configuration.getAfsDetectIpAddress(), configuration.getAfsForceIpv4());
        registry.addEndpoint("/websocket").addInterceptors(interceptor).setAllowedOrigins("*").withSockJS();
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