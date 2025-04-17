package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.CategoryMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Category;
import com.bacos.mokengeli.biloko.infrastructure.model.TenantContext;
import com.bacos.mokengeli.biloko.infrastructure.model.TenantContextCategory;
import com.bacos.mokengeli.biloko.infrastructure.repository.CategoryRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantCategoryRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CategoryAdapter implements CategoryPort {

    private final CategoryRepository categoryRepository;
    private final TenantCategoryAdapter tenantCategoryAdapter;
    private final TenantCategoryRepository tenantCategoryRepository;
    private final TenantContextRepository tenantContextRepository;

    @Autowired
    public CategoryAdapter(CategoryRepository categoryRepository,
                           TenantCategoryAdapter tenantCategoryAdapter, TenantCategoryRepository tenantCategoryRepository, TenantContextRepository tenantContextRepository) {
        this.categoryRepository = categoryRepository;
        this.tenantCategoryAdapter = tenantCategoryAdapter;
        this.tenantCategoryRepository = tenantCategoryRepository;
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

    @Override
    public List<DomainCategory> getAllCategories() {
        List<Category> categories = this.categoryRepository.findAll();
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream().map(CategoryMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void assiginToTenant(Long categoryId, String tenantCode) throws ServiceException {

        TenantContext tenant = this.tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "No tenant found with the code " + tenantCode));
        Category category = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "No category found with the name " + categoryId));

        TenantContextCategory tenantContextCategory = TenantContextCategory.builder()
                .tenantContext(tenant)
                .category(category).build();
        this.tenantCategoryRepository.save(tenantContextCategory);

    }
}
