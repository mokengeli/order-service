package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    Page<Dish> findByTenantCode(String tenantCode, Pageable pageable);

    @Query("SELECT COUNT(d) = :dishCount FROM Dish d WHERE d.id IN :dishIds AND d.tenant.code = :tenantCode")
    boolean isAllDishesOfTenant(@Param("tenantCode") String tenantCode, @Param("dishIds") List<Long> dishIds, @Param("dishCount") long dishCount);

    @Query("SELECT d.price FROM Dish d WHERE d.id = :id")
    Double findPriceById(Long id);

    /**
     * Récupère tous les plats pour un tenant donné et une catégorie donnée.
     */
    List<Dish> findByTenantCodeAndDishCategoriesCategoryId(
            String tenantCode, Long categoryId);

    Page<Dish> findByTenantCodeAndNameContainingIgnoreCase(String tenantCode, String search, Pageable pageable);
}
