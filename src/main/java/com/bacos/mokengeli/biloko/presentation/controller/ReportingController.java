package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyHourlyStat;
import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyRevenueStat;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.ReportingService;
import com.bacos.mokengeli.biloko.application.service.TenantService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/order/reports")
public class ReportingController {

    private final ReportingService reportingService;
    private final TenantService tenantService;

    @Autowired
    public ReportingController(ReportingService reportingService, TenantService tenantService) {
        this.reportingService = reportingService;
        this.tenantService = tenantService;
    }

    @GetMapping("/daily-revenue")
    public void exportDailyRevenueCsv(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam("tenantCode") String tenantCode,
            HttpServletResponse response
    ) throws Exception {
        try {
            String tenantName = tenantService.getTenantName(tenantCode);
            List<DomainDailyRevenueStat> stats =
                    reportingService.fetchDailyRevenue(start, end, tenantCode);

            // Configure la réponse HTTP pour un CSV téléchargeable
            response.setContentType("text/csv");
            String filename = String.format("revenus_quotidiens_%s_%s_to_%s.csv", tenantName, start, end);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // Écriture CSV
            try (PrintWriter writer = response.getWriter()) {
                writer.println("Date;OrdersCount;TotalRevenue;AverageTicket;Currency");
                for (var stat : stats) {
                    writer.printf(
                            "%s;%d;%.2f;%.2f;%s%n",
                            stat.getDate(),
                            stat.getOrdersCount(),
                            stat.getTotalRevenue(),
                            stat.getAverageTicket(),
                            stat.getCurrencyCode()
                    );
                }
            }
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }

    /**
     * le fichier a ceci comme header: Date,Hour,OrdersCount,DishesCount,TotalRevenue,Currency
     *
     * @param start
     * @param end
     * @param tenantCode
     * @param response
     * @throws Exception
     */
    @GetMapping("/daily-hourly-matrix")
    public void exportDailyHourlyMatrixCsv(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam("tenantCode") String tenantCode,
            HttpServletResponse response
    ) throws Exception {
        try {
            String tenantName = tenantService.getTenantName(tenantCode);
            List<DomainDailyHourlyStat> matrix =
                    reportingService.fetchDailyHourlyMatrix(start, end, tenantCode);

            response.setContentType("text/csv");
            String filename = String.format("matrice-quotidienne-horaire_%s_%s_to_%s.csv", tenantName, start, end);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (PrintWriter writer = response.getWriter()) {
                writer.println("Date;Hour;OrdersCount;DishesCount;TotalRevenue;Currency");
                for (var row : matrix) {
                    writer.printf(
                            "%s;%02d;%d;%d;%.2f;%s%n",
                            row.getDate(),
                            row.getHour(),
                            row.getOrdersCount(),
                            row.getDishesCount(),
                            row.getTotalRevenue(),
                            row.getCurrencyCode()
                    );
                }
            }
        } catch (ServiceException e) {
            throw new ResponseStatusWrapperException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId()
            );
        }
    }
}
