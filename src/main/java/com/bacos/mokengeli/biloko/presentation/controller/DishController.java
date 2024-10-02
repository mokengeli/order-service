package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainArticle;
import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainDishArticle;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.DishService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import com.bacos.mokengeli.biloko.presentation.model.CreateDishRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public DomainDish createDish(@RequestBody CreateDishRequest request) {
        try {
            DomainDish dish = DomainDish.builder()
                    .name(request.getName())
                    .tenantCode(request.getTenantCode())
                    .currentPrice(request.getPrice())
                    .dishArticles(request.getDishArticles()
                            .stream().map(x -> DomainDishArticle.builder()
                                    .article(DomainArticle.builder().id(x.getArticleId()).build())
                                    .quantity(x.getQuantity())
                                    .removable(x.getRemovable()).build()).toList())
                    .build();
            return dishService.createDish(dish);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }

    }

    @GetMapping
    public List<DomainDish> getAllDishes(String tenantCode) {
        try {
            return dishService.getAllDishes(tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }
}
