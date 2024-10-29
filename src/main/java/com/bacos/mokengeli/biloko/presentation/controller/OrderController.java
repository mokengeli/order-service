package com.bacos.mokengeli.biloko.presentation.controller;


import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrderItem;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.OrderService;
import com.bacos.mokengeli.biloko.presentation.CreateOrderRequest;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public DomainOrder createOrder(@RequestBody CreateOrderRequest request) {
        try {
            List<CreateOrderRequest.CreateOrderItemRequest> orderItems = request.getOrderItems();
            List<CreateOrderItem> createOrderItems = orderItems.stream().map(x -> CreateOrderItem.builder()
                    .count(x.getCount())
                    .note(x.getNote())
                    .dishId(x.getDishId())
                    .build()).toList();
            return orderService.createOrder(request.getCurrencyId(),
                    request.getRefTable(), createOrderItems);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }
}
