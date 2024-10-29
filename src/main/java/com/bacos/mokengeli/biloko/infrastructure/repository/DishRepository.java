package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    Optional<List<Dish>> findByTenantContextTenantCode(String tenantCode);

    @Query("SELECT COUNT(d) = :dishCount FROM Dish d WHERE d.id IN :dishIds AND d.tenantContext.tenantCode = :tenantCode")
    boolean isAllDishesOfTenant(@Param("tenantCode") String tenantCode, @Param("dishIds") List<Long> dishIds, @Param("dishCount") long dishCount);
    @Query("SELECT d.price FROM Dish d WHERE d.id = :id")
    Double findPriceById(Long id);
}
