package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainProduct;
import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainDishProduct;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.DishService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import com.bacos.mokengeli.biloko.presentation.model.CreateDishRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/dish")
public class DishController {

    private final DishService dishService;

    @Autowired
    public DishController(DishService dishService) {
        this.dishService = dishService;
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
    public List<DomainDish> getAllDishes(@RequestParam("code") String tenantCode) {
        try {
            return dishService.getAllDishes(tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
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

}
