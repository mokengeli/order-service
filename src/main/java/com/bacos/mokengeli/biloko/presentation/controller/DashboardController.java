package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.dashboard.*;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.DashboardService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/dishes/top")
    public List<DomainTopDish> getTopDishes(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String tenantCode
    ) {
        try {
            return dashboardService.getTopDishes(limit, startDate, endDate, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }

    @GetMapping("/revenue/breakdown-by-category")
    public List<DomainCategoryBreakdown> getBreakdownByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam String tenantCode
    ) {
        try {
            return dashboardService.getBreakdownByCategory(
                    startDate, endDate, tenantCode
            );
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }

    @GetMapping("/dishes/stats")
    public DomainDishStats getDishStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam String tenantCode
    ) {
        try {
            return dashboardService.getDishStats(startDate, endDate, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }

    @GetMapping("/hourly-distribution")
    public List<DomainHourlyOrderStat> getHourlyDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String tenantCode
    ) {
        try {
            return dashboardService.getHourlyDistribution(date, tenantCode);

        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }

    @GetMapping("/dishes/hourly-distribution")
    public List<DomainHourlyDishStat> getHourlyDishStats(
            @RequestParam String tenantCode,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            return dashboardService.getHourlyDishDistribution(date, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }


    @GetMapping("/orders/daily")
    public List<DomainDailyOrderStat> getDailyStats(
            @RequestParam String tenantCode,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        try {
            return dashboardService.getDailyOrderDistribution(start, end, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }

    @GetMapping("/dishes/daily")
    public List<DomainDailyDishStat> getDailyDishStats(
            @RequestParam String tenantCode,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        try {
            return dashboardService.getDailyDishDistribution(start, end, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }

    @GetMapping("/orders/payment-status")
    public List<DomainPaymentStatusStat> getPaymentStatusStats(
            @RequestParam String tenantCode,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        try {
            return dashboardService.getOrderCountByPaymentStatus(start, end, tenantCode);
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }
}
