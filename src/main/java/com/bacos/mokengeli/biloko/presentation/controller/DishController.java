package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.*;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.DishService;
import com.bacos.mokengeli.biloko.application.service.OrderService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import com.bacos.mokengeli.biloko.presentation.model.CreateDishRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/dish")
public class DishController {

    private final DishService dishService;
    private final OrderService orderService;

    @Autowired
    public DishController(DishService dishService, OrderService orderService) {
        this.dishService = dishService;
        this.orderService = orderService;
    }


    @PreAuthorize("hasAuthority('CREATE_DISH')")
    @PostMapping
    public DomainDish createDish(@RequestBody CreateDishRequest request) {
        try {
            DomainDish dish = DomainDish.builder()
                    .name(request.getName())
                    .tenantCode(request.getTenantCode())
                    .price(request.getPrice())
                    .categories(request.getCategories())
                    .currency(DomainCurrency.builder().id(request.getCurrencyId()).build())
                    .dishProducts(request.getDishProducts()
                            .stream().map(x -> DomainDishProduct.builder()
                                    .productId(x.getProductId())
                                    .quantity(x.getQuantity())
                                    .removable(x.getRemovable()).build()).toList())
                    .build();
            return dishService.createDish(dish);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }

    }

    @GetMapping
    public ResponseEntity<Page<DomainDish>> getAllDishes(
            @RequestParam("code") String tenantCode,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search) {
        try {
            Page<DomainDish> dishes = dishService.getAllDishes(tenantCode, page, size, search);
            return ResponseEntity.ok(dishes);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @GetMapping("/{id}")
    public DomainDish getDishById(@PathVariable("id") Long id) {
        try {
            return dishService.getDish(id);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @GetMapping("/category")
    public List<DomainDish> getDishesByCategory(@RequestParam("categoryId") Long id) {
        return dishService.getDishesByCategory(id);
    }

    @GetMapping("/name")
    public List<DomainDish> getDishesByName(@RequestParam("name") String name,
                                            @RequestParam("code") String tenantCode) {
        try {
            return dishService.getDishesByName(name, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    /**
     * @param id: orderItem to reject
     */
    @PreAuthorize("hasAuthority('REJECT_DISH')")
    @PutMapping("/reject")
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
    @PutMapping("/ready")
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
    @PutMapping("/served")
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
    @PutMapping("/paid")
    public void paidDish(@RequestParam("id") Long id) {
        try {
            orderService.changeOrderItemState(id, OrderItemState.PAID);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    /**
     * @param id: orderItem to reject
     */
    @PreAuthorize("hasAuthority('REJECT_DISH')")
    @PutMapping("/return")
    public void returnDish(@RequestParam("id") Long id) {
        try {
            orderService.changeOrderItemState(id, OrderItemState.RETURNED);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }


}
