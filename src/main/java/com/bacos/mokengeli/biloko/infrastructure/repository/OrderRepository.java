package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o, i FROM Order o JOIN o.items i WHERE o.tenantContext.tenantCode = :tenantCode AND i.state = :orderItemState")
    List<Object[]> findOrderAndItemsByTenantCodeAndItemState(@Param("tenantCode") String tenantCode, @Param("orderItemState") OrderItemState orderItemState);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i " +
            "WHERE o.refTable.id = :refTableId " +
            "  AND i.state NOT IN :excludedStates")
    List<Order> findOrdersByRefTableIdAndExcludedStates(
            @Param("refTableId") Long refTableId,
            @Param("excludedStates") Collection<OrderItemState> excludedStates
    );

    default List<Order> findActiveOrdersByRefTableId(Long refTableId) {
        return findOrdersByRefTableIdAndExcludedStates(
                refTableId,
                List.of(OrderItemState.REJECTED, OrderItemState.PAID)
        );
    }

}
