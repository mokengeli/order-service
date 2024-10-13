package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.port.CategoryPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.CategoryMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Category;
import com.bacos.mokengeli.biloko.infrastructure.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryAdapter implements CategoryPort {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryAdapter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<DomainCategory> getAllCategories(String tenantCode) {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryMapper::toDomain)
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
