package com.bacos.mokengeli.biloko.infrastructure.adapter.socketio;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import com.bacos.mokengeli.biloko.config.socketio.handler.SocketIOEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter dual pour notifications - supporte STOMP et Socket.io
 * Permet une migration progressive
 */
@Slf4j
@Component
@Primary // Remplace l'adapter STOMP par défaut
public class SocketIONotificationAdapter implements OrderNotificationPort {

    private final SimpMessagingTemplate messagingTemplate;
    private final SocketIOEventHandler socketIOEventHandler;

    @Value("${socketio.migration.mode:DUAL}")
    private MigrationMode migrationMode;

    private static final String STOMP_ORDER_DESTINATION = "/topic/orders/";

    // Compteurs pour monitoring
    private long stompNotifications = 0;
    private long socketioNotifications = 0;

    @Autowired
    public SocketIONotificationAdapter(
            SimpMessagingTemplate messagingTemplate,
            SocketIOEventHandler socketIOEventHandler) {
        this.messagingTemplate = messagingTemplate;
        this.socketIOEventHandler = socketIOEventHandler;
    }

    @Override
    public void notifyWebSocketUser(OrderNotification notification) {
        log.info("📤 Sending notification - Mode: {} | Order: {} | Tenant: {}",
                migrationMode, notification.getOrderId(), notification.getTenantCode());

        try {
            switch (migrationMode) {
                case STOMP_ONLY:
                    sendViaSTOMP(notification);
                    break;

                case SOCKETIO_ONLY:
                    sendViaSocketIO(notification);
                    break;

                case DUAL:
                default:
                    // Envoyer sur les deux canaux pendant la migration
                    sendViaSTOMP(notification);
                    sendViaSocketIO(notification);
                    break;
            }
        } catch (Exception e) {
            log.error("❌ Error sending notification", e);
            // Ne pas faire échouer le processus métier
        }
    }

    /**
     * Envoi via STOMP (legacy)
     */
    private void sendViaSTOMP(OrderNotification notification) {
        try {
            String destination = STOMP_ORDER_DESTINATION + notification.getTenantCode();
            messagingTemplate.convertAndSend(destination, notification);
            stompNotifications++;

            log.debug("✅ STOMP notification sent to {} - Total: {}", destination, stompNotifications);
        } catch (Exception e) {
            log.error("❌ STOMP send failed", e);
        }
    }

    /**
     * Envoi via Socket.io (nouveau)
     */
    private void sendViaSocketIO(OrderNotification notification) {
        try {
            socketIOEventHandler.sendNotificationToTenant(
                    notification.getTenantCode(),
                    notification
            );
            socketioNotifications++;

            log.debug("✅ Socket.io notification sent to tenant {} - Total: {}",
                    notification.getTenantCode(), socketioNotifications);
        } catch (Exception e) {
            log.error("❌ Socket.io send failed", e);
        }
    }

    /**
     * Broadcast générique à un tenant
     */
    public void broadcastToTenant(String tenantCode, String eventType, Object data) {
        log.info("📡 Broadcasting {} to tenant {}", eventType, tenantCode);

        try {
            switch (migrationMode) {
                case STOMP_ONLY:
                    String stompDestination = "/topic/" + eventType + "/" + tenantCode;
                    messagingTemplate.convertAndSend(stompDestination, data);
                    break;

                case SOCKETIO_ONLY:
                    socketIOEventHandler.broadcastToTenant(tenantCode, eventType, data);
                    break;

                case DUAL:
                default:
                    // Broadcast sur les deux
                    String dualStompDest = "/topic/" + eventType + "/" + tenantCode;
                    messagingTemplate.convertAndSend(dualStompDest, data);
                    socketIOEventHandler.broadcastToTenant(tenantCode, eventType, data);
                    break;
            }
        } catch (Exception e) {
            log.error("❌ Broadcast failed", e);
        }
    }

    /**
     * Obtenir les statistiques de notification
     */
    public NotificationStats getStats() {
        return NotificationStats.builder()
                .mode(migrationMode.toString())
                .stompCount(stompNotifications)
                .socketioCount(socketioNotifications)
                .totalCount(stompNotifications + socketioNotifications)
                .build();
    }

    /**
     * Changer le mode de migration à chaud (pour tests)
     */
    public void setMigrationMode(MigrationMode mode) {
        log.warn("⚠️ Changing migration mode from {} to {}", this.migrationMode, mode);
        this.migrationMode = mode;
    }

    /**
     * Modes de migration
     */
    public enum MigrationMode {
        STOMP_ONLY,     // Ancien système uniquement
        SOCKETIO_ONLY,  // Nouveau système uniquement
        DUAL            // Les deux (défaut pendant migration)
    }

    /**
     * Statistiques de notification
     */
    @lombok.Data
    @lombok.Builder
    public static class NotificationStats {
        private String mode;
        private long stompCount;
        private long socketioCount;
        private long totalCount;
    }
}