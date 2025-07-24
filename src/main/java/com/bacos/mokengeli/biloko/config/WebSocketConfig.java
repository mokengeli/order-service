package com.bacos.mokengeli.biloko.config;

import com.bacos.mokengeli.biloko.config.websocket.WebSocketAuthenticationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthenticationInterceptor authInterceptor;

    public WebSocketConfig(WebSocketAuthenticationInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * TaskScheduler personnalisé pour éviter les conflits avec le bean par défaut
     * Configuration optimisée pour applications POS/Restaurant
     */
    @Bean
    public TaskScheduler customWebSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Configuration du pool de threads
        scheduler.setPoolSize(4);                                    // 4 threads pour les heartbeats
        scheduler.setThreadNamePrefix("pos-websocket-heartbeat-");   // Nom explicite pour debug

        // Configuration de l'arrêt propre
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);                    // Attendre 10s pour finir les tâches

        // Initialisation du scheduler
        scheduler.initialize();

        return scheduler;
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/order/ws")
                .setAllowedOriginPatterns("*") // Configuration temporaire pour tests
                .addInterceptors(authInterceptor) // AJOUTER L'INTERCEPTEUR
                // Configuration SockJS
                .withSockJS()
                // =================================================================
                // SockJS Heartbeat : Transport Level (TCP/HTTP keep-alive)
                // =================================================================
                .setHeartbeatTime(45000)        // 45 secondes - SockJS ping/pong
                .setDisconnectDelay(5000);      // 5 secondes avant fermeture
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
                // =================================================================
                // STOMP Heartbeat : Application Level (STOMP protocol)
                // [incoming, outgoing] = [ce que le serveur attend du client, ce que le serveur envoie au client]
                // =================================================================
                .setHeartbeatValue(new long[]{
                        60000,  // incoming: client doit envoyer heartbeat toutes les 60s
                        30000   // outgoing: serveur envoie heartbeat toutes les 30s
                })
                .setTaskScheduler(customWebSocketTaskScheduler()); // ✅ Lier le TaskScheduler


        // Préfixe pour les messages entrants
        registry.setApplicationDestinationPrefixes("/ws");

        // Configuration utilisateur pour les sessions personnalisées
        registry.setUserDestinationPrefix("/user");
    }


}
