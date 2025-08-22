package com.bacos.mokengeli.biloko.config.socketio;


import com.bacos.mokengeli.biloko.config.service.JwtService;
import com.corundumstudio.socketio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;
import java.util.Arrays;

/**
 * Configuration du serveur Socket.io avec Netty
 * Coexiste avec WebSocket/STOMP sur un port différent
 */
@Slf4j
@Configuration
public class SocketIOServerConfig {

    @Value("${socketio.port:9092}")
    private int port;

    @Value("${socketio.hostname:0.0.0.0}")
    private String hostname;

    @Value("${socketio.ping-interval:30000}")
    private int pingInterval;

    @Value("${socketio.ping-timeout:60000}")
    private int pingTimeout;

    @Value("${socketio.max-frame-payload-length:65536}")
    private int maxFramePayloadLength;

    @Value("${socketio.max-http-content-length:65536}")
    private int maxHttpContentLength;

    @Value("${socketio.worker-threads:4}")
    private int workerThreads;

    @Value("${socketio.cors.origins:*}")
    private String corsOrigins;

    private SocketIOServer server;

    /**
     * Création et configuration du serveur Socket.io
     * IMPORTANT: Ne PAS démarrer le serveur ici, laisser le SocketIOEventHandler le faire
     */
    @Bean
    public SocketIOServer socketIOServer(JwtService jwtService) {
        log.info("🚀 Configuring Socket.io server on {}:{}", hostname, port);

        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();

        // Configuration réseau
        config.setHostname(hostname);
        config.setPort(port);

        // CORS - Important pour React Native
        if ("*".equals(corsOrigins)) {
            config.setOrigin("*");
        } else {
            config.setOrigin(corsOrigins);
        }

        // Transports supportés (WebSocket prioritaire, polling en fallback)
        config.setTransports(Transport.WEBSOCKET, Transport.POLLING);

        // Configuration des timeouts et intervals
        config.setPingInterval(pingInterval);
        config.setPingTimeout(pingTimeout);

        // Limites de payload
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        config.setMaxHttpContentLength(maxHttpContentLength);

        // Threads workers
        config.setWorkerThreads(workerThreads);

        // Options supplémentaires
        config.setAllowCustomRequests(true);
        config.setUpgradeTimeout(10000);
        config.setFirstDataTimeout(5000);

        // Random session ID generator
        config.setRandomSession(true);

        // Authentification via AuthorizationListener
        config.setAuthorizationListener(new CustomAuthorizationListener(jwtService));

        // Permettre les headers custom
        config.setHttpCompression(true);
        config.setWebsocketCompression(true);

        // Configuration pour Android/React Native
        config.setAckMode(AckMode.MANUAL);

        server = new SocketIOServer(config);

        // NE PAS DÉMARRER LE SERVEUR ICI !
        // Le SocketIOEventHandler s'en chargera après avoir enregistré tous les listeners

        log.info("✅ Socket.io server configured (not started yet) on port {}", port);

        return server;
    }

    /**
     * Listener d'autorisation personnalisé pour JWT
     */
    private static class CustomAuthorizationListener implements AuthorizationListener {
        private final JwtService jwtService;

        public CustomAuthorizationListener(JwtService jwtService) {
            this.jwtService = jwtService;
        }

        @Override
        public AuthorizationResult getAuthorizationResult(HandshakeData handshakeData) {
            log.debug("🔐 Authorization check for new connection");

            try {
                // Extraire le token depuis les différentes sources possibles
                String token = extractToken(handshakeData);

                if (!StringUtils.hasText(token)) {
                    log.warn("❌ No token found in handshake");
                    return new AuthorizationResult(false);
                }

                // Valider le token
                if (!jwtService.validateToken(token)) {
                    log.warn("❌ Invalid token in handshake");
                    return new AuthorizationResult(false);
                }

                // Extraire les informations utilisateur
                String employeeNumber = jwtService.extractUsername(token);
                String tenantCode = jwtService.getTenantCode(token);

                log.info("✅ Authorization successful for {} from tenant {}",
                        employeeNumber, tenantCode);

                return new AuthorizationResult(true);

            } catch (Exception e) {
                log.error("❌ Authorization error: {}", e.getMessage());
                return new AuthorizationResult(false);
            }
        }

        private String extractToken(HandshakeData handshakeData) {
            // 1. Query parameter (priorité pour React Native)
            String token = handshakeData.getSingleUrlParam("token");
            if (StringUtils.hasText(token)) {
                log.debug("Token found in query parameter");
                return token;
            }

            // 2. Authorization header
            String authHeader = handshakeData.getHttpHeaders().get("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                log.debug("Token found in Authorization header");
                return authHeader.substring(7);
            }

            // 3. Custom header
            String customToken = handshakeData.getHttpHeaders().get("X-Auth-Token");
            if (StringUtils.hasText(customToken)) {
                log.debug("Token found in X-Auth-Token header");
                return customToken;
            }

            // 4. Cookie (pour compatibilité web)
            String cookieHeader = handshakeData.getHttpHeaders().get("Cookie");
            if (StringUtils.hasText(cookieHeader)) {
                String[] cookies = cookieHeader.split(";");
                for (String cookie : cookies) {
                    cookie = cookie.trim();
                    if (cookie.startsWith("accessToken=")) {
                        log.debug("Token found in cookie");
                        return cookie.substring("accessToken=".length());
                    }
                }
            }

            return null;
        }
    }

    /**
     * Arrêt propre du serveur
     * Sera appelé automatiquement à l'arrêt de l'application
     */
    @PreDestroy
    public void stopServer() {
        if (server != null) {
            log.info("🔴 Stopping Socket.io server...");
            server.stop();
            log.info("Socket.io server stopped");
        }
    }
}