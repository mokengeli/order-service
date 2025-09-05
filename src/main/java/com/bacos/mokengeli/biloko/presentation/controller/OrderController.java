package com.bacos.mokengeli.biloko.presentation.controller;


import com.bacos.mokengeli.biloko.application.domain.*;
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

    @PreAuthorize("hasAuthority('CLOSE_ORDER_WITH_DEBT')")
    @PostMapping("/close-with-debt")
    public DomainCloseOrderWithDebt closeWithDebt(
            @RequestBody DomainCloseOrderWithDebtRequest request
    ) {
        try {
            return orderService.closeOrderWithDebt(request);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @GetMapping("debt-validations/pending")
    public List<DomainPendingDebtValidation> getPending(
            @RequestParam("tenantCode") String tenantCode
    ) {
        try {
            return orderService.fetchPendingDebtValidations(tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @PreAuthorize("hasAuthority('ORDER_DEBT_VALIDATION')")
    @PostMapping("/debt-validations/validate")
    public DomainValidateDebtValidation validate(
            @RequestBody DomainValidateDebtValidationRequest req
    ) {
        try {
            return orderService.processDebtValidation(req);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @PutMapping("/force-close")
    public void closeOrder(@RequestParam("id") Long id) {
        try {
          this.orderService.forceCloseOrder(id);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

}
