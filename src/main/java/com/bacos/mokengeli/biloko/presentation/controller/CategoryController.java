package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.CategoryService;
import com.bacos.mokengeli.biloko.presentation.controller.model.AssignCategoryToTenantRequest;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Page<DomainCategory>> getAllCategories(
            @RequestParam("code") String tenantCode,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            Page<DomainCategory> categories = categoryService.getAllCategories(tenantCode, page, size);
            return ResponseEntity.ok(categories);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DomainCategory>> getAllCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            Page<DomainCategory> categories = categoryService.getAllCategories(page, size);
            return ResponseEntity.ok(categories);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @PostMapping("/assign")
    public void assignCategoryToTenant(@RequestBody AssignCategoryToTenantRequest
                                                                       assignCategoryToTenantRequest) throws ServiceException {
        try {
            categoryService.assignCategoryToTenant(assignCategoryToTenantRequest.getTenantCode(),
                    assignCategoryToTenantRequest.getCategoryId());

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @PostMapping
    public ResponseEntity<DomainCategory> createCategory(@RequestBody DomainCategory category) {
        try {
            DomainCategory createdCategory = categoryService.createCategory(category);
            return ResponseEntity.ok(createdCategory);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }
}
