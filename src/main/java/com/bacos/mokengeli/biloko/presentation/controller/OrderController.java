package com.bacos.mokengeli.biloko.presentation.controller;


import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

   /** @PostMapping
    public DomainOrder createOrder(@RequestBody CreateOrderRequest request) {
        try {
            DomainOrder createdOrder = orderService.createOrder(request);
            return createdOrder;
        } catch ( ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }*/

  /**  @PostMapping("/{orderId}/status")
    public void updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateOrderStatusRequest request) {
        try {
            orderService.updateOrderStatus(orderId, request.getStatus());
        } catch ( ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }*/
}
