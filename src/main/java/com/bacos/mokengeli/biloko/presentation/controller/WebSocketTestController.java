package com.bacos.mokengeli.biloko.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class WebSocketTestController {

    @GetMapping("/api/order/ws/test")
    public String testWebSocketConfig() {
        log.info("🧪 WebSocket configuration test endpoint called");
        return "WebSocket configuration is active!";
    }
    
    @GetMapping("/public/ws-status")
    public String wsStatus() {
        return "WebSocket endpoint available at: /api/order/ws";
    }
}
