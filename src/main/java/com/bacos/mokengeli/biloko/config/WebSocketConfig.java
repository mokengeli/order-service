package com.bacos.mokengeli.biloko.config;

import com.bacos.mokengeli.biloko.config.websocket.WebSocketAuthChannelInterceptor;
import com.bacos.mokengeli.biloko.config.websocket.WebSocketHandshakeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;
    private final WebSocketHandshakeHandler handshakeHandler;

    public WebSocketConfig(WebSocketAuthChannelInterceptor authChannelInterceptor,
                           WebSocketHandshakeHandler handshakeHandler) {
        this.authChannelInterceptor = authChannelInterceptor;
        this.handshakeHandler = handshakeHandler;
    }

    /**
     * Configuration du container WebSocket pour optimiser les performances
     * Particulièrement important pour React Native
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        // Tailles des buffers optimisées pour mobile
        container.setMaxTextMessageBufferSize(64 * 1024);    // 64KB pour les messages texte
        container.setMaxBinaryMessageBufferSize(64 * 1024);   // 64KB pour les messages binaires

        // Timeout de session (30 minutes)
        container.setMaxSessionIdleTimeout(30 * 60 * 1000L);

        // Async send timeout (10 secondes)
        container.setAsyncSendTimeout(10 * 1000L);

        log.info("📦 WebSocket container configured for native WebSocket");
        return container;
    }

    /**
     * TaskScheduler pour les heartbeats STOMP
     */
    @Bean
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.initialize();

        log.info("⏰ WebSocket heartbeat scheduler configured");
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint pour WebSocket NATIF (sans SockJS)
        registry.addEndpoint("/api/order/ws/websocket")
                .setAllowedOriginPatterns("*")  // À restreindre en production
                .setHandshakeHandler(handshakeHandler)  // Gestionnaire custom pour l'authentification
        // PAS de .withSockJS() - WebSocket natif uniquement
        ;

        log.info("🔌 Native WebSocket endpoint registered at: /api/order/ws/websocket");

        // OPTIONNEL : Garder temporairement l'ancien endpoint SockJS pour compatibilité
        // (à supprimer une fois que tous les clients sont migrés)
        registry.addEndpoint("/api/order/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(45000)
                .setDisconnectDelay(5000);

        log.info("🔌 Legacy SockJS endpoint kept at: /api/order/ws (for compatibility)");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Configuration du broker avec heartbeats STOMP
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{
                        30000,  // Server expects client heartbeat every 30s
                        30000   // Server sends heartbeat every 30s
                })
                .setTaskScheduler(webSocketTaskScheduler());

        // Préfixes pour les messages entrants
        registry.setApplicationDestinationPrefixes("/ws", "/app");

        // Configuration pour les destinations utilisateur
        registry.setUserDestinationPrefix("/user");

        // Préserver la compatibilité avec différents clients
        registry.setPreservePublishOrder(true);

        log.info("📨 Message broker configured with STOMP heartbeats");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Intercepteur pour l'authentification des messages STOMP
        registration.interceptors(authChannelInterceptor);

        // Configuration du pool de threads pour les messages entrants
        registration.taskExecutor()
                .corePoolSize(4)      // Threads minimum
                .maxPoolSize(8)       // Threads maximum
                .queueCapacity(100)   // Taille de la queue
                .keepAliveSeconds(60);

        log.info("📥 Inbound channel configured with auth interceptor");
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Configuration du pool de threads pour les messages sortants
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(8)
                .queueCapacity(100);

        log.info("📤 Outbound channel configured");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Limites de transport WebSocket
        registration
                .setMessageSizeLimit(64 * 1024)        // 64KB max par message
                .setSendBufferSizeLimit(512 * 1024)    // 512KB buffer d'envoi
                .setSendTimeLimit(20 * 1000)           // 20s timeout d'envoi
                .setTimeToFirstMessage(30 * 1000);     // 30s pour recevoir le premier message

        log.info("🚀 WebSocket transport configured for optimal mobile performance");
    }
}