package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderState;
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
                .refTable(order.getRefTable())
                .items(orderItems)
                .state(OrderState.valueOf(order.getState()))
                .currency(DomainCurrency.builder()
                        .code(currency.getCode())
                        .label(currency.getLabel())
                        .id(currency.getId())

                        .build())
                .totalPrice(order.getTotalPrice())
                .build();

    }

    public static DomainOrder.DomainOrderItem toDomainOrderItem(OrderItem OrderItem) {
        return DomainOrder.DomainOrderItem.builder()
                .count(OrderItem.getCount())
                .note(OrderItem.getNote())
                .dishId(OrderItem.getDish().getId())
                .unitPrice(OrderItem.getUnitPrice())
                .build();
    }
}
