package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.dashboard.*;
import com.bacos.mokengeli.biloko.application.port.DashboardPort;
import com.bacos.mokengeli.biloko.application.utils.DateUtils;
import com.bacos.mokengeli.biloko.infrastructure.mapper.OrderMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.model.OrderItem;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderItemRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class DashboardAdapter implements DashboardPort {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ZoneId systemZone = ZoneId.systemDefault();

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
        List<OrderItemState> orderItemStates = Arrays.asList(OrderItemState.SERVED, OrderItemState.PAID);

        // Pageable pour limiter au "limit" le nombre de résultats
        return orderItemRepository.findTopDishesServedProjection(
                        orderItemStates, start, end, tenantCode, PageRequest.of(0, limit)
                ).stream()
                .map(p -> new DomainTopDish(
                        p.getDishId(),
                        p.getName(),
                        p.getQuantity(),
                        new DomainCurrency(p.getCurrencyId(), p.getCurrencyLabel(), p.getCurrencyCode()),
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
        List<OrderItemState> orderItemStates = Arrays.asList(OrderItemState.SERVED, OrderItemState.PAID);

        return orderItemRepository.findBreakdownByCategory(
                        start, end, tenantCode, orderItemStates
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
        List<OrderItemState> orderItemStates = Arrays.asList(OrderItemState.SERVED, OrderItemState.PAID);
        long total = orderItemRepository.countServedItems(
                orderItemStates, start, end, tenantCode
        );

        var perCategory = orderItemRepository.findDishesPerCategory(
                        orderItemStates, start, end, tenantCode
                ).stream()
                .map(p -> new DomainDishCategoryStat(
                        p.getCategoryName(),
                        p.getValue()
                ))
                .collect(Collectors.toList());

        var perHour = orderItemRepository.findDishesPerHour(
                        orderItemStates, start, end, tenantCode
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

        List<Order> orders = orderRepository
                .findAllByCreatedAtBetweenAndTenantCode(start, end, tenantCode);

        // 4. Group & count par heure locale
        Map<Integer, Long> countsByHour = orders.stream()
                .map(o ->
                        // convertit en ZonedDateTime dans le tz utilisateur
                        o.getCreatedAt()
                                .atZoneSameInstant(systemZone)
                                .getHour()
                )
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        // 5. Construis la liste finale (0h→23h), avec remplissage à 0
        return IntStream.range(0, 24)
                .mapToObj(hour -> new DomainHourlyOrderStat(
                        hour,
                        countsByHour.getOrDefault(hour, 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainHourlyDishStat> getHourlyDishDistribution(
            LocalDate date,
            String tenantCode
    ) {
        // ➊ bornes de la journée dans le fuseau système
        OffsetDateTime start = DateUtils.startOfDay(date);
        OffsetDateTime end = DateUtils.endOfDay(date);

        // ➋ récupère tous les OrderItem concernés
        List<OrderItem> items = orderItemRepository
                .findAllByOrder_CreatedAtBetweenAndOrder_Tenant_Code(
                        start, end, tenantCode
                );

        // ➌ group & count par heure locale de l'Order
        Map<Integer, Long> countsByHour = items.stream()
                .map(item ->
                        item.getOrder()
                                .getCreatedAt()
                                .atZoneSameInstant(systemZone)
                                .getHour()
                )
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        // ➍ génère la liste 0h→23h avec remplissage à 0
        return IntStream.range(0, 24)
                .mapToObj(hour -> new DomainHourlyDishStat(
                        hour,
                        countsByHour.getOrDefault(hour, 0L)
                ))
                .collect(Collectors.toList());
    }


    @Override
    public List<DomainDailyOrderStat> getDailyOrderDistribution(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) {
        // bornes UTC pour la période [startDate, endDate]
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end = DateUtils.endOfDay(endDate);

        // récupération brute
        List<Order> orders = orderRepository
                .findAllByCreatedAtBetweenAndTenantCode(
                        start, end, tenantCode);

        // grouping par date locale
        Map<LocalDate, Long> countsByDay = orders.stream()
                .map(o -> o.getCreatedAt()
                        .atZoneSameInstant(systemZone)
                        .toLocalDate()
                )
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        // construire la liste complète jour par jour
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(days)
                .map(day -> new DomainDailyOrderStat(
                        day,
                        countsByDay.getOrDefault(day, 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainDailyDishStat> getDailyDishDistribution(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) {
        // Bornes en UTC comme précédemment
        OffsetDateTime startUtc = DateUtils.startOfDay(startDate);
        OffsetDateTime endUtc = DateUtils.endOfDay(endDate);


        // 1) Récupérer tous les items de commande
        List<OrderItem> items = orderItemRepository
                .findAllByOrder_CreatedAtBetweenAndOrder_Tenant_Code(
                        startUtc, endUtc, tenantCode
                );

        // 2) Grouper par date locale et sommer les quantités
        Map<LocalDate, Long> sumByDay = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getOrder().getCreatedAt()
                                .atZoneSameInstant(systemZone)
                                .toLocalDate(),
                        Collectors.counting()
                ));

        // 3) Générer la liste complète de jours
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(days)
                .map(day -> new DomainDailyDishStat(
                        day,
                        sumByDay.getOrDefault(day, 0L)
                ))
                .collect(Collectors.toList());
    }


}
