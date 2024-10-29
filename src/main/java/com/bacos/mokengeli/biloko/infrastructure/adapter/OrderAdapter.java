package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.OrderState;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrder;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrderItem;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.OrderPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DomainOrderMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderAdapter implements OrderPort {
    private final OrderRepository orderRepository;
    private final CurrencyRepository currencyRepository;
    private final TenantContextRepository tenantContextRepository;
    private final DishRepository dishRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderAdapter(OrderRepository orderRepository, CurrencyRepository currencyRepository, TenantContextRepository tenantContextRepository, DishRepository dishRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.currencyRepository = currencyRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishRepository = dishRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    @Override
    public Optional<DomainOrder> createOrder(CreateOrder createOrder) throws ServiceException {
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(createOrder.getTenantCode())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + createOrder.getTenantCode()));
        Currency currency = this.currencyRepository.findById(createOrder.getCurrencyId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No currency found with id " + createOrder.getCurrencyId()));

        Order order = Order.builder()
                .state(OrderState.PENDING.name())
                .refTable(createOrder.getRefTable())
                .totalPrice(createOrder.getTotalPrice())
                .currency(currency)
                .tenantContext(tenantContext)
                .createdAt(LocalDateTime.now())
                .build();

        createAndSetOrderItems(order, currency, createOrder.getOrderItems());

        order = this.orderRepository.save(order);

        return Optional.of(DomainOrderMapper.toDomain(order));
    }

    private void createAndSetOrderItems(Order order, Currency currency, List<CreateOrderItem> orderItems) throws ServiceException {
        for (CreateOrderItem orderItem : orderItems) {
            Optional<Dish> optionalDish = this.dishRepository.findById(orderItem.getDishId());
            if (optionalDish.isEmpty()) {
                throw new ServiceException(UUID.randomUUID().toString(), "No dish found with id " + orderItem.getDishId());
            }
            Dish dish = optionalDish.get();
            OrderItem build = OrderItem.builder()
                    .count(orderItem.getCount())
                    .note(orderItem.getNote() == null ? "" : orderItem.getNote())
                    .currency(currency)
                    .createdAt(LocalDateTime.now())
                    .dish(dish)
                    .order(order)
                    .unitPrice(dish.getPrice())
                    .build();
            order.addItem(build);
        }

    }

}
