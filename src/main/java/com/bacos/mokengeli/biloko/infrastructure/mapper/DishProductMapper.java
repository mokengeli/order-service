package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainDishProduct;
import com.bacos.mokengeli.biloko.infrastructure.model.DishProduct;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DishProductMapper {


    public DomainDishProduct toDomain(DishProduct dishProduct) {
        if (dishProduct == null) {
            return null;
        }

        return DomainDishProduct.builder()
                .dish(DishMapper.toDomain(dishProduct.getDish()))
                //.product(ProductMapper.toDomain(dishProduct.getProduct()))
                .quantity(dishProduct.getQuantity())
                .build();
    }
}
