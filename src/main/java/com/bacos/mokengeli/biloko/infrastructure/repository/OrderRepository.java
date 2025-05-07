package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import com.bacos.mokengeli.biloko.infrastructure.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o, i FROM Order o JOIN o.items i WHERE o.tenant.code = :tenantCode AND i.state = :orderItemState")
    List<Object[]> findOrderAndItemsByTenantCodeAndItemState(@Param("tenantCode") String tenantCode,
                                                             @Param("orderItemState") OrderItemState orderItemState);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i " +
            "WHERE o.refTable.id = :refTableId " +
            "  AND i.state NOT IN :excludedStates")
    List<Order> findOrdersByRefTableIdAndExcludedStates(
            @Param("refTableId") Long refTableId,
            @Param("excludedStates") Collection<OrderItemState> excludedStates
    );

    @Query("SELECT DISTINCT o FROM Order o " +
            "WHERE o.refTable.id = :refTableId " +
            "  AND o.paymentStatus NOT IN :paymentStatuses")
    List<Order> findOrdersByRefTableAndPaymentStatus(
            @Param("refTableId") Long refTableId,
            @Param("paymentStatuses") Collection<OrderPaymentStatus> paymentStatuses
    );

    @Query("SELECT DISTINCT o FROM Order o " +
            "WHERE o.refTable.id = :refTableId " +
            "  AND o.paymentStatus NOT IN :paymentStatuses")
    boolean isRefTableOccupied(
            @Param("refTableId") Long refTableId,
            @Param("paymentStatuses") Collection<OrderPaymentStatus> paymentStatuses
    );

    default List<Order> findActiveOrdersByRefTableId(Long refTableId) {
        return findOrdersByRefTableAndPaymentStatus(
                refTableId,
                OrderPaymentStatus.getAllPaidStatus()
        );
    }

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.id = :orderId AND o.tenant.code = :tenantCode")
    boolean existsByIdAndTenantCode(@Param("orderId") Long orderId, @Param("tenantCode") String tenantCode);

    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :status AND o.tenant.code = :tenantCode")
    List<Order> findByPaymentStatusAndTenantCode(
            @Param("status") OrderPaymentStatus status,
            @Param("tenantCode") String tenantCode
    );

    @Query("SELECT o FROM Order o WHERE o.paymentStatus IN :statuses AND o.tenant.code = :tenantCode")
    List<Order> findByPaymentStatusInAndTenantCode(
            @Param("statuses") Collection<OrderPaymentStatus> statuses,
            @Param("tenantCode") String tenantCode
    );

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.id = :itemId")
    Optional<Order> findByItemId(@Param("itemId") Long itemId);


    // 1) Méthode plus efficace : existe-t-il une commande non payée ?
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o " +
            "WHERE o.refTable.id = :refTableId " +
            "  AND o.paymentStatus NOT IN :paymentStatuses")
    boolean existsActiveOrderByRefTableId(
            @Param("refTableId") Long refTableId,
            @Param("paymentStatuses") Collection<OrderPaymentStatus> paymentStatuses
    );

    // 2) Default pour savoir si la table est libre
    default boolean isTableFree(Long refTableId) {
        // Retourne true si PAS de commande active
        return !existsActiveOrderByRefTableId(
                refTableId,
                OrderPaymentStatus.getAllPaidStatus()
        );
    }

    @Query("SELECT o FROM Order o "
            + "WHERE o.createdAt BETWEEN :start AND :end "
            + "  AND o.tenant.code = :tenantCode")
    List<Order> findByCreatedAtBetweenAndTenantCode(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("tenantCode") String tenantCode
    );

    @Query("""
            SELECT HOUR(o.createdAt)    AS hour,
                   COUNT(o)              AS orders
              FROM Order o
             WHERE o.createdAt BETWEEN :start AND :end
               AND o.tenant.code = :tenantCode
             GROUP BY HOUR(o.createdAt)
             ORDER BY hour
            """)
    List<HourlyOrderProjection> findOrdersPerHour(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("tenantCode") String tenantCode
    );

    interface HourlyOrderProjection {
        Integer getHour();

        Long getOrders();
    }
}
