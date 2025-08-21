package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.model.OrderNotification;
import com.bacos.mokengeli.biloko.application.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order/socketio")
@RequiredArgsConstructor
public class SocketIoTestController {

    private final OrderNotificationService notificationService;

    @Value("${socketio.port}")
    private int socketPort;

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> map = new HashMap<>();
        map.put("socketPort", socketPort);
        map.put("status", "running");
        return map;
    }

    @PostMapping("/test-notification")
    public Map<String, Object> sendTest(@RequestParam String tenantCode) {
        OrderNotification notification = OrderNotification.builder()
                .orderId(1L)
                .tableId(1L)
                .tenantCode(tenantCode)
                .orderStatus(OrderNotification.OrderNotificationStatus.NEW_ORDER)
                .previousState("PENDING")
                .newState("CONFIRMED")
                .tableState("OCCUPIED")
                .timestamp(LocalDateTime.now())
                .build();
        notificationService.notifyStateChange(1L,1L,OrderNotification.OrderNotificationStatus.NEW_ORDER,"PENDING","CONFIRMED","OCCUPIED");
        Map<String, Object> resp = new HashMap<>();
        resp.put("sent", true);
        resp.put("tenant", tenantCode);
        return resp;
    }
}
