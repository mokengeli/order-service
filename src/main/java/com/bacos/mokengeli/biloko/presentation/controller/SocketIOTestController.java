package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.config.socketio.handler.SocketIOEventHandler;
import com.bacos.mokengeli.biloko.infrastructure.adapter.socketio.SocketIONotificationAdapter;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller de test pour Socket.io
 * Permet de tester les connexions et l'envoi de messages
 */
@Slf4j
@RestController
@RequestMapping("/api/order/socketio")
public class SocketIOTestController {

    private final SocketIOServer socketIOServer;
    private final SocketIOEventHandler eventHandler;
    private final SocketIONotificationAdapter notificationAdapter;

    @Value("${socketio.port:9092}")
    private int socketioPort;

    @Autowired
    public SocketIOTestController(
            SocketIOServer socketIOServer,
            SocketIOEventHandler eventHandler,
            SocketIONotificationAdapter notificationAdapter) {
        this.socketIOServer = socketIOServer;
        this.eventHandler = eventHandler;
        this.notificationAdapter = notificationAdapter;
    }

    /**
     * Status de Socket.io
     */
    @GetMapping("/status")
    public Map<String, Object> getSocketIOStatus() {
        log.info("🔍 Socket.io status check");

        Map<String, Object> status = new HashMap<>();
        status.put("server", "Socket.io Server");
        status.put("running", socketIOServer != null);
        status.put("port", socketioPort);
        status.put("clients", socketIOServer.getAllClients().size());
        status.put("namespaces", socketIOServer.getAllNamespaces().size());
        status.put("stats", eventHandler.getStats());
        status.put("timestamp", OffsetDateTime.now());

        return status;
    }

    /**
     * Test de connexion Socket.io
     */
    @GetMapping("/test")
    public Map<String, Object> testSocketIOConfig() {
        log.info("🧪 Socket.io configuration test");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("socketioEndpoint", "ws://localhost:" + socketioPort + "/socket.io/");
        response.put("transports", new String[]{"websocket", "polling"});
        response.put("authentication", "JWT in query param or header");
        response.put("events", new String[]{
                "authenticate",
                "join:tenant",
                "order:notification",
                "table:update",
                "dish:ready",
                "payment:update",
                "validation:required",
                "tenant:broadcast"
        });
        response.put("connectedClients", socketIOServer.getAllClients().size());
        response.put("timestamp", OffsetDateTime.now());

        return response;
    }

    /**
     * Envoyer une notification de test via Socket.io
     */
    @PostMapping("/test-notification")
    public Map<String, Object> sendTestNotification(
            @RequestParam String tenantCode,
            @RequestParam(defaultValue = "1") Long orderId,
            @RequestParam(defaultValue = "1") Long tableId,
            @RequestParam(defaultValue = "NEW_ORDER") String orderStatus) {

        log.info("📤 Sending Socket.io test notification to tenant: {}", tenantCode);

        try {
            OrderNotification notification = OrderNotification.builder()
                    .orderId(orderId)
                    .tableId(tableId)
                    .tenantCode(tenantCode)
                    .orderStatus(OrderNotification.OrderNotificationStatus.valueOf(orderStatus))
                    .tableState("OCCUPIED")
                    .newState("CONFIRMED")
                    .previousState("PENDING")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Envoyer directement via Socket.io
            eventHandler.sendNotificationToTenant(tenantCode, notification);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "sent");
            response.put("transport", "Socket.io");
            response.put("tenant", tenantCode);
            response.put("orderId", orderId);
            response.put("timestamp", LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("❌ Error sending Socket.io notification", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return error;
        }
    }

    /**
     * Broadcast à un tenant via Socket.io
     */
    @PostMapping("/broadcast")
    public Map<String, Object> broadcastToTenant(
            @RequestParam String tenantCode,
            @RequestParam String eventName,
            @RequestBody Map<String, Object> data) {

        log.info("📡 Broadcasting {} to tenant {} via Socket.io", eventName, tenantCode);

        try {
            eventHandler.broadcastToTenant(tenantCode, eventName, data);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "broadcasted");
            response.put("transport", "Socket.io");
            response.put("tenant", tenantCode);
            response.put("event", eventName);
            response.put("timestamp", LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("❌ Socket.io broadcast error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return error;
        }
    }

    /**
     * Obtenir les statistiques de notification
     */
    @GetMapping("/notification-stats")
    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("adapter", notificationAdapter.getStats());
        stats.put("socketio", eventHandler.getStats());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }

    /**
     * Changer le mode de migration (pour tests)
     */
    @PostMapping("/migration-mode")
    public Map<String, String> changeMigrationMode(
            @RequestParam SocketIONotificationAdapter.MigrationMode mode) {

        log.warn("⚠️ Changing migration mode to: {}", mode);
        notificationAdapter.setMigrationMode(mode);

        Map<String, String> response = new HashMap<>();
        response.put("status", "changed");
        response.put("mode", mode.toString());
        return response;
    }

    /**
     * Liste des clients connectés
     */
    @GetMapping("/clients")
    public Map<String, Object> getConnectedClients() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalClients", socketIOServer.getAllClients().size());
        response.put("clientIds", socketIOServer.getAllClients().stream()
                .map(client -> client.getSessionId().toString())
                .toList());
        return response;
    }
}