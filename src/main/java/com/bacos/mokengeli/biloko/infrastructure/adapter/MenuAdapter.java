package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.MenuPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DishMapper;
import com.bacos.mokengeli.biloko.infrastructure.mapper.MenuMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.DishRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.MenuDishRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.MenuRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantContextRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MenuAdapter implements MenuPort {

    private final MenuRepository menuRepository;
    private final TenantContextRepository tenantContextRepository;
    private final DishRepository dishRepository;
    private final MenuDishRepository menuDishRepository;

    @Autowired
    public MenuAdapter(MenuRepository menuRepository, TenantContextRepository tenantContextRepository, DishRepository dishRepository, MenuDishRepository menuDishRepository) {
        this.menuRepository = menuRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishRepository = dishRepository;
        this.menuDishRepository = menuDishRepository;
    }

    @Transactional
    @Override
    public DomainMenu createMenu(DomainMenu domainMenu) throws ServiceException {
        Menu menu = MenuMapper.toEntity(domainMenu);
        String tenantCode = domainMenu.getTenantCode();
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + tenantCode));
        menu.setTenantContext(tenantContext);
        menu.setCreatedAt(LocalDateTime.now());
        menu = this.menuRepository.save(menu);
        List<MenuDish> menuDishes = new ArrayList<>();
        for (DomainDish domainDish : domainMenu.getDishes()) {
            Dish dish = this.dishRepository.findById(domainDish.getId())
                    .orElseThrow(
                            () -> new ServiceException(UUID.randomUUID().toString(), "No dish found with id=" + domainDish.getId())
                    );
            MenuDish menuDish = MenuDish.builder()
                    .dish(dish)
                    .menu(menu)
                    .build();
            menuDishes.add(menuDish);
        }
        menuDishes = this.menuDishRepository.saveAll(menuDishes);
        menu.setMenuDishes(menuDishes);
        return MenuMapper.toDomain(menu);
    }

    @Override
    public Optional<List<DomainMenu>> findAllMenusByTenant(String tenantCode) {
        Optional<List<Menu>> optMenus = this.menuRepository.findByTenantContextTenantCode(tenantCode);
        if (optMenus.isEmpty()) {
            return Optional.empty();
        }
        List<DomainMenu> domainMenus = optMenus.get().stream().map(MenuMapper::toDomain).toList();
        return Optional.of(domainMenus);
    }
}
