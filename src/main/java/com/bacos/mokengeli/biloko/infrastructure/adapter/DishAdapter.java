package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainDishArticle;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DishMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.ArticleRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.DishArticleRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.DishRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantContextRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DishAdapter implements DishPort {

    private final DishRepository dishRepository;
    private final TenantContextRepository tenantContextRepository;
    private final DishArticleRepository dishArticleRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public DishAdapter(DishRepository dishRepository, TenantContextRepository tenantContextRepository, DishArticleRepository dishArticleRepository, ArticleRepository articleRepository) {
        this.dishRepository = dishRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishArticleRepository = dishArticleRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    @Override
    public DomainDish createDish(DomainDish domainDish) throws ServiceException {
        Dish dish = DishMapper.toEntity(domainDish);
        String tenantCode = domainDish.getTenantCode();
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + tenantCode));
        dish.setTenantContext(tenantContext);
        dish.setCreatedAt(LocalDateTime.now());
        dish = dishRepository.save(dish);
        List<DishArticle> dishArticles = new ArrayList<>();
        for (DomainDishArticle domainDishArticle : domainDish.getDishArticles()) {
            Article article = articleRepository.findById(domainDishArticle.getArticle().getId())
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No article found with the given id " + domainDishArticle.getArticle().getId()));

            DishArticle dishArticle = DishArticle.builder()
                    .dish(dish)
                    .article(article)
                    .quantity(domainDishArticle.getQuantity())
                    .removable(domainDishArticle.getRemovable())
                    .build();

            dishArticles.add(dishArticle);
        }

        this.dishArticleRepository.saveAll(dishArticles);

        return DishMapper.toDomain(dish);
    }

    @Override
    public Optional<List<DomainDish>> findAllDishesByTenant(String tenantCode) {
        Optional<List<Dish>> optDish = dishRepository.findByTenantContextTenantCode(tenantCode);
        if (optDish.isEmpty()) {
            return Optional.empty();
        }
        List<DomainDish> domainDishes = optDish.get().stream().map(DishMapper::toDomain).toList();
        return Optional.of(domainDishes);
    }

    @Override
    public boolean isAllDishesOfTenant(String tenantCode, List<Long> dishIds) {
        return this.dishRepository.isAllDishesOfTenant(tenantCode, dishIds, dishIds.size());
    }
}
