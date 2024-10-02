package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.port.MenuPort;
import com.bacos.mokengeli.biloko.infrastructure.model.Menu;
import com.bacos.mokengeli.biloko.infrastructure.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuAdapter implements MenuPort {

    private final MenuRepository menuRepository;

    @Autowired
    public MenuAdapter(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public DomainMenu saveMenu(DomainMenu menu) {
        // menuRepository.save(menu);
         return null;
    }

    @Override
    public List<DomainMenu> findAllMenusByTenant(String tenantCode) {
       // List<Menu> allByTenantCode = menuRepository.findAllByTenantCode(tenantCode);// Fetch by tenant code
        return null;
    }
}
