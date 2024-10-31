package com.bacos.mokengeli.biloko.presentation.controller;


import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.service.RefTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<DomainRefTable> getRefTables() {
        return this.refTableService.getRefTables();
    }
}
