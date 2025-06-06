package com.bacos.mokengeli.biloko.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint that clients will connect to.
        registry.addEndpoint("/api/order/ws")
                .setAllowedOrigins("*") // Adjust origins as needed.
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker for destinations starting with /topic.
        registry.enableSimpleBroker("/topic");
        // Prefix for messages that are bound for methods annotated with @MessageMapping.
        registry.setApplicationDestinationPrefixes("/ws");
    }
}
