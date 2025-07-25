package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface CategoryPort {
    Page<DomainCategory> getAllCategoriesOfTenant(String tenantCode, int page, int size,  String search) throws ServiceException;
    DomainCategory addCategory(DomainCategory category);

    Page<DomainCategory> getAllCategories(int page, int size, String search) throws ServiceException;
    void assiginToTenant(Long categoryId, String tenantCode) throws ServiceException;
}
