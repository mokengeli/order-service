package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.domain.DomainMenuCategoryOptions;
import com.bacos.mokengeli.biloko.infrastructure.model.Currency;
import com.bacos.mokengeli.biloko.infrastructure.model.Dish;
import com.bacos.mokengeli.biloko.infrastructure.model.Menu;
import com.bacos.mokengeli.biloko.infrastructure.model.MenuCategoryOptions;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class MenuMapper {

    public Menu toEntity(DomainMenu domainMenu) {
        if (domainMenu == null) {
            return null;
        }

        return Menu.builder()
                .id(domainMenu.getId())
                .name(domainMenu.getName())
                .price(domainMenu.getPrice())
                .build();
    }

    public DomainMenu toDomain(Menu menu) {
        if (menu == null) {
            return null;
        }

        return DomainMenu.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .currency(getCurrency(menu))
                .tenantCode(menu.getTenantContext().getTenantCode())
                .compositions(menu.getMenuCategoryOptions().stream()
                        .map(MenuMapper::toComposition).toList())
                .build();
    }

    public DomainMenu toDomainLigth(Menu menu) {
        if (menu == null) {
            return null;
        }

        return DomainMenu.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .currency(getCurrency(menu))
                .build();
    }

    private static DomainMenuCategoryOptions toComposition(MenuCategoryOptions categoryOptions) {
        return DomainMenuCategoryOptions.builder()
                .category(categoryOptions.getCategory())
                .maxChoice(categoryOptions.getMaxChoices())
                .dishes(categoryOptions.getMenu().getMenuDishes().stream().map(x ->
                        DishMapper.toDomainLigth(x.getDish())).toList()).build();
    }

    private static DomainCurrency getCurrency(Menu menu) {
        Currency currency = menu.getCurrency();
        if (currency == null) {
            return null;
        }
        return DomainCurrency.builder().id(currency.getId())
                .code(currency.getCode()).label(currency.getLabel()).build();
    }
}
