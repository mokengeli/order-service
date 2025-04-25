package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.infrastructure.model.Currency;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.model.OrderItem;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class OrderMapper {
    public static DomainOrder toDomain(Order order) {
        Currency currency = order.getCurrency();
        List<OrderItem> items = order.getItems();
        List<DomainOrder.DomainOrderItem> orderItems = items.stream().map(OrderMapper::toDomainOrderItem)
                .toList();
        return DomainOrder
                .builder()
                .id(order.getId())
                .tenantCode(order.getTenantContext().getTenantCode())
                .refTable(order.getRefTable().getName())
                .items(orderItems)
                .currency(DomainCurrency.builder()
                        .code(currency.getCode())
                        .label(currency.getLabel())
                        .id(currency.getId())
                        .build())
                .orderDate(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .build();

    }

    public static DomainOrder toDomainOrderWithoutItem(Order order) {
        Currency currency = order.getCurrency();
        List<OrderItem> items = order.getItems();

        return DomainOrder
                .builder()
                .id(order.getId())
                .tenantCode(order.getTenantContext().getTenantCode())
                .refTable(order.getRefTable().getName())
                .orderDate(order.getCreatedAt())
                .currency(DomainCurrency.builder()
                        .code(currency.getCode())
                        .label(currency.getLabel())
                        .id(currency.getId())
                        .build())
                .totalPrice(order.getTotalPrice())
                .build();

    }

    public static DomainOrder toLigthDomain(Order order) {
        Currency currency = order.getCurrency();
        List<OrderItem> items = order.getItems();
        List<DomainOrder.DomainOrderItem> orderItems = items.stream().map(OrderMapper::toLigthDomainOrderItem)
                .toList();
        return DomainOrder
                .builder()
                .id(order.getId())
                .tenantCode(order.getTenantContext().getTenantCode())
                .refTable(order.getRefTable().getName())
                .items(orderItems)
                .currency(DomainCurrency.builder()
                        .code(currency.getCode())
                        .label(currency.getLabel())
                        .id(currency.getId())

                        .build())
                .totalPrice(order.getTotalPrice())
                .build();

    }

    public static DomainOrder.DomainOrderItem toDomainOrderItem(OrderItem orderItem) {
        return DomainOrder.DomainOrderItem.builder()
                .id(orderItem.getId())
                .state(orderItem.getState())
                .note(orderItem.getNote())
                .dishId(orderItem.getDish().getId())
                .dishName(orderItem.getDish().getName())
                .unitPrice(orderItem.getUnitPrice())
                .orderItemDate(orderItem.getCreatedAt())
                .count(1)
                .build();
    }

    public static DomainOrder.DomainOrderItem toLigthDomainOrderItem(OrderItem orderItem) {
        return DomainOrder.DomainOrderItem.builder()
                .id(orderItem.getId())
                .dishName(orderItem.getDish().getName())
                .state(orderItem.getState())
                .note(orderItem.getNote())
                .unitPrice(orderItem.getUnitPrice())
                .orderItemDate(orderItem.getCreatedAt())
                .build();
    }
}
