package com.bacos.mokengeli.biloko.infrastructure.adapter;


import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyHourlyStat;
import com.bacos.mokengeli.biloko.application.domain.report.DomainDailyRevenueStat;
import com.bacos.mokengeli.biloko.application.port.ReportingPort;
import com.bacos.mokengeli.biloko.application.utils.DateUtils;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.model.OrderItem;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderItemRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class ReportingAdapter implements ReportingPort {

    private final OrderRepository orderRepository;
    private final ZoneId systemZone = ZoneId.systemDefault();
    private final OrderItemRepository orderItemRepository;

    public ReportingAdapter(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
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

    @Override
    public List<DomainDailyHourlyStat> getDailyHourlyMatrix(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) {
        // bornes UTC
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end   = DateUtils.endOfDay(endDate);

        // récupérer commandes et items
        List<Order>     orders = orderRepository
                .findAllByCreatedAtBetweenAndTenantCode(start, end, tenantCode);
        List<OrderItem> items  = orderItemRepository
                .findAllByOrder_CreatedAtBetweenAndOrder_Tenant_Code(start, end, tenantCode);

        // grouper les commandes par (date,heure)
        Map<LocalDate, Map<Integer, Long>> ordersCount = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().atZoneSameInstant(systemZone).toLocalDate(),
                        Collectors.groupingBy(
                                o -> o.getCreatedAt().atZoneSameInstant(systemZone).getHour(),
                                Collectors.counting()
                        )
                ));

        // grouper les plats par (date,heure)
        Map<LocalDate, Map<Integer, Long>> dishesCount = items.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getOrder().getCreatedAt().atZoneSameInstant(systemZone).toLocalDate(),
                        Collectors.groupingBy(
                                i -> i.getOrder().getCreatedAt().atZoneSameInstant(systemZone).getHour(),
                                Collectors.counting()
                        )
                ));

        // grouper le CA par (date,heure)
        Map<LocalDate, Map<Integer, Double>> revenueSum = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().atZoneSameInstant(systemZone).toLocalDate(),
                        Collectors.groupingBy(
                                o -> o.getCreatedAt().atZoneSameInstant(systemZone).getHour(),
                                Collectors.summingDouble(Order::getTotalPrice)
                        )
                ));

        // génération de la matrice complète
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<DomainDailyHourlyStat> matrix = new ArrayList<>();
        Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(days)
                .forEach(date -> {
                    IntStream.range(0, 24).forEach(hour -> {
                        long ordCount = ordersCount
                                .getOrDefault(date, Collections.emptyMap())
                                .getOrDefault(hour, 0L);
                        long dishCount = dishesCount
                                .getOrDefault(date, Collections.emptyMap())
                                .getOrDefault(hour, 0L);
                        double rev = revenueSum
                                .getOrDefault(date, Collections.emptyMap())
                                .getOrDefault(hour, 0.0);
                        // on prend la devise d'une commande s'il y en a
                        String currency = orders.stream()
                                .filter(o ->
                                        o.getCreatedAt().atZoneSameInstant(systemZone).toLocalDate().equals(date) &&
                                                o.getCreatedAt().atZoneSameInstant(systemZone).getHour() == hour
                                )
                                .findFirst()
                                .map(o -> o.getCurrency().getCode())
                                .orElse("");
                        matrix.add(new DomainDailyHourlyStat(
                                date, hour, ordCount, dishCount, rev, currency
                        ));
                    });
                });

        return matrix;
    }
}
