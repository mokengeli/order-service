package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.infrastructure.model.Category;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CategoryMapper {

    public DomainCategory toDomain(Category category) {
        return DomainCategory.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category toEntity(DomainCategory domainCategory) {
        Category category = new Category();
        category.setName(domainCategory.getName());
        return category;
    }
}
