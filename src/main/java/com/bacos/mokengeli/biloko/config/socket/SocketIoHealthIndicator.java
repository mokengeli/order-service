package com.bacos.mokengeli.biloko.config.socket;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketIoHealthIndicator implements HealthIndicator {

    private final SocketIOServer server;

    @Override
    public Health health() {
        if (server != null) {
            return Health.up().build();
        }
        return Health.down().build();
    }
}
