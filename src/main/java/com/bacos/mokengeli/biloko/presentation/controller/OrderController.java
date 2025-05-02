package com.bacos.mokengeli.biloko.presentation.controller;


import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrderItem;
import com.bacos.mokengeli.biloko.application.domain.model.UpdateOrder;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.OrderService;
import com.bacos.mokengeli.biloko.presentation.CreateOrderRequest;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

            return orderService.createOrder(request.getCurrencyId(), request.getTableId(), createOrderItems);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public DomainOrder getOrderById(@PathVariable Long id) {
        try {
            return orderService.getOrderById(id);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }

    }


    /**
     * @param state state of orderItems to retrieve
     * @return
     */
    @GetMapping("")
    public List<DomainOrder> getOrdersByState(@RequestParam(name = "state") String state) {
        try {
            return orderService.getOrderByState(state);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }

    }

    /**
     * @param id: orderItem to reject
     */
    @PreAuthorize("hasAuthority('REJECT_DISH')")
    @PutMapping("/dish/reject")
    public void rejectDish(@RequestParam("id") Long id) {
        try {
            orderService.changeOrderItemState(id, OrderItemState.REJECTED);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    /**
     * to mark item ready means has been cooked
     *
     * @param id: id of orderItem to cook
     */
    @PreAuthorize("hasAuthority('COOK_DISH')")
    @PutMapping("/dish/ready")
    public void prepareOrderItem(@RequestParam("id") Long id) {
        try {
            orderService.changeOrderItemState(id, OrderItemState.READY);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    /**
     * the dish has been served to the client
     *
     * @param id: orderItem to reject
     */
    @PreAuthorize("hasAuthority('SERVE_DISH')")
    @PutMapping("/dish/served")
    public void servedDish(@RequestParam("id") Long id) {
        try {
            orderService.changeOrderItemState(id, OrderItemState.SERVED);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    /**
     * @param id: orderItem to reject
     */
    @PreAuthorize("hasAuthority('REGISTER_PAY_DISH')")
    @PutMapping("/dish/paid")
    public void paidDish(@RequestParam("id") Long id) {
        try {
            orderService.changeOrderItemState(id, OrderItemState.PAID);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @GetMapping("/active")
    public List<DomainOrder> getActiveOrdersByTable(@RequestParam("tableId") Long tableId) {
        try {
            return this.orderService.getActiveOrdersByTable(tableId);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @PutMapping("/addItems")
    public DomainOrder addItem(@RequestBody UpdateOrder order) {
        try {
            return this.orderService.addItems(order);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }
}
