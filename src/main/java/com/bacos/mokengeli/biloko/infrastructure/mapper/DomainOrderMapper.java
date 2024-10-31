package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.infrastructure.model.Currency;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import com.bacos.mokengeli.biloko.infrastructure.model.OrderItem;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class DomainOrderMapper {
    public static DomainOrder toDomain(Order order) {
        Currency currency = order.getCurrency();
        List<OrderItem> items = order.getItems();
        List<DomainOrder.DomainOrderItem> orderItems = items.stream().map(DomainOrderMapper::toDomainOrderItem)
                .toList();
        return DomainOrder
                .builder()
                .id(order.getId())
                .tenantCode(order.getTenantContext().getTenantCode())
                .refTable(order.getRefTable().getName())
                .items(orderItems)
                .state(order.getState())
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
        List<DomainOrder.DomainOrderItem> orderItems = items.stream().map(DomainOrderMapper::toLigthDomainOrderItem)
                .toList();
        return DomainOrder
                .builder()
                .id(order.getId())
                .tenantCode(order.getTenantContext().getTenantCode())
                .refTable(order.getRefTable().getName())
                .state(order.getState())
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
                .count(orderItem.getCount())
                .note(orderItem.getNote())
                .dishId(orderItem.getDish().getId())
                .dishName(orderItem.getDish().getName())
                .unitPrice(orderItem.getUnitPrice())
                .build();
    }

    public static DomainOrder.DomainOrderItem toLigthDomainOrderItem(OrderItem orderItem) {
        return DomainOrder.DomainOrderItem.builder()
                .id(orderItem.getId())
                .dishName(orderItem.getDish().getName())
                .count(orderItem.getCount())
                .build();
    }
}
