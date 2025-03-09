package com.bacos.mokengeli.biloko.presentation.controller;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.domain.DomainMenuCategoryOptions;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.service.MenuService;
import com.bacos.mokengeli.biloko.presentation.exception.ResponseStatusWrapperException;
import com.bacos.mokengeli.biloko.presentation.model.CreateMenuRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/menu")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public DomainMenu createMenu(@RequestBody CreateMenuRequest request) {
        try {
            DomainMenu menu = DomainMenu.builder()
                    .price(request.getPrice())
                    .tenantCode(request.getTenantCode())
                    .name(request.getName())
                    .currency(DomainCurrency.builder().id(request.getCurrencyId()).build())
                    .compositions(createMenuComposition(request.getCompositions()))
                    .build();

            return menuService.createMenu(menu);
        } catch (
                ServiceException e) {
            throw new ResponseStatusWrapperException(HttpStatus.BAD_REQUEST, e.getMessage(), e.getTechnicalId());
        }
    }

    private List<DomainMenuCategoryOptions> createMenuComposition(List<CreateMenuRequest.CompositionMenuRequest> compositions) {
        return compositions.stream()
                .map(x -> DomainMenuCategoryOptions.builder().category(x.getCategory()).maxChoice(x.getMaxChoice())
                        .dishes(x.getDishIds().stream()
                                .map(c -> DomainDish.builder().id(c).build()
                                )
                                .toList()).build()).toList();
    }

    @GetMapping
    public List<DomainMenu> getAllMenus() {
        return menuService.getAllMenus();
    }
}
