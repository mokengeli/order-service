package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;

import java.util.List;

public interface CategoryPort {
    List<DomainCategory> getAllCategoriesOfTenant(String tenantCode) throws ServiceException;
    DomainCategory addCategory(DomainCategory category);
}
