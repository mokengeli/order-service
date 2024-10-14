package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.CategoryMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Category;
import com.bacos.mokengeli.biloko.infrastructure.repository.CategoryRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class CategoryAdapter implements CategoryPort {

    private final CategoryRepository categoryRepository;
    private final TenantCategoryAdapter tenantCategoryAdapter;
    private final TenantContextRepository tenantContextRepository;

    @Autowired
    public CategoryAdapter(CategoryRepository categoryRepository, TenantCategoryAdapter tenantCategoryAdapter, TenantContextRepository tenantContextRepository) {
        this.categoryRepository = categoryRepository;
        this.tenantCategoryAdapter = tenantCategoryAdapter;
        this.tenantContextRepository = tenantContextRepository;
    }

    @Override
    public List<DomainCategory> getAllCategoriesOfTenant(String tenantCode) throws ServiceException {
        return this.tenantCategoryAdapter.getCategories(tenantCode);
    }

    @Override
    public DomainCategory addCategory(DomainCategory category) {
        Category entity = CategoryMapper.toEntity(category);
        entity.setCreatedAt(LocalDateTime.now());
        Category savedCategory = categoryRepository.save(entity);
        return CategoryMapper.toDomain(savedCategory);
    }
}
