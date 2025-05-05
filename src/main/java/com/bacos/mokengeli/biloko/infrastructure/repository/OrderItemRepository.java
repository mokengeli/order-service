package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.application.domain.DomainTopDish;
import com.bacos.mokengeli.biloko.application.domain.OrderItemState;
import com.bacos.mokengeli.biloko.infrastructure.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.id = :id AND oi.order.tenantContext.tenantCode = :tenantCode")
    Boolean isOrderItemOfTenantCode(@Param("id") Long id, @Param("tenantCode") String tenantCode);

    @Query("""
              SELECT 
                i.dish.id      AS dishId,
                i.dish.name    AS name,
                COUNT(i)       AS quantity,
                SUM(i.unitPrice) AS revenue
              FROM OrderItem i
              JOIN i.order o
              WHERE o.createdAt BETWEEN :start AND :end
                AND o.tenantContext.tenantCode = :tenantCode
                AND i.state = :servedState
              GROUP BY i.dish.id, i.dish.name
              ORDER BY quantity DESC
            """)
    List<TopDishProjection> findTopDishesServedProjection(
            @Param("servedState") OrderItemState servedState,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("tenantCode") String tenantCode,
            Pageable pageable
    );


    interface TopDishProjection {
        Long getDishId();

        String getName();

        Long getQuantity();

        Double getRevenue();
    }

}
