package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByCode(String tenantCode);

    boolean existsByCode(String tenantCode);

    @Query("SELECT t.name FROM Tenant t WHERE t.code = :code")
    Optional<String> findNameByCode(@Param("code") String code);
}
