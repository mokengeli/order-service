package com.bacos.mokengeli.biloko.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener pour surveiller les événements WebSocket/STOMP
 * Aide à diagnostiquer les problèmes de connexion
 */
@Slf4j
@Component
public class WebSocketSessionListener {

    // Suivi des sessions actives
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";

        SessionInfo info = new SessionInfo(sessionId, user, System.currentTimeMillis());
        activeSessions.put(sessionId, info);

        log.info("🟢 WebSocket SESSION CONNECTED - Session: {} | User: {} | Active sessions: {}",
                sessionId, user, activeSessions.size());

        // Log des attributs de session pour debug
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            log.debug("Session attributes: {}", sessionAttributes.keySet());
            log.debug("Authenticated: {}", sessionAttributes.get("authenticated"));
            log.debug("Employee: {}", sessionAttributes.get("employeeNumber"));
            log.debug("Tenant: {}", sessionAttributes.get("tenantCode"));
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        SessionInfo info = activeSessions.remove(sessionId);
        if (info != null) {
            long duration = System.currentTimeMillis() - info.connectedAt;
            log.info("🔴 WebSocket SESSION DISCONNECTED - Session: {} | User: {} | Duration: {}ms | Active sessions: {}",
                    sessionId, info.user, duration, activeSessions.size());

            // Si la session a duré moins de 100ms, c'est suspect
            if (duration < 100) {
                log.warn("⚠️ Session disconnected immediately after connection! This might indicate a client issue.");
                log.warn("Check if the client is sending STOMP CONNECT frame properly.");
            }
        } else {
            log.warn("🔴 WebSocket SESSION DISCONNECTED - Unknown session: {}", sessionId);
        }
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";

        log.info("📡 WebSocket SUBSCRIPTION - Session: {} | User: {} | Destination: {}",
                sessionId, user, destination);

        // Mettre à jour l'info de session
        SessionInfo info = activeSessions.get(sessionId);
        if (info != null) {
            info.subscriptionCount++;
            info.lastActivity = System.currentTimeMillis();
        }
    }

    /**
     * Obtenir les statistiques des sessions actives
     */
    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("activeSessionCount", activeSessions.size());
        stats.put("sessions", activeSessions);
        return stats;
    }

    /**
     * Classe interne pour stocker les infos de session
     */
    private static class SessionInfo {
        String sessionId;
        String user;
        long connectedAt;
        long lastActivity;
        int subscriptionCount = 0;

        SessionInfo(String sessionId, String user, long connectedAt) {
            this.sessionId = sessionId;
            this.user = user;
            this.connectedAt = connectedAt;
            this.lastActivity = connectedAt;
        }

        @Override
        public String toString() {
            return String.format("SessionInfo{user='%s', connected=%d, subscriptions=%d}",
                    user, connectedAt, subscriptionCount);
        }
    }
}