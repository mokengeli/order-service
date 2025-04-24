package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.RefTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefTableRepository extends JpaRepository<RefTable, Long> {
    boolean existsByNameAndTenantContextTenantCode(String name, String code);
    Optional<RefTable> findByName(String refTable);

    Page<RefTable> findByTenantContextTenantCode(String tenantCode, Pageable pageable);
    boolean existsByIdAndTenantContextTenantCode(Long id, String code);
}
