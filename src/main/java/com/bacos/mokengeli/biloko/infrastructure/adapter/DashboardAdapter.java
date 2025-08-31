package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
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
import java.util.*;
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

    @Override
    public List<DomainPaymentStatusStat> getOrderCountByPaymentStatus(
            LocalDate startDate,
            LocalDate endDate,
            String tenantCode
    ) {
        // bornes UTC sur la journée
        OffsetDateTime start = DateUtils.startOfDay(startDate);
        OffsetDateTime end = DateUtils.endOfDay(endDate);

        // requête groupée en base
        List<OrderRepository.PaymentStatusCountProjection> rows =
                orderRepository.findOrderCountByPaymentStatus(
                        start, end, tenantCode
                );

        // mapping vers le DTO domaine
        return rows.stream()
                .map(p -> new DomainPaymentStatusStat(
                        p.getStatus(),
                        p.getCount()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public DomainDailyDishReport getDailyDishReport(LocalDate date, String tenantCode) {
        OffsetDateTime startOfDay = DateUtils.startOfDay(date);
        OffsetDateTime endOfDay = DateUtils.endOfDay(date);

        // 1) Récupérer les commandes terminées (payées) pour la date
        List<OrderPaymentStatus> paidStatuses = OrderPaymentStatus.getAllPaidStatus();
        List<Order> paidOrders = orderRepository.findByCreatedAtBetweenAndTenantCodeAndPaymentStatusIn(
                startOfDay, endOfDay, tenantCode, paidStatuses
        );

        if (paidOrders.isEmpty()) {
            return new DomainDailyDishReport(date, List.of(), 0, 0.0, null);
        }

        // 2) Pour chaque commande, récupérer les items selon la logique métier
        Map<String, DishAggregation> dishAggregations = new HashMap<>();
        int totalDishesCount = 0;
        double totalAmount = 0.0;
        DomainCurrency currency = null;

        for (Order order : paidOrders) {
            List<OrderItem> eligibleItems;

            if (order.getPaymentStatus() == OrderPaymentStatus.FULLY_PAID) {
                // Pour FULLY_PAID: tous les items sauf REJECTED et RETURNED
                eligibleItems = order.getItems().stream()
                        .filter(item -> item.getState() != OrderItemState.REJECTED &&
                                item.getState() != OrderItemState.RETURNED)
                        .collect(Collectors.toList());
            } else {
                // Pour autres statuts payés: uniquement les items explicitement PAID
                eligibleItems = order.getItems().stream()
                        .filter(item -> item.getState() == OrderItemState.PAID)
                        .collect(Collectors.toList());
            }

            // Agréger les données par plat
            for (OrderItem item : eligibleItems) {
                String dishKey = item.getDish().getId().toString();
                
                // Récupérer les catégories depuis Dish -> DishCategories -> Category -> name
                List<String> categoryNames = item.getDish().getDishCategories().stream()
                        .map(dishCategory -> dishCategory.getCategory().getName())
                        .collect(Collectors.toList());
                
                DishAggregation aggregation = dishAggregations.computeIfAbsent(dishKey,
                        k -> new DishAggregation(item.getDish().getId(), item.getDish().getName(), categoryNames));

                // Compter 1 occurrence de ce plat (chaque OrderItem = 1 plat commandé)
                aggregation.addItem(1, item.getUnitPrice());
                totalDishesCount += 1;
                totalAmount += item.getUnitPrice();
            }

            // Récupérer la devise depuis la première commande
            if (currency == null && order.getCurrency() != null) {
                currency = DomainCurrency.builder()
                        .id(order.getCurrency().getId())
                        .code(order.getCurrency().getCode())
                        .label(order.getCurrency().getLabel())
                        .build();
            }
        }

        // 3) Convertir les agrégations en DomainDishSummary
        List<DomainDishSummary> dishSummaries = dishAggregations.values().stream()
                .map(agg -> new DomainDishSummary(
                        agg.getDishId(),
                        agg.getDishName(),
                        agg.getQuantity(),
                        agg.getUnitPrice(),
                        agg.getTotalAmount(),
                        agg.getCategories()
                ))
                .sorted((a, b) -> b.getQuantityServed() - a.getQuantityServed()) // Trier par quantité décroissante
                .collect(Collectors.toList());

        return new DomainDailyDishReport(date, dishSummaries, totalDishesCount, totalAmount, currency);
    }

    // Classe helper pour l'agrégation
    private static class DishAggregation {
        private final Long dishId;
        private final String dishName;
        private final List<String> categories;
        private int quantity = 0;
        private double totalAmount = 0.0;
        private double unitPrice = 0.0; // Prix unitaire (sera défini au premier item)

        public DishAggregation(Long dishId, String dishName, List<String> categories) {
            this.dishId = dishId;
            this.dishName = dishName;
            this.categories = categories != null ? categories : List.of();
        }

        public void addItem(int count, double amount) {
            // Définir le prix unitaire au premier item (on assume que tous les items du même plat ont le même prix)
            if (this.quantity == 0 && count > 0) {
                this.unitPrice = amount / count;
            }
            this.quantity += count;
            this.totalAmount += amount;
        }

        public Long getDishId() {
            return dishId;
        }

        public String getDishName() {
            return dishName;
        }

        public List<String> getCategories() {
            return categories;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }

}
