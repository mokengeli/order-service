package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.bacos.mokengeli.biloko.application.domain.OrderStatus.*;

@Service
public class OrderService {

  /**  private final OrderPort orderPort;
    private final NotificationService notificationService;
    private final InventoryService inventoryService;

    @Autowired
    public OrderService(OrderPort orderPort, NotificationService notificationService, InventoryService inventoryService) {
        this.orderPort = orderPort;
        this.notificationService = notificationService;
        this.inventoryService = inventoryService;
    }

    public DomainOrder createOrder(CreateOrderRequest request) {
        DomainOrder order = OrderMapper.toDomain(request);
        order.setStatus(OrderStatus.PENDING);
        order = orderPort.saveOrder(order);

        // Notify the kitchen about the new order
        notificationService.sendNotificationToKitchen(order);

        return order;
    }

    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        DomainOrder order = orderPort.findById(orderId)
                .orElseThrow(() -> new ServiceException("Order not found"));

        order.setStatus(newStatus);

        switch (newStatus) {
            case IN_PREPARATION:
                notificationService.notifyWaiter(order);
                break;
            case PREPARED:
                inventoryService.updateInventory(order);
                notificationService.notifyWaiter(order);
                break;
            case SERVED:
                notificationService.notifyWaiter(order);
                break;
            case PAID:
                // Optionally notify accounting or management
                notificationService.notifyAccounting(order);
                break;
            default:
                break;
        }

        orderPort.createNewOrder(order);
    }*/
}
