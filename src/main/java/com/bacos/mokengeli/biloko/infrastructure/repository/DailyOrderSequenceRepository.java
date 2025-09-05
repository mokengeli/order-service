package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.DailyOrderSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyOrderSequenceRepository extends JpaRepository<DailyOrderSequence, String> {

    /**
     * Trouve la séquence pour un tenant et une date métier donnés
     * Utilise un verrou pessimiste pour éviter les conflits lors de l'incrémentation
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyOrderSequence d WHERE d.tenantCode = :tenantCode AND d.businessDate = :businessDate")
    Optional<DailyOrderSequence> findByTenantCodeAndBusinessDateWithLock(
            @Param("tenantCode") String tenantCode,
            @Param("businessDate") LocalDate businessDate
    );

    /**
     * Trouve la séquence sans verrou (pour lecture seule)
     */
    Optional<DailyOrderSequence> findByTenantCodeAndBusinessDate(String tenantCode, LocalDate businessDate);

    /**
     * Vérifie l'existence d'une séquence pour un tenant et une date
     */
    boolean existsByTenantCodeAndBusinessDate(String tenantCode, LocalDate businessDate);

    /**
     * Supprime les anciennes séquences (pour nettoyage)
     */
    void deleteByBusinessDateBefore(LocalDate cutoffDate);

    /**
     * Trouve toutes les séquences pour un tenant
     */
    @Query("SELECT d FROM DailyOrderSequence d WHERE d.tenantCode = :tenantCode ORDER BY d.businessDate DESC")
    java.util.List<DailyOrderSequence> findByTenantCodeOrderByBusinessDateDesc(@Param("tenantCode") String tenantCode);
}