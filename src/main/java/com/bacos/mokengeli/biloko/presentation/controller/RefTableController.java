package com.bacos.mokengeli.biloko.presentation.controller;


import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.RefTableService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/table")
public class RefTableController {

    private final RefTableService refTableService;

    @Autowired
    public RefTableController(RefTableService refTableService) {
        this.refTableService = refTableService;
    }

    @GetMapping("")
    public Page<DomainRefTable> getRefTablesByTenantCode(@RequestParam("code") String tenantCode,
                                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            return this.refTableService.getRefTablesByTenantCode(tenantCode, page, size);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @PostMapping
    public ResponseEntity<DomainRefTable> createRefTable(@RequestBody DomainRefTable refTable) {
        try {
            DomainRefTable createdRefTable = refTableService.createRefTable(refTable);
            return ResponseEntity.ok(createdRefTable);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }
}
