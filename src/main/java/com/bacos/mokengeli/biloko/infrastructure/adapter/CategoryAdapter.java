package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.CategoryMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Category;
import com.bacos.mokengeli.biloko.infrastructure.model.TenantCategory;
import com.bacos.mokengeli.biloko.infrastructure.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CategoryAdapter implements CategoryPort {

    private final CategoryRepository categoryRepository;
    private final TenantCategoryAdapter tenantCategoryAdapter;

    @Autowired
    public CategoryAdapter(CategoryRepository categoryRepository, TenantCategoryAdapter tenantCategoryAdapter) {
        this.categoryRepository = categoryRepository;
        this.tenantCategoryAdapter = tenantCategoryAdapter;
    }

    @Override
    public List<DomainCategory> getAllCategoriesOfTenant(String tenantCode) throws ServiceException {
        Optional<TenantCategory> optCategories = this.tenantCategoryAdapter.getCategories(tenantCode);
        if (optCategories.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> categorySet = this.tenantCategoryAdapter.getCategorySet(optCategories.get());
        return categorySet
                .stream()
                .map(x -> DomainCategory.builder().name(x).build())
                .collect(Collectors.toList());
    }

    @Override
    public DomainCategory addCategory(DomainCategory category) {
        Category entity = CategoryMapper.toEntity(category);
        entity.setCreatedAt(LocalDateTime.now());
        Category savedCategory = categoryRepository.save(entity);
        return CategoryMapper.toDomain(savedCategory);
    }
}
