package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.infrastructure.model.TenantContextCategory;
import com.bacos.mokengeli.biloko.infrastructure.repository.CategoryRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantCategoryRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantContextRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TenantCategoryAdapter {
    private final TenantCategoryRepository tenantCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final TenantContextRepository tenantContextRepository;

    public TenantCategoryAdapter(TenantCategoryRepository TenantCategoryRepository, CategoryRepository categoryRepository, TenantContextRepository tenantContextRepository) {
        this.tenantCategoryRepository = TenantCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.tenantContextRepository = tenantContextRepository;
    }

    // Method to add a category
  /**  public void addCategory(String tenantCode, String categoryName) throws ServiceException {
        TenantContext tenantContext = tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "Tenant not found with given tenant code " + tenantCode));
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                        "Category not found with the given name " + categoryName));

        TenantCategory tenantCategory = new TenantCategory();
        tenantCategory.setCategory(category);
        tenantCategory.setTenantContext(tenantContext);
        TenantCategory tenantCategory1 = this.tenantCategoryRepository.save(tenantCategory);

    }
*/
    public List<DomainCategory> getCategories(String tenantCode) throws ServiceException {
        List<TenantContextCategory> tenantCategories = this.tenantCategoryRepository.findByTenantContextTenantCode(tenantCode).orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                "Tenant not found with given tenant code " + tenantCode));

        return tenantCategories.stream()
                .map(x -> DomainCategory.builder()
                        .id(x.getCategory().getId())
                        .name(x.getCategory().getName())
                        .build())
                .toList();
    }


}
