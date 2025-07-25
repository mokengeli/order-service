package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.DomainTenant;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.CategoryMapper;
import com.bacos.mokengeli.biloko.infrastructure.mapper.TenantMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Category;
import com.bacos.mokengeli.biloko.infrastructure.model.Tenant;
import com.bacos.mokengeli.biloko.infrastructure.model.TenantCategory;
import com.bacos.mokengeli.biloko.infrastructure.repository.CategoryRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantCategoryRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.proxy.UserProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class CategoryAdapter implements CategoryPort {

    private final CategoryRepository categoryRepository;
    private final TenantCategoryRepository tenantCategoryRepository;
    private final TenantRepository tenantRepository;
    private final UserProxy userProxy;

    @Autowired
    public CategoryAdapter(CategoryRepository categoryRepository,
                           TenantCategoryRepository tenantCategoryRepository, TenantRepository tenantRepository, UserProxy userProxy) {
        this.categoryRepository = categoryRepository;
        this.tenantCategoryRepository = tenantCategoryRepository;
        this.tenantRepository = tenantRepository;
        this.userProxy = userProxy;
    }

    @Override
    public Page<DomainCategory> getAllCategoriesOfTenant(
            String tenantCode,
            int    page,
            int    size,
            String search
    ) throws ServiceException {
        Pageable pageable = PageRequest.of(page, size);
        Page<TenantCategory> pageResult;

        if (search == null || search.trim().isEmpty()) {
            pageResult = tenantCategoryRepository.findByTenantCode(tenantCode, pageable);
        } else {
            pageResult = tenantCategoryRepository
                    .findByTenantCodeAndCategoryNameContainingIgnoreCase(
                            tenantCode, search, pageable
                    );
        }

        return pageResult.map(tc -> CategoryMapper.toDomain(tc.getCategory()));
    }

    @Override
    public DomainCategory addCategory(DomainCategory category) {
        Category entity = CategoryMapper.toEntity(category);
        entity.setCreatedAt(OffsetDateTime.now());
        Category savedCategory = categoryRepository.save(entity);
        return CategoryMapper.toDomain(savedCategory);
    }

    @Override
    public Page<DomainCategory> getAllCategories(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Category> pageResult;
        if (search == null || search.trim().isEmpty()) {
            pageResult = categoryRepository.findAll(pageable);
        } else {
            pageResult = categoryRepository.findByNameContainingIgnoreCase(search, pageable);
        }

        return pageResult.map(CategoryMapper::toDomain);
    }

    @Override
    public void assiginToTenant(Long categoryId, String tenantCode) throws ServiceException {
        Optional<Tenant> optTenant = this.tenantRepository.findByCode(tenantCode);
        Tenant tenant = optTenant.orElse(null);
        if (tenant == null) {
            DomainTenant domainTenant = this.userProxy.getTenantByCode(tenantCode)
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No tenant found with code " + tenantCode));
            tenant = TenantMapper.toEntity(domainTenant);
            tenant = this.tenantRepository.save(tenant);

        }
        Category category = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "No category found with the name " + categoryId));

        TenantCategory tenantCategory = TenantCategory.builder()
                .tenant(tenant)
                .category(category).build();
        this.tenantCategoryRepository.save(tenantCategory);

    }
}
