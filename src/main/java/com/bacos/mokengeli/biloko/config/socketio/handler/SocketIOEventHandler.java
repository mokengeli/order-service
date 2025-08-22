package com.bacos.mokengeli.biloko.config.socketio.handler;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.config.service.JwtService;
import com.bacos.mokengeli.biloko.infrastructure.socketio.model.*;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire central des événements Socket.io
 * Gère les connexions, authentifications, et événements métier
 */
@Slf4j
@Component
public class SocketIOEventHandler {

    private final SocketIOServer server;
    private final JwtService jwtService;

    // Gestion des sessions et rooms
    private final Map<UUID, ClientSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<UUID>> tenantRooms = new ConcurrentHashMap<>();
    private final Map<String, UUID> employeeToSessionId = new ConcurrentHashMap<>();

    // Métriques
    private long totalConnections = 0;
    private long totalMessages = 0;
    private long totalErrors = 0;

    @Autowired
    public SocketIOEventHandler(SocketIOServer server, JwtService jwtService) {
        this.server = server;
        this.jwtService = jwtService;
    }

    @PostConstruct
    public void init() {
        log.info("🎯 Initializing Socket.io event handlers...");

        // Enregistrer tous les listeners AVANT de démarrer le serveur
        registerConnectionHandlers();
        registerAuthenticationHandlers();
        registerBusinessEventHandlers();
        registerUtilityHandlers();

        // MAINTENANT démarrer le serveur
        startServer();

        log.info("✅ Socket.io event handlers registered and server started successfully");
    }

    /**
     * Démarrage du serveur après configuration des handlers
     */
    private void startServer() {
        try {
            server.start();
            log.info("🟢 Socket.io server started successfully on port {}",
                    server.getConfiguration().getPort());
            log.info("📡 Accepting connections at ws://{}:{}/socket.io/",
                    server.getConfiguration().getHostname(),
                    server.getConfiguration().getPort());
        } catch (Exception e) {
            log.error("❌ Failed to start Socket.io server", e);
            throw new RuntimeException("Socket.io server startup failed", e);
        }
    }

    /**
     * Gestion des connexions/déconnexions
     */
    private void registerConnectionHandlers() {
        // Connexion
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                handleConnect(client);
            }
        });

        // Déconnexion
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                handleDisconnect(client);
            }
        });

        log.info("✅ Connection handlers registered");
    }

    /**
     * Gestion de l'authentification
     */
    private void registerAuthenticationHandlers() {
        // Événement "authenticate" envoyé par le client après connexion
        server.addEventListener("authenticate", AuthenticationRequest.class,
                new DataListener<AuthenticationRequest>() {
                    @Override
                    public void onData(SocketIOClient client, AuthenticationRequest data, AckRequest ackRequest) {
                        handleAuthentication(client, data, ackRequest);
                    }
                });

        // Rejoindre une room tenant
        server.addEventListener("join:tenant", JoinTenantRequest.class,
                new DataListener<JoinTenantRequest>() {
                    @Override
                    public void onData(SocketIOClient client, JoinTenantRequest data, AckRequest ackRequest) {
                        handleJoinTenant(client, data, ackRequest);
                    }
                });

        log.info("✅ Authentication handlers registered");
    }

    /**
     * Événements métier
     */
    private void registerBusinessEventHandlers() {
        // Actions sur les commandes
        server.addEventListener("order:action", OrderActionRequest.class,
                new DataListener<OrderActionRequest>() {
                    @Override
                    public void onData(SocketIOClient client, OrderActionRequest data, AckRequest ackRequest) {
                        handleOrderAction(client, data, ackRequest);
                    }
                });

        // Changement statut table
        server.addEventListener("table:status", TableStatusRequest.class,
                new DataListener<TableStatusRequest>() {
                    @Override
                    public void onData(SocketIOClient client, TableStatusRequest data, AckRequest ackRequest) {
                        handleTableStatus(client, data, ackRequest);
                    }
                });

        // Validation de dette
        server.addEventListener("debt:validate", DebtValidationRequest.class,
                new DataListener<DebtValidationRequest>() {
                    @Override
                    public void onData(SocketIOClient client, DebtValidationRequest data, AckRequest ackRequest) {
                        handleDebtValidation(client, data, ackRequest);
                    }
                });

        log.info("✅ Business event handlers registered");
    }

    /**
     * Événements utilitaires
     */
    private void registerUtilityHandlers() {
        // Ping/Pong pour latence
        server.addEventListener("ping", Map.class,
                new DataListener<Map>() {
                    @Override
                    public void onData(SocketIOClient client, Map data, AckRequest ackRequest) {
                        handlePing(client);
                    }
                });

        // Echo pour tests
        server.addEventListener("echo", Map.class,
                new DataListener<Map>() {
                    @Override
                    public void onData(SocketIOClient client, Map data, AckRequest ackRequest) {
                        handleEcho(client, data, ackRequest);
                    }
                });

        log.info("✅ Utility handlers registered");
    }

    /**
     * Gestion de la connexion initiale
     */
    private void handleConnect(SocketIOClient client) {
        UUID sessionId = client.getSessionId();
        String remoteAddress = client.getRemoteAddress().toString();

        totalConnections++;

        log.info("🔌 New Socket.io connection - Session: {} from {}", sessionId, remoteAddress);

        // Extraire le token depuis le handshake
        String token = extractTokenFromClient(client);

        if (StringUtils.hasText(token)) {
            try {
                // Pré-authentification basée sur le handshake
                String employeeNumber = jwtService.extractUsername(token);
                String tenantCode = jwtService.getTenantCode(token);
                List<String> roles = jwtService.getRoles(token);

                // Créer la session
                ClientSession session = ClientSession.builder()
                        .sessionId(sessionId)
                        .employeeNumber(employeeNumber)
                        .tenantCode(tenantCode)
                        .roles(roles)
                        .connectedAt(LocalDateTime.now())
                        .authenticated(true)
                        .build();

                sessions.put(sessionId, session);
                employeeToSessionId.put(employeeNumber, sessionId);

                // Auto-join tenant room
                joinTenantRoom(client, tenantCode);

                // Envoyer confirmation
                client.sendEvent("authenticated", Map.of(
                        "success", true,
                        "employeeNumber", employeeNumber,
                        "tenantCode", tenantCode,
                        "sessionId", sessionId.toString()
                ));

                log.info("✅ Auto-authenticated: {} from tenant {}", employeeNumber, tenantCode);

            } catch (Exception e) {
                log.warn("⚠️ Pre-authentication failed, waiting for explicit auth: {}", e.getMessage());

                // Créer une session non authentifiée
                ClientSession session = ClientSession.builder()
                        .sessionId(sessionId)
                        .connectedAt(LocalDateTime.now())
                        .authenticated(false)
                        .build();

                sessions.put(sessionId, session);
            }
        } else {
            // Pas de token au handshake, attendre l'authentification explicite
            ClientSession session = ClientSession.builder()
                    .sessionId(sessionId)
                    .connectedAt(LocalDateTime.now())
                    .authenticated(false)
                    .build();

            sessions.put(sessionId, session);

            log.info("⏳ Connection without token, waiting for authentication");
        }
    }

    /**
     * Gestion de l'authentification explicite
     */
    private void handleAuthentication(SocketIOClient client, AuthenticationRequest request, AckRequest ackRequest) {
        UUID sessionId = client.getSessionId();

        log.info("🔐 Authentication request from session: {}", sessionId);

        try {
            String token = request.getToken();

            if (!StringUtils.hasText(token) || !jwtService.validateToken(token)) {
                throw new SecurityException("Invalid or missing token");
            }

            // Extraire les infos utilisateur
            String employeeNumber = jwtService.extractUsername(token);
            String tenantCode = request.getTenantCode() != null ?
                    request.getTenantCode() : jwtService.getTenantCode(token);
            List<String> roles = jwtService.getRoles(token);

            // Mettre à jour la session
            ClientSession session = sessions.get(sessionId);
            if (session != null) {
                session.setEmployeeNumber(employeeNumber);
                session.setTenantCode(tenantCode);
                session.setRoles(roles);
                session.setAuthenticated(true);

                employeeToSessionId.put(employeeNumber, sessionId);

                // Auto-join tenant room
                joinTenantRoom(client, tenantCode);
            }

            // Réponse avec ACK
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of(
                        "success", true,
                        "employeeNumber", employeeNumber,
                        "tenantCode", tenantCode
                ));
            }

            // Événement de confirmation
            client.sendEvent("authenticated", Map.of(
                    "success", true,
                    "employeeNumber", employeeNumber,
                    "tenantCode", tenantCode,
                    "timestamp", LocalDateTime.now().toString()
            ));

            log.info("✅ Authentication successful: {} from tenant {}", employeeNumber, tenantCode);

        } catch (Exception e) {
            log.error("❌ Authentication failed: {}", e.getMessage());

            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of(
                        "success", false,
                        "error", e.getMessage()
                ));
            }

            client.sendEvent("unauthorized", Map.of(
                    "error", e.getMessage()
            ));

            // Déconnecter après échec d'auth
            client.disconnect();
        }
    }

    /**
     * Rejoindre une room tenant
     */
    private void handleJoinTenant(SocketIOClient client, JoinTenantRequest request, AckRequest ackRequest) {
        UUID sessionId = client.getSessionId();
        ClientSession session = sessions.get(sessionId);

        if (session == null || !session.isAuthenticated()) {
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of("success", false, "error", "Not authenticated"));
            }
            return;
        }

        String requestedTenant = request.getTenantCode();

        // Vérifier que l'utilisateur peut rejoindre ce tenant
        if (!session.getTenantCode().equals(requestedTenant)) {
            log.warn("❌ Unauthorized tenant join: {} tried to join {}",
                    session.getTenantCode(), requestedTenant);
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of("success", false, "error", "Unauthorized tenant"));
            }
            return;
        }

        joinTenantRoom(client, requestedTenant);

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(Map.of("success", true, "tenant", requestedTenant));
        }
    }

    /**
     * Helper pour rejoindre une room tenant
     */
    private void joinTenantRoom(SocketIOClient client, String tenantCode) {
        String roomName = "tenant:" + tenantCode;
        client.joinRoom(roomName);

        // Tracker les rooms
        tenantRooms.computeIfAbsent(tenantCode, k -> ConcurrentHashMap.newKeySet())
                .add(client.getSessionId());

        log.info("📡 Client {} joined room: {}", client.getSessionId(), roomName);
    }

    /**
     * Actions sur les commandes
     */
    private void handleOrderAction(SocketIOClient client, OrderActionRequest request, AckRequest ackRequest) {
        ClientSession session = sessions.get(client.getSessionId());

        if (session == null || !session.isAuthenticated()) {
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of("success", false, "error", "Not authenticated"));
            }
            return;
        }

        log.info("📦 Order action: {} on order {} by {}",
                request.getAction(), request.getOrderId(), session.getEmployeeNumber());

        totalMessages++;

        // TODO: Implémenter la logique métier
        // Pour l'instant, on simule une réponse

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(Map.of(
                    "success", true,
                    "orderId", request.getOrderId(),
                    "action", request.getAction(),
                    "timestamp", LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * Changement de statut de table
     */
    private void handleTableStatus(SocketIOClient client, TableStatusRequest request, AckRequest ackRequest) {
        ClientSession session = sessions.get(client.getSessionId());

        if (session == null || !session.isAuthenticated()) {
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of("success", false, "error", "Not authenticated"));
            }
            return;
        }

        log.info("🪑 Table status change: table {} to {} by {}",
                request.getTableId(), request.getNewStatus(), session.getEmployeeNumber());

        totalMessages++;

        // Notifier tous les clients du tenant
        String roomName = "tenant:" + session.getTenantCode();
        server.getRoomOperations(roomName).sendEvent("table:update", Map.of(
                "tableId", request.getTableId(),
                "newStatus", request.getNewStatus(),
                "updatedBy", session.getEmployeeNumber(),
                "timestamp", LocalDateTime.now().toString()
        ));

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(Map.of("success", true));
        }
    }

    /**
     * Validation de dette
     */
    private void handleDebtValidation(SocketIOClient client, DebtValidationRequest request, AckRequest ackRequest) {
        ClientSession session = sessions.get(client.getSessionId());

        if (session == null || !session.isAuthenticated()) {
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of("success", false, "error", "Not authenticated"));
            }
            return;
        }

        log.info("💰 Debt validation request: {} for customer {} by {}",
                request.getValidationType(), request.getCustomerId(), session.getEmployeeNumber());

        totalMessages++;

        // TODO: Implémenter la logique de validation

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(Map.of(
                    "success", true,
                    "customerId", request.getCustomerId(),
                    "validated", true
            ));
        }
    }

    /**
     * Ping/Pong pour mesurer la latence
     */
    private void handlePing(SocketIOClient client) {
        client.sendEvent("pong", Map.of(
                "timestamp", System.currentTimeMillis(),
                "sessionId", client.getSessionId().toString()
        ));
    }

    /**
     * Echo pour tests
     */
    private void handleEcho(SocketIOClient client, Map data, AckRequest ackRequest) {
        ClientSession session = sessions.get(client.getSessionId());

        Map<String, Object> response = new HashMap<>();
        response.put("echo", data);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("sessionId", client.getSessionId().toString());

        if (session != null && session.isAuthenticated()) {
            response.put("employeeNumber", session.getEmployeeNumber());
            response.put("tenantCode", session.getTenantCode());
        }

        client.sendEvent("echo:response", response);

        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(response);
        }
    }

    /**
     * Gestion de la déconnexion
     */
    private void handleDisconnect(SocketIOClient client) {
        UUID sessionId = client.getSessionId();
        ClientSession session = sessions.remove(sessionId);

        if (session != null) {
            // Retirer des rooms
            if (session.getTenantCode() != null) {
                Set<UUID> tenantSessions = tenantRooms.get(session.getTenantCode());
                if (tenantSessions != null) {
                    tenantSessions.remove(sessionId);
                }
            }

            // Retirer le mapping employé
            if (session.getEmployeeNumber() != null) {
                employeeToSessionId.remove(session.getEmployeeNumber());
            }

            long duration = java.time.Duration.between(session.getConnectedAt(), LocalDateTime.now()).toMillis();

            log.info("👋 Socket.io disconnection - Session: {} | User: {} | Duration: {}ms",
                    sessionId, session.getEmployeeNumber(), duration);
        } else {
            log.info("👋 Socket.io disconnection - Session: {} (unauthenticated)", sessionId);
        }
    }

    /**
     * Méthode publique pour envoyer une notification à un tenant
     */
    public void sendNotificationToTenant(String tenantCode, OrderNotification notification) {
        String roomName = "tenant:" + tenantCode;

        // Formater la notification pour Socket.io
        Map<String, Object> payload = Map.of(
                "orderId", notification.getOrderId(),
                "tableId", notification.getTableId(),
                "tenantCode", notification.getTenantCode(),
                "orderStatus", notification.getOrderStatus().toString(),
                "newState", notification.getNewState(),
                "previousState", notification.getPreviousState(),
                "tableState", notification.getTableState(),
                "timestamp", notification.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        server.getRoomOperations(roomName).sendEvent("order:notification", payload);

        log.info("📨 Sent order notification to tenant {} - Order: {}", tenantCode, notification.getOrderId());
    }

    /**
     * Broadcast à un tenant spécifique
     */
    public void broadcastToTenant(String tenantCode, String eventName, Object data) {
        String roomName = "tenant:" + tenantCode;
        server.getRoomOperations(roomName).sendEvent(eventName, data);
        totalMessages++;
    }

    /**
     * Extraire le token du client
     */
    private String extractTokenFromClient(SocketIOClient client) {
        // Le token devrait être dans les paramètres de handshake
        String token = client.getHandshakeData().getSingleUrlParam("token");

        if (!StringUtils.hasText(token)) {
            // Essayer depuis les headers
            token = client.getHandshakeData().getHttpHeaders().get("Authorization");
            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }

        return token;
    }

    /**
     * Obtenir les statistiques
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeSessions", sessions.size());
        stats.put("authenticatedSessions", sessions.values().stream()
                .filter(ClientSession::isAuthenticated).count());
        stats.put("totalConnections", totalConnections);
        stats.put("totalMessages", totalMessages);
        stats.put("totalErrors", totalErrors);
        stats.put("tenantRooms", tenantRooms.keySet());
        return stats;
    }

    /**
     * Arrêt propre
     */
    @PreDestroy
    public void shutdown() {
        log.info("🔴 Shutting down Socket.io event handler...");
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Classe interne pour gérer les sessions
     */
    @lombok.Data
    @lombok.Builder
    private static class ClientSession {
        private UUID sessionId;
        private String employeeNumber;
        private String tenantCode;
        private List<String> roles;
        private LocalDateTime connectedAt;
        private boolean authenticated;
    }
}