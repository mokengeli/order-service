package com.bacos.mokengeli.biloko.config.websocket;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "websocket")
public class WebSocketEndpoint {

    private final SimpUserRegistry userRegistry;

    public WebSocketEndpoint(SimpUserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> data = new HashMap<>();

        // Nombre total de sessions utilisateurs connectées
        data.put("userCount", userRegistry.getUserCount());

        // Pour chaque utilisateur STOMP, on détaille le nombre de souscriptions
        userRegistry.getUsers().forEach(user -> {
            int subs = user.getSessions()
                    .stream()
                    .mapToInt(sess -> sess.getSubscriptions().size())
                    .sum();
            data.put(user.getName(), subs);
        });

        return data;
    }
}

