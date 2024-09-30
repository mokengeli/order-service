package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dish")
public class DishController {

    private final DishService dishService;

    @Autowired
    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    public DomainDish createDish(@RequestHeader("Tenant-Code") String tenantCode, DomainDish request) {
        DomainDish dish = dishService.createDish(request, tenantCode);
        return dish;
    }

    @GetMapping
    public List<DomainDish> getAllDishes(@RequestHeader("Tenant-Code") String tenantCode) {
        return dishService.getAllDishes(tenantCode);
    }
}
