package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainMenu;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface MenuPort {
    DomainMenu createMenu(DomainMenu menu)  throws ServiceException;;
    Optional<List<DomainMenu>> findAllMenusByTenant(String tenantCode);
}
