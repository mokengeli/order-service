package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.DashboardService;
import com.bacos.mokengeli.biloko.application.domain.model.DomainRevenueDashboard;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/order/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/revenue")
    public DomainRevenueDashboard getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String tenantCode
    ) {
        try {
            return dashboardService.getRevenue(startDate, endDate, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }
}
