package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.infrastructure.model.Currency;
import com.bacos.mokengeli.biloko.infrastructure.model.Dish;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DishMapper {

    public Dish toEntity(DomainDish domainDish) {
        if (domainDish == null) {
            return null;
        }
        return Dish.builder()
                .id(domainDish.getId())
                .name(domainDish.getName())
                .price(domainDish.getPrice())
                .build();
    }

    public DomainDish toDomain(Dish dish) {
        if (dish == null) {
            return null;
        }

        return DomainDish.builder()
                .id(dish.getId())
                .name(dish.getName())
                .price(dish.getPrice())
                .tenantCode(dish.getTenantContext().getTenantCode())
                .categories(getCategories(dish))
                .currency(getCurrency(dish))
                .build();
    }

    private static DomainCurrency getCurrency(Dish dish) {
        Currency currency = dish.getCurrency();
        if (currency == null) {
            return null;
        }
        return DomainCurrency.builder().id(currency.getId())
                .code(currency.getCode()).label(currency.getLabel()).build();
    }

    private static List<String> getCategories(Dish dish) {
        if (dish.getDishCategories() == null) {
            return new ArrayList<>();
        }
        return dish.getDishCategories().stream().map(x -> x.getCategory().getName()).toList();
    }
}
