package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.port.DashboardPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.OrderMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardAdapter implements DashboardPort {

    private final OrderRepository orderRepository;

    @Autowired
    public DashboardAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<DomainOrder> getOrdersBetweenDates(LocalDate startDate, LocalDate endDate, String tenantCode) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.atTime(LocalTime.MAX);
        List<Order> orders = orderRepository
                .findByCreatedAtBetweenAndTenantContextTenantCode(start, end, tenantCode);
        return orders.stream()
                .map(OrderMapper::toDomain)   // mappe en DomainOrder :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}
                .collect(Collectors.toList());
    }
}
