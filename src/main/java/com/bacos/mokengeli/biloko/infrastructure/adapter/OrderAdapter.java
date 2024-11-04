package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.DomainRefTable;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderAdapter implements OrderPort {
    private final OrderRepository orderRepository;
    private final CurrencyRepository currencyRepository;
    private final TenantContextRepository tenantContextRepository;
    private final DishRepository dishRepository;
    private final RefTableRepository refTableRepository;

    @Autowired
    public OrderAdapter(OrderRepository orderRepository, CurrencyRepository currencyRepository,
                        TenantContextRepository tenantContextRepository, DishRepository dishRepository, RefTableRepository refTableRepository) {
        this.orderRepository = orderRepository;
        this.currencyRepository = currencyRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishRepository = dishRepository;
        this.refTableRepository = refTableRepository;
    }

    @Transactional
    @Override
    public Optional<DomainOrder> createOrder(CreateOrder createOrder) throws ServiceException {
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(createOrder.getTenantCode())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + createOrder.getTenantCode()));
        Currency currency = this.currencyRepository.findById(createOrder.getCurrencyId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No currency found with id " + createOrder.getCurrencyId()));
        RefTable refTable = this.refTableRepository.findByName(createOrder.getRefTable())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No Ref table found with the name " + createOrder.getRefTable()));

        Order order = Order.builder()
                .refTable(refTable)
                .totalPrice(createOrder.getTotalPrice())
                .currency(currency)
                .tenantContext(tenantContext)
                .createdAt(LocalDateTime.now())
                .build();
        createAndSetOrderItems(order, currency, createOrder.getOrderItems());
        order = this.orderRepository.save(order);

        return Optional.of(DomainOrderMapper.toDomain(order));
    }

    @Override
    public Optional<List<DomainOrder>> getOrdersByState(OrderItemState orderItemState, String tenantCode) throws ServiceException {
        boolean existsByTenantCode = this.tenantContextRepository.existsByTenantCode(tenantCode);
        if (!existsByTenantCode) {
            throw new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + tenantCode);
        }


        List<Order> orders = this.orderRepository.findByTenantContextTenantCodeAndOrderItemsState(tenantCode, orderItemState);
        List<DomainOrder> list = orders.stream()
                .map(DomainOrderMapper::toLigthDomain)
                .toList();
        return Optional.of(list);
    }

    @Override
    public boolean isRefTableBelongToTenant(String refTableName, String tenantCode) {
        return this.refTableRepository.existsByNameAndTenantContextTenantCode(refTableName, tenantCode);
    }

    @Override
    public Optional<List<DomainRefTable>> getRefTablesByTenantCode(String tenantCode) {
        Optional<List<RefTable>> optRefTable = this.refTableRepository.findByTenantContextTenantCode(tenantCode);
        if (optRefTable.isEmpty()) {
            return Optional.empty();
        }
        List<RefTable> refTables = optRefTable.get();
        List<DomainRefTable> list = refTables.stream()
                .map(x -> DomainRefTable.builder().name(x.getName()).build())
                .toList();
        return Optional.of(list);
    }

    private void createAndSetOrderItems(Order order, Currency currency, List<CreateOrderItem> orderItems) throws ServiceException {
        for (CreateOrderItem orderItem : orderItems) {
            Optional<Dish> optionalDish = this.dishRepository.findById(orderItem.getDishId());
            if (optionalDish.isEmpty()) {
                throw new ServiceException(UUID.randomUUID().toString(), "No dish found with id " + orderItem.getDishId());
            }
            Dish dish = optionalDish.get();
            for (int i = 0; i < orderItem.getCount(); i++) {
                OrderItem build = OrderItem.builder()
                        .state(OrderItemState.PENDING)
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

}
