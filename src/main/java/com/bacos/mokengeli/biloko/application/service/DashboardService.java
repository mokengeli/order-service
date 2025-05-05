package com.bacos.mokengeli.biloko.application.service;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.application.domain.model.DomainBreakdown;
import com.bacos.mokengeli.biloko.application.domain.model.DomainRevenueDashboard;
import com.bacos.mokengeli.biloko.application.domain.model.DomainOrderDashboard;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DashboardPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DashboardPort dashboardPort;
    private final UserAppService userAppService;

    @Autowired
    public DashboardService(DashboardPort dashboardPort, UserAppService userAppService) {
        this.dashboardPort  = dashboardPort;
        this.userAppService = userAppService;
    }

    public DomainRevenueDashboard getRevenue(LocalDate startDate, LocalDate endDate, String tenantCode)
            throws ServiceException {
        // Vérification multi-tenant identique aux autres services :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}
        var user = userAppService.getConnectedUser();
        if (!userAppService.isAdminUser() && !user.getTenantCode().equals(tenantCode)) {
            throw new ServiceException("PERM", "Accès refusé pour ce tenant");
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
                orders.isEmpty() ? null : orders.get(0).getCurrency(),
                orderDtos,
                new DomainBreakdown(fullPayments, discountedPayments)
        );
    }
}

