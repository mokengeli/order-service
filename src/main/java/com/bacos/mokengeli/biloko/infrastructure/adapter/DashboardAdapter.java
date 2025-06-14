package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.dashboard.*;
import com.bacos.mokengeli.biloko.application.port.DashboardPort;
import com.bacos.mokengeli.biloko.application.utils.DateUtils;
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
import java.time.OffsetDateTime;
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
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end = DateUtils.endOfDay(endDate);
        List<Order> orders = orderRepository
                .findByCreatedAtBetweenAndTenantCode(start, end, tenantCode);
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
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end = DateUtils.endOfDay(endDate);
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
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end = DateUtils.endOfDay(endDate);

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

    @Override
    public DomainDishStats getDishStats(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) {
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end = DateUtils.endOfDay(endDate);

        long total = orderItemRepository.countServedItems(
                OrderItemState.SERVED, start, end, tenantCode
        );

        var perCategory = orderItemRepository.findDishesPerCategory(
                        OrderItemState.SERVED, start, end, tenantCode
                ).stream()
                .map(p -> new DomainDishCategoryStat(
                        p.getCategoryName(),
                        p.getValue()
                ))
                .collect(Collectors.toList());

        var perHour = orderItemRepository.findDishesPerHour(
                        OrderItemState.SERVED, start, end, tenantCode
                ).stream()
                .map(p -> new DomainDishHourStat(
                        p.getHour(),
                        p.getValue()
                ))
                .collect(Collectors.toList());

        return new DomainDishStats(total, perCategory, perHour);
    }

    @Override
    public List<DomainHourlyOrderStat> getHourlyOrderDistribution(
            LocalDate date,
            String tenantCode
    ) {
        OffsetDateTime start = DateUtils.startOfDay(date);
        OffsetDateTime end = DateUtils.endOfDay(date);

        return orderRepository.findOrdersPerHour(start, end, tenantCode)
                .stream()
                .map(p -> new DomainHourlyOrderStat(
                        p.getHour(),
                        p.getOrders()
                ))
                .toList();
    }
}
