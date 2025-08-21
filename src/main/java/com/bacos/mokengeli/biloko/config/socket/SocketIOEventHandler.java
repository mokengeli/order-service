package com.bacos.mokengeli.biloko.config.socket;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.bacos.mokengeli.biloko.config.service.JwtService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketIOEventHandler {

    private final SocketIOServer server;
    private final JwtService jwtService;

    @PostConstruct
    public void init() {
        server.addConnectListener(this::onConnect);
        server.addDisconnectListener(this::onDisconnect);
    }

    private void onConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getHttpHeaders().get("Authorization");
        if (token == null) {
            token = client.getHandshakeData().getSingleUrlParam("token");
        }
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || !jwtService.validateToken(token)) {
            log.warn("Connection refused for client {}", client.getSessionId());
            client.disconnect();
            return;
        }
        String tenantCode = jwtService.getTenantCode(token);
        client.set("tenantCode", tenantCode);
        client.joinRoom("tenant:" + tenantCode);
        log.info("Client {} connected for tenant {}", client.getSessionId(), tenantCode);
    }

    private void onDisconnect(SocketIOClient client) {
        log.info("Client {} disconnected", client.getSessionId());
    }
}
