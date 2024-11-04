package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN o.items i WHERE o.tenantContext.tenantCode = :tenantCode AND i.state = :orderItemState")
    List<Order> findByTenantContextTenantCodeAndOrderItemsState(String tenantCode, OrderItemState orderItemState);
}
