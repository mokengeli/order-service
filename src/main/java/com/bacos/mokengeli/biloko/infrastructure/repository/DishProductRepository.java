package com.bacos.mokengeli.biloko.infrastructure.repository;

import com.bacos.mokengeli.biloko.infrastructure.model.Dish;
import com.bacos.mokengeli.biloko.infrastructure.model.DishProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DishProductRepository extends JpaRepository<DishProduct, Long> {

    @Query("SELECT dp.quantity FROM DishProduct dp WHERE dp.productId = :productId AND dp.dish.id = :dishId")
    Double getQuantityByProductIdAndDishId(@Param("productId") Long productId,
                                                     @Param("dishId") Long dishId);
}
