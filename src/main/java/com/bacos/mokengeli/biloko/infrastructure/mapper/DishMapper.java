package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.infrastructure.model.Dish;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DishMapper {

    public Dish toEntity(DomainDish domainDish) {
        if (domainDish == null) {
            return null;
        }
        return Dish.builder()
                .id(domainDish.getId())
                .name(domainDish.getName())
                .currentPrice(domainDish.getCurrentPrice())
                .build();
    }

    public DomainDish toDomain(Dish dish) {
        if (dish == null) {
            return null;
        }

        return DomainDish.builder()
                .id(dish.getId())
                .name(dish.getName())
                .currentPrice(dish.getCurrentPrice())
                .tenantCode(dish.getTenantContext().getTenantCode())
                //.articles(dish.getArticles().stream()
                  //      .map(ArticleMapper::toDomain)
                    //    .collect(Collectors.toList()))
                .build();
    }
}
