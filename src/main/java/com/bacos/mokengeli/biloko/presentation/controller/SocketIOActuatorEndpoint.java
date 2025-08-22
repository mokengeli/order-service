package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.config.socketio.handler.SocketIOEventHandler;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Actuator endpoint pour monitorer Socket.io
 * Accessible via /actuator/socketio
 */
@Component
@Endpoint(id = "socketio")
public class SocketIOActuatorEndpoint {

    private final SocketIOServer server;
    private final SocketIOEventHandler eventHandler;

    public SocketIOActuatorEndpoint(SocketIOServer server, SocketIOEventHandler eventHandler) {
        this.server = server;
        this.eventHandler = eventHandler;
    }

    /**
     * GET /actuator/socketio
     * Retourne les métriques globales Socket.io
     */
    @ReadOperation
    public Map<String, Object> getSocketIOMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Status du serveur
        metrics.put("serverStatus", Map.of(
            "running", server != null,
            "port", server.getConfiguration().getPort(),
            "hostname", server.getConfiguration().getHostname(),
            "transports", server.getConfiguration().getTransports()
        ));
        
        // Clients connectés
        metrics.put("clients", Map.of(
            "total", server.getAllClients().size(),
            "authenticated", eventHandler.getStats().get("authenticatedSessions"),
            "sessionIds", server.getAllClients().stream()
                .limit(10) // Limiter pour éviter trop de données
                .map(client -> client.getSessionId().toString())
                .collect(Collectors.toList())
        ));
        
        // Namespaces et rooms
        metrics.put("namespaces", server.getAllNamespaces().size());
        
        // Stats du handler
        metrics.put("statistics", eventHandler.getStats());
        
        // Configuration
        metrics.put("configuration", Map.of(
            "pingInterval", server.getConfiguration().getPingInterval(),
            "pingTimeout", server.getConfiguration().getPingTimeout(),
            "maxFramePayloadLength", server.getConfiguration().getMaxFramePayloadLength(),
            "workerThreads", server.getConfiguration().getWorkerThreads()
        ));
        
        return metrics;
    }

    /**
     * GET /actuator/socketio/{clientId}
     * Retourne les infos d'un client spécifique
     */
    @ReadOperation
    public Map<String, Object> getClientInfo(@Selector String clientId) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            var client = server.getClient(java.util.UUID.fromString(clientId));
            if (client != null) {
                info.put("found", true);
                info.put("sessionId", client.getSessionId().toString());
                info.put("remoteAddress", client.getRemoteAddress().toString());
                info.put("transport", client.getTransport().toString());
                info.put("namespace", client.getNamespace().getName());
                info.put("allRooms", client.getAllRooms());
            } else {
                info.put("found", false);
                info.put("message", "Client not found");
            }
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    /**
     * POST /actuator/socketio/disconnect/{clientId}
     * Force la déconnexion d'un client
     */
    @WriteOperation
    public Map<String, String> disconnectClient(@Selector String clientId) {
        Map<String, String> result = new HashMap<>();
        
        try {
            var client = server.getClient(java.util.UUID.fromString(clientId));
            if (client != null) {
                client.disconnect();
                result.put("status", "disconnected");
                result.put("clientId", clientId);
            } else {
                result.put("status", "not_found");
                result.put("message", "Client not found");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        
        return result;
    }
}