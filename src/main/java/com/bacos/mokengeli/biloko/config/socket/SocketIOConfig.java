package com.bacos.mokengeli.biloko.config.socket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.bacos.mokengeli.biloko.config.service.JwtService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
public class SocketIOConfig {

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.port:9092}")
    private int port;

    private final JwtService jwtService;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setTransports(Transport.WEBSOCKET, Transport.POLLING);
        config.setAuthorizationListener(handshakeData -> {
            String token = handshakeData.getHttpHeaders().get("Authorization");
            if (token == null) {
                token = handshakeData.getSingleUrlParam("token");
            }
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return token != null && jwtService.validateToken(token);
        });
        return new SocketIOServer(config);
    }

    @Bean
    public SocketServerLifecycle socketServerLifecycle(SocketIOServer server) {
        return new SocketServerLifecycle(server);
    }

    public static class SocketServerLifecycle {
        private final SocketIOServer server;

        public SocketServerLifecycle(SocketIOServer server) {
            this.server = server;
        }

        @PostConstruct
        public void start() {
            server.start();
            log.info("Socket.IO server started on port {}", server.getConfiguration().getPort());
        }

        @PreDestroy
        public void stop() {
            server.stop();
            log.info("Socket.IO server stopped");
        }
    }
}
