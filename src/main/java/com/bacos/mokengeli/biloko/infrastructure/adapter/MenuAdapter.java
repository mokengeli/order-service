package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.*;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.MenuPort;
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
    private final TenantRepository tenantRepository;
    private final DishRepository dishRepository;
    private final MenuDishRepository menuDishRepository;
    private final MenuCategoryOptionsRepository menuCategoryOptionsRepository;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public MenuAdapter(MenuRepository menuRepository, TenantRepository tenantRepository, DishRepository dishRepository, MenuDishRepository menuDishRepository, MenuCategoryOptionsRepository menuCategoryOptionsRepository, CurrencyRepository currencyRepository) {
        this.menuRepository = menuRepository;
        this.tenantRepository = tenantRepository;
        this.dishRepository = dishRepository;
        this.menuDishRepository = menuDishRepository;
        this.menuCategoryOptionsRepository = menuCategoryOptionsRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    @Override
    public DomainMenu createMenu(DomainMenu domainMenu) throws ServiceException {
        // Map DomainMenu to Menu entity
        Menu menu = MenuMapper.toEntity(domainMenu);

        // Validate and set Currency
        DomainCurrency domainCurrency = domainMenu.getCurrency();
        if (domainCurrency == null) {
            throw new ServiceException(UUID.randomUUID().toString(), "The currency must be provided");
        }
        Currency currency = this.currencyRepository.findById(domainCurrency.getId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No currency found with id " + domainCurrency.getId()));
        menu.setCurrency(currency);

        // Validate and set Tenant Context
        String tenantCode = domainMenu.getTenantCode();
        Tenant tenant = this.tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant found with tenant_code=" + tenantCode));
        menu.setTenant(tenant);

        // Set creation date
        menu.setCreatedAt(LocalDateTime.now());

        // Save Menu to retrieve generated ID
        menu = this.menuRepository.save(menu);

        // Handle menu dishes and category options
        List<MenuDish> menuDishes = new ArrayList<>();
        List<MenuCategoryOptions> menuCategoryOptions = new ArrayList<>();

        // Process DomainMenuCategoryOptions for dishes and max choices
        for (DomainMenuCategoryOptions domainCategoryOption : domainMenu.getCompositions()) {
            String category = domainCategoryOption.getCategory();
            int maxChoices = domainCategoryOption.getMaxChoice();

            // Create MenuCategoryOptions entity
            MenuCategoryOptions categoryOptions = new MenuCategoryOptions();
            categoryOptions.setMenu(menu);
            categoryOptions.setCategory(category);
            categoryOptions.setMaxChoices(maxChoices);
            menuCategoryOptions.add(categoryOptions);

            // Process each dish in the category
            for (DomainDish domainDish : domainCategoryOption.getDishes()) {
                Dish dish = this.dishRepository.findById(domainDish.getId())
                        .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No dish found with id=" + domainDish.getId()));

                MenuDish menuDish = MenuDish.builder()
                        .dish(dish)
                        .menu(menu)
                        .category(MenuCategory.valueOf(category.toUpperCase()))
                        .build();
                menuDishes.add(menuDish);
            }
        }

        // Save all menu dishes and category options
        menu.setMenuCategoryOptions(menuCategoryOptions);
        menu.setMenuDishes(menuDishes);
        this.menuRepository.save(menu);

        // Return the mapped DomainMenu object
        return MenuMapper.toDomain(menu);
    }


    @Override
    public Optional<List<DomainMenu>> findAllMenusByTenant(String tenantCode) {
        Optional<List<Menu>> optMenus = this.menuRepository.findByTenantCode(tenantCode);
        if (optMenus.isEmpty()) {
            return Optional.empty();
        }
        List<DomainMenu> domainMenus = optMenus.get().stream().map(MenuMapper::toDomainLigth).toList();
        return Optional.of(domainMenus);
    }
}
