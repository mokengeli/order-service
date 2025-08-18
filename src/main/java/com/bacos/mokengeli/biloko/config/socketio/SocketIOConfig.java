package com.bacos.mokengeli.biloko.config.socketio;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@Configuration
public class SocketIOConfig {

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.port:8081}")
    private int port;

    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer(SocketIOHandshakeInterceptor handshakeInterceptor) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setContext("/socket.io/");
        config.setAllowCustomRequests(true);

        SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(handshakeInterceptor);
        server.addDisconnectListener(handshakeInterceptor);

        server.addEventListener("join-tenant", Map.class, (client, data, ackRequest) -> {
            String tenantCode = data != null ? (String) data.get("tenantCode") : null;
            if (tenantCode != null) {
                client.joinRoom(tenantCode);
                if (ackRequest.isAckRequested()) {
                    ackRequest.sendAckData(Collections.singletonMap("success", true));
                }
            }
        });

        server.addEventListener("leave-tenant", Map.class, (client, data, ackRequest) -> {
            String tenantCode = data != null ? (String) data.get("tenantCode") : null;
            if (tenantCode != null) {
                client.leaveRoom(tenantCode);
                if (ackRequest.isAckRequested()) {
                    ackRequest.sendAckData(Collections.singletonMap("success", true));
                }
            }
        });

        server.start();
        return server;
    }
}
