package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order/ws")
public class WebSocketTestController {

    private final OrderNotificationPort notificationPort;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketTestController(OrderNotificationPort notificationPort,
                                   SimpMessagingTemplate messagingTemplate) {
        this.notificationPort = notificationPort;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Test de configuration WebSocket
     */
    @GetMapping("/test")
    public Map<String, Object> testWebSocketConfig() {
        log.info("🧪 WebSocket configuration test endpoint called");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("nativeWebSocketEndpoint", "/api/order/ws/websocket");
        response.put("legacySockJSEndpoint", "/api/order/ws");
        response.put("stompBrokerPrefixes", new String[]{"/topic", "/queue"});
        response.put("applicationPrefixes", new String[]{"/ws", "/app"});
        response.put("timestamp", OffsetDateTime.now());

        return response;
    }

    /**
     * Status public des WebSockets
     */
    @GetMapping("/status")
    public Map<String, String> wsStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("nativeWebSocket", "Available at /api/order/ws/websocket");
        status.put("sockJS", "Available at /api/order/ws (legacy)");
        status.put("protocol", "STOMP 1.2");
        status.put("heartbeat", "30000ms");
        return status;
    }

    /**
     * Test d'envoi de notification (endpoint REST pour tester)
     */
    @PostMapping("/test-notification")
    public Map<String, String> sendTestNotification(
            @RequestParam String tenantCode,
            @RequestParam(defaultValue = "1") Long orderId,
            @RequestParam(defaultValue = "1") Long tableId) {

        log.info("📤 Sending test notification to tenant: {}", tenantCode);

        try {
            OrderNotification notification = OrderNotification.builder()
                    .orderId(orderId)
                    .tableId(tableId)
                    .tenantCode(tenantCode)
                    .orderStatus(OrderNotification.OrderNotificationStatus.NEW_ORDER)
                    .tableState("OCCUPIED")
                    .newState("CONFIRMED")
                    .previousState("PENDING")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Envoyer via le port standard
            notificationPort.notifyWebSocketUser(notification);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sent");
            response.put("destination", "/topic/orders/" + tenantCode);
            response.put("orderId", String.valueOf(orderId));
            return response;

        } catch (Exception e) {
            log.error("❌ Error sending test notification", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return error;
        }
    }

    /**
     * Echo endpoint STOMP pour tester la connexion bidirectionnelle
     * Client envoie à: /ws/echo ou /app/echo
     * Réponse broadcasted à: /topic/echo
     */
    @MessageMapping("/echo")
    @SendTo("/topic/echo")
    public Map<String, Object> echo(Map<String, String> message, Principal principal) {
        log.info("📨 Echo received from: {} - Message: {}",
                principal != null ? principal.getName() : "anonymous",
                message);

        Map<String, Object> response = new HashMap<>();
        response.put("originalMessage", message);
        response.put("timestamp", OffsetDateTime.now().toString());
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("serverEcho", "Message received and echoed back!");

        return response;
    }

    /**
     * Echo privé - répond uniquement à l'utilisateur qui a envoyé
     * Client envoie à: /ws/private-echo ou /app/private-echo
     * Réponse à: /user/queue/reply
     */
    @MessageMapping("/private-echo")
    @SendToUser("/queue/reply")
    public Map<String, Object> privateEcho(Map<String, String> message, Principal principal) {
        log.info("🔒 Private echo from: {}", principal != null ? principal.getName() : "anonymous");

        Map<String, Object> response = new HashMap<>();
        response.put("originalMessage", message);
        response.put("timestamp", OffsetDateTime.now().toString());
        response.put("privateReply", true);
        response.put("user", principal != null ? principal.getName() : "anonymous");

        return response;
    }

    /**
     * Test direct d'envoi via SimpMessagingTemplate
     */
    @PostMapping("/broadcast")
    public Map<String, String> broadcastMessage(
            @RequestParam String destination,
            @RequestBody Map<String, Object> message) {

        log.info("📡 Broadcasting to: {} - Message: {}", destination, message);

        try {
            messagingTemplate.convertAndSend(destination, message);

            Map<String, String> response = new HashMap<>();
            response.put("status", "broadcasted");
            response.put("destination", destination);
            return response;

        } catch (Exception e) {
            log.error("❌ Broadcast error", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return error;
        }
    }
}