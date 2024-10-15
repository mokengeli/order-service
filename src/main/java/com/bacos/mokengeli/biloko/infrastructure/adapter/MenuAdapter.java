package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.MenuPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DishMapper;
import com.bacos.mokengeli.biloko.infrastructure.mapper.MenuMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.*;
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
    private final CurrencyRepository currencyRepository;

    @Autowired
    public MenuAdapter(MenuRepository menuRepository, TenantContextRepository tenantContextRepository, DishRepository dishRepository, MenuDishRepository menuDishRepository, CurrencyRepository currencyRepository) {
        this.menuRepository = menuRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishRepository = dishRepository;
        this.menuDishRepository = menuDishRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    @Override
    public DomainMenu createMenu(DomainMenu domainMenu) throws ServiceException {
        Menu menu = MenuMapper.toEntity(domainMenu);

        DomainCurrency domainCurrency = domainMenu.getCurrency();
        if (domainCurrency == null) {
            throw new ServiceException(UUID.randomUUID().toString(), "The currency must be provided");
        }
        Currency currency = this.currencyRepository.findById(domainCurrency.getId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No currency found with id " + domainCurrency.getId()));


        String tenantCode = domainMenu.getTenantCode();
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + tenantCode));
        menu.setTenantContext(tenantContext);
        menu.setCurrency(currency);
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
