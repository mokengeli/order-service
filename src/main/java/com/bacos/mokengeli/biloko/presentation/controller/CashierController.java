package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainCashierOrderSummary;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.CashierService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/order/cashier")
public class CashierController {

    private final CashierService cashierService;

    @Autowired
    public CashierController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping("")
    public DomainCashierOrderSummary getCashierOrderSummary(
            @RequestParam(name = "code", required = true) String tenantCode,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @RequestParam(name = "searchType", required = false, defaultValue = "ORDER")
            String searchType,

            @RequestParam(name = "search", required = false)
            String search,

            @RequestParam(name = "status", required = false, defaultValue = "ALL")
            String status
    ) {
        try {
            return cashierService.getCashierOrderSummary(tenantCode, date, searchType, search, status);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    @GetMapping("/{tableId}/orders")
    public List<DomainCashierOrderSummary.DomainCashierOrder> getOrdersByTable(
            @PathVariable Long tableId,
            @RequestParam(name = "code", required = true) String tenantCode,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String status
    ) {
        try {
            return cashierService.getOrdersByTable(tenantCode, tableId, date, status);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }
}