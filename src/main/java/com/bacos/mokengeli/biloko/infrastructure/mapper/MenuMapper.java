package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.infrastructure.model.Menu;
import lombok.experimental.UtilityClass;

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
                .tenantCode(menu.getTenantContext().getTenantCode())
                .dishes(menu.getMenuDishes().stream().map(x ->
                        DishMapper.toDomain(x.getDish())
                ).toList())
                .build();
    }
}
