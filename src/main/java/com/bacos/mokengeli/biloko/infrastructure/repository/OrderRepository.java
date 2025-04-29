package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
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

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.id = :orderId AND o.tenantContext.tenantCode = :tenantCode")
    boolean existsByIdAndTenantCode(@Param("orderId") Long orderId, @Param("tenantCode") String tenantCode);

    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :status AND o.tenantContext.tenantCode = :tenantCode")
    List<Order> findByPaymentStatusAndTenantCode(
            @Param("status") OrderPaymentStatus status,
            @Param("tenantCode") String tenantCode
    );

    @Query("SELECT o FROM Order o WHERE o.paymentStatus IN :statuses AND o.tenantContext.tenantCode = :tenantCode")
    List<Order> findByPaymentStatusInAndTenantCode(
            @Param("statuses") Collection<OrderPaymentStatus> statuses,
            @Param("tenantCode") String tenantCode
    );

}
