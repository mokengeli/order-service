package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCashierOrderSummary;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CashierPort;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.model.OrderItem;
import com.bacos.mokengeli.biloko.infrastructure.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CashierAdapter implements CashierPort {

    private final OrderRepository orderRepository;

    @Autowired
    public CashierAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public DomainCashierOrderSummary getCashierOrderSummary(
            LocalDate date, 
            String searchType, 
            String search,
            String status, 
            String tenantCode
    ) throws ServiceException {
        try {
            OffsetDateTime startOfDay = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

            List<Order> orders = getOrdersByStatusFilter(startOfDay, endOfDay, tenantCode, status);

            List<DomainCashierOrderSummary.DomainCashierOrder> cashierOrders = orders.stream()
                    .map(this::mapToDomainCashierOrder)
                    .collect(Collectors.toList());

            // Apply search filter if provided
            if (search != null && !search.trim().isEmpty()) {
                cashierOrders = applySearchFilter(cashierOrders, searchType, search);
            }

            // Filter by search type if needed
            if ("TABLE".equals(searchType)) {
                // Group by table and take the latest order per table
                cashierOrders = cashierOrders.stream()
                        .collect(Collectors.groupingBy(
                                order -> order.getTableId() != null ? order.getTableId() : -1L,
                                Collectors.maxBy((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()))
                        ))
                        .values()
                        .stream()
                        .filter(optional -> optional.isPresent())
                        .map(optional -> optional.get())
                        .collect(Collectors.toList());
            }

            int totalOrders = cashierOrders.size();
            double totalRevenue = cashierOrders.stream()
                    .mapToDouble(DomainCashierOrderSummary.DomainCashierOrder::getPaidAmount)
                    .sum();

            return DomainCashierOrderSummary.builder()
                    .date(date.toString())
                    .totalOrders(totalOrders)
                    .totalRevenue(totalRevenue)
                    .orders(cashierOrders)
                    .build();

        } catch (Exception e) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: Error getting cashier order summary: {}", errorId, e.getMessage(), e);
            throw new ServiceException(errorId, "Error retrieving cashier order summary");
        }
    }

    @Override
    public List<DomainCashierOrderSummary.DomainCashierOrder> getOrdersByTable(
            Long tableId,
            LocalDate date,
            String status,
            String tenantCode
    ) throws ServiceException {
        try {
            OffsetDateTime startOfDay = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

            // Get all orders for the specific table
            List<Order> orders = orderRepository.findByCreatedAtBetweenAndTenantCode(startOfDay, endOfDay, tenantCode)
                    .stream()
                    .filter(order -> order.getRefTable() != null && order.getRefTable().getId().equals(tableId))
                    .collect(Collectors.toList());

            // Filter by status
            orders = filterOrdersByStatus(orders, status);

            return orders.stream()
                    .map(this::mapToDomainCashierOrder)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            String errorId = UUID.randomUUID().toString();
            log.error("[{}]: Error getting orders by table {}: {}", errorId, tableId, e.getMessage(), e);
            throw new ServiceException(errorId, "Error retrieving orders by table");
        }
    }

    private List<DomainCashierOrderSummary.DomainCashierOrder> applySearchFilter(
            List<DomainCashierOrderSummary.DomainCashierOrder> orders,
            String searchType,
            String search
    ) {
        if ("TABLE".equals(searchType)) {
            // Search by table name (case insensitive, starts with)
            return orders.stream()
                    .filter(order -> order.getTableName() != null && 
                            order.getTableName().toLowerCase().startsWith(search.trim().toLowerCase()))
                    .collect(Collectors.toList());
        } else if ("ORDER".equals(searchType)) {
            // Search by order number (new robust system)
            return orders.stream()
                    .filter(order -> order.getOrderNumber() != null && 
                            order.getOrderNumber().startsWith(search.trim()))
                    .collect(Collectors.toList());
        }
        // If searchType is not recognized, return all orders
        return orders;
    }

    private List<Order> filterOrdersByStatus(List<Order> orders, String status) {
        switch (status) {
            case "PAID":
                return orders.stream()
                        .filter(order -> OrderPaymentStatus.getAllPaidStatus().contains(order.getPaymentStatus()))
                        .collect(Collectors.toList());
            case "PENDING":
                return orders.stream()
                        .filter(order -> order.getPaymentStatus() == OrderPaymentStatus.UNPAID || 
                                order.getPaymentStatus() == OrderPaymentStatus.PARTIALLY_PAID)
                        .collect(Collectors.toList());
            case "READY":
                return orders.stream()
                        .filter(order -> !OrderPaymentStatus.getAllPaidStatus().contains(order.getPaymentStatus()) &&
                                order.getItems().stream().anyMatch(item -> 
                                        item.getState() == OrderItemState.READY || 
                                        item.getState() == OrderItemState.COOKED || 
                                        item.getState() == OrderItemState.SERVED))
                        .collect(Collectors.toList());
            case "ALL":
            default:
                return orders;
        }
    }

    private List<Order> getOrdersByStatusFilter(
            OffsetDateTime start, 
            OffsetDateTime end, 
            String tenantCode, 
            String status
    ) {
        switch (status) {
            case "PAID":
                return orderRepository.findByCreatedAtBetweenAndTenantCodeAndPaymentStatusIn(
                        start, end, tenantCode, OrderPaymentStatus.getAllPaidStatus()
                );
            case "PENDING":
                return orderRepository.findByCreatedAtBetweenAndTenantCodeAndPaymentStatusIn(
                        start, end, tenantCode, List.of(OrderPaymentStatus.UNPAID, OrderPaymentStatus.PARTIALLY_PAID)
                );
            case "READY":
                // Orders with at least one item ready to be paid (READY, COOKED, or SERVED) and not fully paid
                return orderRepository.findByCreatedAtBetweenAndTenantCode(start, end, tenantCode)
                        .stream()
                        .filter(order -> !OrderPaymentStatus.getAllPaidStatus().contains(order.getPaymentStatus()) &&
                                order.getItems().stream().anyMatch(item -> 
                                        item.getState() == OrderItemState.READY || 
                                        item.getState() == OrderItemState.COOKED || 
                                        item.getState() == OrderItemState.SERVED))
                        .collect(Collectors.toList());
            case "ALL":
            default:
                return orderRepository.findByCreatedAtBetweenAndTenantCode(start, end, tenantCode);
        }
    }

    private DomainCashierOrderSummary.DomainCashierOrder mapToDomainCashierOrder(Order order) {
        DomainCashierOrderSummary.DomainCashierOrder.DomainDishesStatus dishesStatus = 
                calculateDishesStatus(order.getItems());

        String orderStatus = determineOrderStatus(order, dishesStatus);
        int waitingTime = calculateWaitingTime(order);

        return DomainCashierOrderSummary.DomainCashierOrder.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber()) // Ajout du nouveau champ
                .tableId(order.getRefTable() != null ? order.getRefTable().getId() : null)
                .tableName(order.getRefTable() != null ? order.getRefTable().getName() : "N/A")
                .totalAmount(order.getTotalPrice())
                .paidAmount(order.getPaidAmount())
                .remainingAmount(order.getRemainingAmount())
                .status(orderStatus)
                .createdAt(order.getCreatedAt())
                .dishesStatus(dishesStatus)
                .waitingTime(waitingTime)
                .build();
    }

    private DomainCashierOrderSummary.DomainCashierOrder.DomainDishesStatus calculateDishesStatus(
            Collection<OrderItem> items
    ) {
        Map<OrderItemState, Long> statusCounts = items.stream()
                .collect(Collectors.groupingBy(OrderItem::getState, Collectors.counting()));

        int total = items.size();
        int ready = statusCounts.getOrDefault(OrderItemState.READY, 0L).intValue();
        // IN_PREPARATION and PENDING are considered as "in progress"
        int inProgress = statusCounts.getOrDefault(OrderItemState.IN_PREPARATION, 0L).intValue() +
                        statusCounts.getOrDefault(OrderItemState.PENDING, 0L).intValue();
        int served = statusCounts.getOrDefault(OrderItemState.SERVED, 0L).intValue();

        return DomainCashierOrderSummary.DomainCashierOrder.DomainDishesStatus.builder()
                .total(total)
                .ready(ready)
                .inProgress(inProgress)
                .served(served)
                .build();
    }

    private String determineOrderStatus(
            Order order, 
            DomainCashierOrderSummary.DomainCashierOrder.DomainDishesStatus dishesStatus
    ) {
        // Return the specific payment status rather than just "paid"
        if (OrderPaymentStatus.getAllPaidStatus().contains(order.getPaymentStatus())) {
            return mapPaymentStatusToString(order.getPaymentStatus());
        }
        
        if (dishesStatus.getReady() > 0 && dishesStatus.getReady() == dishesStatus.getTotal()) {
            return "ready";
        }
        
        return "pending";
    }

    private String mapPaymentStatusToString(OrderPaymentStatus paymentStatus) {
        switch (paymentStatus) {
            case FULLY_PAID:
                return "paid";
            case PAID_WITH_DISCOUNT:
                return "paid_with_discount";
            case PAID_WITH_REJECTED_ITEM:
                return "paid_with_rejected_item";
            case PAID_WITH_RETURNED_ITEM:
                return "paid_with_returned_item";
            case FORCED_CLOSED:
                return "forced_closed";
            case CLOSED_WITH_DEBT:
                return "closed_with_debt";
            default:
                return "pending";
        }
    }

    private int calculateWaitingTime(Order order) {
        OffsetDateTime now = OffsetDateTime.now();
        return (int) ChronoUnit.MINUTES.between(order.getCreatedAt(), now);
    }
}