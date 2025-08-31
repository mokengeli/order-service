package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.application.domain.dashboard.*;
import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DashboardPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardService {

    private final DashboardPort dashboardPort;
    private final UserAppService userAppService;

    @Autowired
    public DashboardService(DashboardPort dashboardPort, UserAppService userAppService) {
        this.dashboardPort = dashboardPort;
        this.userAppService = userAppService;
    }

    public DomainRevenueDashboard getRevenue(LocalDate startDate, LocalDate endDate, String tenantCode)
            throws ServiceException {

        // Vérification multi-tenant identique aux autres services
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() && !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get revenue of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }

        List<DomainOrder> orders = dashboardPort.getOrdersBetweenDates(startDate, endDate, tenantCode);

        // 1) CA réel = somme des paidAmount pour les statuts "finalisés"
        List<OrderPaymentStatus> paidStatuses = OrderPaymentStatus.getAllPaidStatus();
        double realRevenue = orders.stream()
                .filter(o -> paidStatuses.contains(o.getPaymentStatus()))
                .mapToDouble(DomainOrder::getPaidAmount)
                .sum(); // getPaidAmount() depuis DomainOrder
        // 2) CA théorique = somme des totalPrice de toutes les commandes
        double theoreticalRevenue = orders.stream()
                .mapToDouble(DomainOrder::getTotalPrice)
                .sum(); // getTotalPrice() depuis DomainOrder

        // 3) Breakdown
        double fullPayments = orders.stream()
                .filter(o -> o.getPaymentStatus() == OrderPaymentStatus.FULLY_PAID)
                .mapToDouble(DomainOrder::getPaidAmount)
                .sum();

        double discountedPayments = orders.stream()
                .filter(o -> o.getPaymentStatus() == OrderPaymentStatus.PAID_WITH_DISCOUNT
                        || o.getPaymentStatus() == OrderPaymentStatus.PAID_WITH_REJECTED_ITEM)
                .mapToDouble(DomainOrder::getPaidAmount)
                .sum();

        // 4) Map en DTO pour le front
        List<DomainOrderDashboard> orderDtos = orders.stream()
                .map(o -> new DomainOrderDashboard(
                        o.getId(),
                        o.getOrderDate().toLocalDate(),
                        o.getTotalPrice(),
                        o.getPaidAmount(),
                        o.getPaymentStatus()
                ))
                .collect(Collectors.toList());

        return new DomainRevenueDashboard(
                realRevenue,
                theoreticalRevenue,
                orders.isEmpty() ? null : orders.getFirst().getCurrency(),
                orderDtos,
                new DomainBreakdown(fullPayments, discountedPayments)
        );
    }

    public List<DomainTopDish> getTopDishes(
            int limit,
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) throws ServiceException {
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() && !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get top dishes of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getTopDishesServed(startDate, endDate, tenantCode, limit);
    }

    public List<DomainCategoryBreakdown> getBreakdownByCategory(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) throws ServiceException {
        // Contrôle multi-tenant
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the breakdown by category of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getBreakdownByCategory(
                startDate, endDate, tenantCode
        );
    }

    public DomainDishStats getDishStats(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) throws ServiceException {
        // contrôle multi-tenant
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the dish stats of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getDishStats(startDate, endDate, tenantCode);
    }

    public List<DomainHourlyOrderStat> getHourlyDistribution(
            LocalDate date,
            String tenantCode
    ) throws ServiceException {
        // contrôle multi-tenant identique aux autres endpoints
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the getHourlyDistribution of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getHourlyOrderDistribution(date, tenantCode);
    }

    public List<DomainHourlyDishStat> getHourlyDishDistribution(
            LocalDate date,
            String tenantCode
    ) throws ServiceException {
        // contrôle multi-tenant identique aux autres endpoints
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the getHourlyDistribution of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getHourlyDishDistribution(date, tenantCode);
    }

    public List<DomainDailyOrderStat> getDailyOrderDistribution(
            LocalDate start,
            LocalDate end,
            String tenantCode
    ) throws ServiceException {
        // contrôle multi-tenant identique aux autres endpoints
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the getDailyOrderDistribution of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getDailyOrderDistribution(start, end, tenantCode);
    }

    public List<DomainDailyDishStat> getDailyDishDistribution(
            LocalDate start,
            LocalDate end,
            String tenantCode
    ) throws ServiceException {
        {
            // contrôle multi-tenant identique aux autres endpoints
            ConnectedUser connectedUser = userAppService.getConnectedUser();
            if (!userAppService.isAdminUser() &&
                    !connectedUser.getTenantCode().equals(tenantCode)) {
                String uuid = UUID.randomUUID().toString();
                log.error("[{}]: User [{}] Tenant [{}] try to get the getDailyDishDistribution of another tenant: {}", uuid,
                        connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
                throw new ServiceException(uuid, "Accès refusé pour ce tenant");
            }
            return dashboardPort.getDailyDishDistribution(start, end, tenantCode);
        }
    }

    public List<DomainPaymentStatusStat> getOrderCountByPaymentStatus(
            LocalDate start,
            LocalDate end,
            String tenantCode
    ) throws ServiceException {
        // contrôle multi-tenant identique aux autres endpoints
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the getOrderCountByPaymentStatus of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getOrderCountByPaymentStatus(start, end, tenantCode);
    }

    public DomainDailyDishReport getDailyDishReport(
            LocalDate date,
            String tenantCode
    ) throws ServiceException {
        ConnectedUser connectedUser = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() &&
                !connectedUser.getTenantCode().equals(tenantCode)) {
            String uuid = UUID.randomUUID().toString();
            log.error("[{}]: User [{}] Tenant [{}] try to get the getDailyDishReport of another tenant: {}", uuid,
                    connectedUser.getEmployeeNumber(), connectedUser.getTenantCode(), tenantCode);
            throw new ServiceException(uuid, "Accès refusé pour ce tenant");
        }
        return dashboardPort.getDailyDishReport(date, tenantCode);
    }
}

