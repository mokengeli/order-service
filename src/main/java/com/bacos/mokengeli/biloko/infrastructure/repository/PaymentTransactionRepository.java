package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    @Query("SELECT COUNT(p) > 0 FROM PaymentTransaction p WHERE p.id = :paymentId AND p.order.tenantContext.tenantCode = :tenantCode")
    boolean existsByIdAndTenantCode(@Param("paymentId") Long paymentId, @Param("tenantCode") String tenantCode);
}