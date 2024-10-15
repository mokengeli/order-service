package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.domain.DomainDishArticle;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DishMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.*;
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
    private final DishCategoryRepository dishCategoryRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public DishAdapter(DishRepository dishRepository, TenantContextRepository tenantContextRepository, DishArticleRepository dishArticleRepository, DishCategoryRepository dishCategoryRepository, ArticleRepository articleRepository, CategoryRepository categoryRepository, CurrencyRepository currencyRepository) {
        this.dishRepository = dishRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishArticleRepository = dishArticleRepository;
        this.dishCategoryRepository = dishCategoryRepository;
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    @Override
    public DomainDish createDish(DomainDish domainDish) throws ServiceException {
        List<DomainDishArticle> domainDishArticles = domainDish.getDishArticles();
        if (domainDishArticles == null || domainDishArticles.isEmpty()) {
            throw new ServiceException(UUID.randomUUID().toString(), "The compostion of the dish must be provided");
        }
        DomainCurrency domainCurrency = domainDish.getCurrency();
        if (domainCurrency == null ) {
            throw new ServiceException(UUID.randomUUID().toString(), "The currency must be provided");
        }
        Currency currency = this.currencyRepository.findById(domainCurrency.getId())
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No currency found with id " + domainCurrency.getId()));

        Dish dish = DishMapper.toEntity(domainDish);
        String tenantCode = domainDish.getTenantCode();
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + tenantCode));
        dish.setTenantContext(tenantContext);
        dish.setCurrency(currency);
        dish.setCreatedAt(LocalDateTime.now());
        dish = dishRepository.save(dish);

        List<DishArticle> dishArticles = createDishArticles(domainDish.getDishArticles(), dish);
        this.dishArticleRepository.saveAll(dishArticles);
        List<String> categories = domainDish.getCategories();
        if (categories != null && !categories.isEmpty()) {
            List<DishCategory> dishCategories = createDishCategories(categories, dish);
            this.dishCategoryRepository.saveAll(dishCategories);
        }

        DomainDish domainDish1 = DishMapper.toDomain(dish);
        domainDish1.setCategories(categories);
        return domainDish1;
    }

    private List<DishArticle> createDishArticles(List<DomainDishArticle> domainDishArticles, Dish dish) throws ServiceException {
        List<DishArticle> dishArticles = new ArrayList<>();
        for (DomainDishArticle domainDishArticle : domainDishArticles) {
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
        return dishArticles;
    }

    private List<DishCategory> createDishCategories(List<String> categories, Dish dish) throws ServiceException {
        List<DishCategory> dishCategories = new ArrayList<>();
        for (String name :categories) {
            Category category = this.categoryRepository.findByName(name)
                    .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(),
                            "No category found with the name " + name));

            DishCategory dishArticle = DishCategory.builder()
                    .dish(dish)
                    .category(category)
                    .build();

            dishCategories.add(dishArticle);
        }
        return dishCategories;
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
