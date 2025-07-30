package com.bacos.mokengeli.biloko.infrastructure.adapter;


import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyRevenueStat;
import com.bacos.mokengeli.biloko.application.port.ReportingPort;
import com.bacos.mokengeli.biloko.application.utils.DateUtils;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ReportingAdapter implements ReportingPort {

    private final OrderRepository orderRepository;
    private final ZoneId systemZone = ZoneId.systemDefault();

    public ReportingAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<DomainDailyRevenueStat> getDailyRevenueStats(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) {
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end = DateUtils.endOfDay(endDate);

        List<Order> orders = orderRepository
                .findAllByCreatedAtBetweenAndTenantCode(start, end, tenantCode);

        // Regroupe par date locale
        Map<LocalDate, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o ->
                        o.getCreatedAt()
                                .atZoneSameInstant(systemZone)
                                .toLocalDate()
                ));

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(days)
                .map(day -> {
                    List<Order> onDay = grouped.getOrDefault(day, List.of());
                    long count = onDay.size();
                    double total = onDay.stream()
                            .mapToDouble(Order::getTotalPrice)  // méthode de votre entité
                            .sum();
                    String currency = onDay.isEmpty()
                            ? ""
                            : onDay.get(0).getCurrency().getCode();
                    return new DomainDailyRevenueStat(day, count, total, currency);
                })
                .collect(Collectors.toList());
    }
}
