package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.port.OrderNotificationPort;
import com.bacos.mokengeli.biloko.config.socketio.handler.SocketIOEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Adapter Socket.IO pour notifications de commandes
 * Remplace complètement l'ancien système WebSocket STOMP
 */
@Slf4j
@Component
@Primary
public class OrderNotificationAdapter implements OrderNotificationPort {

    private final SocketIOEventHandler socketIOEventHandler;

    // Compteur pour monitoring
    private long totalNotifications = 0;

    @Autowired
    public OrderNotificationAdapter(SocketIOEventHandler socketIOEventHandler) {
        this.socketIOEventHandler = socketIOEventHandler;
    }

    @Override
    public void notifyWebSocketUser(OrderNotification notification) {
        log.info("📤 Sending Socket.IO notification | Order: {} | Tenant: {}",
                notification.getOrderId(), notification.getTenantCode());

        try {
            socketIOEventHandler.sendNotificationToTenant(
                    notification.getTenantCode(),
                    notification
            );
            totalNotifications++;

            log.debug("✅ Socket.IO notification sent to tenant {} - Total: {}",
                    notification.getTenantCode(), totalNotifications);
        } catch (Exception e) {
            log.error("❌ Socket.IO notification failed", e);
            // Ne pas faire échouer le processus métier
        }
    }

    /**
     * Broadcast générique à un tenant
     */
    public void broadcastToTenant(String tenantCode, String eventType, Object data) {
        log.info("📡 Broadcasting {} to tenant {}", eventType, tenantCode);

        try {
            socketIOEventHandler.broadcastToTenant(tenantCode, eventType, data);
            totalNotifications++;
        } catch (Exception e) {
            log.error("❌ Broadcast failed", e);
        }
    }

    /**
     * Obtenir les statistiques de notification
     */
    public NotificationStats getStats() {
        return NotificationStats.builder()
                .totalNotifications(totalNotifications)
                .protocol("Socket.IO")
                .build();
    }

    /**
     * Statistiques de notification
     */
    @lombok.Data
    @lombok.Builder
    public static class NotificationStats {
        private long totalNotifications;
        private String protocol;
    }
}