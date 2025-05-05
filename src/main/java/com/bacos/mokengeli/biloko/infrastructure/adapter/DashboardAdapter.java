package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.dashboard.DomainCategoryBreakdown;
import com.bacos.mokengeli.biloko.application.domain.dashboard.DomainTopDish;
import com.bacos.mokengeli.biloko.application.port.DashboardPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.OrderMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderItemRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardAdapter implements DashboardPort {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public DashboardAdapter(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<DomainOrder> getOrdersBetweenDates(LocalDate startDate, LocalDate endDate, String tenantCode) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<Order> orders = orderRepository
                .findByCreatedAtBetweenAndTenantContextTenantCode(start, end, tenantCode);
        return orders.stream()
                .map(OrderMapper::toDomain)   // mappe en DomainOrder :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainTopDish> getTopDishesServed(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode,
            int limit
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        // Pageable pour limiter au "limit" le nombre de résultats
        return orderItemRepository.findTopDishesServedProjection(
                        OrderItemState.SERVED, start, end, tenantCode, PageRequest.of(0, limit)
                ).stream()
                .map(p -> new DomainTopDish(
                        p.getDishId(),
                        p.getName(),
                        p.getQuantity(),
                        p.getRevenue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainCategoryBreakdown> getBreakdownByCategory(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return orderItemRepository.findBreakdownByCategory(
                        start, end, tenantCode, OrderItemState.SERVED
                ).stream()
                .map(p -> new DomainCategoryBreakdown(
                        p.getCategoryName(),
                        p.getValue(),
                        p.getRevenue()
                ))
                .toList();
    }
}
