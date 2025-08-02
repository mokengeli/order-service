package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.application.domain.DebtValidationStatus;
import com.bacos.mokengeli.biloko.infrastructure.model.DebtValidation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DebtValidationRepository extends JpaRepository<DebtValidation, Long> {

    List<DebtValidation> findByTenantCodeAndStatusOrderByCreatedAtDesc(
            String tenantCode,
            DebtValidationStatus status,
            Pageable pageable
    );

    Optional<DebtValidation> findByIdAndStatus(Long id, DebtValidationStatus status);

    @Query("SELECT t.tenantCode FROM DebtValidation t WHERE t.id = :id")
    Optional<String> findTenantCode(@Param("id") Long debtValidationId);
}