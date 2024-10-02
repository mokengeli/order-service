package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainDish;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;
import com.bacos.mokengeli.biloko.application.port.DishPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.ArticleMapper;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DishArticleMapper;
import com.bacos.mokengeli.biloko.infrastructure.mapper.DishMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.*;
import com.bacos.mokengeli.biloko.infrastructure.repository.DishArticleRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.DishRepository;
import com.bacos.mokengeli.biloko.infrastructure.repository.TenantContextRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DishAdapter implements DishPort {

    private final DishRepository dishRepository;
    private final TenantContextRepository tenantContextRepository;
    private final DishArticleRepository dishArticleRepository;

    @Autowired
    public DishAdapter(DishRepository dishRepository, TenantContextRepository tenantContextRepository, DishArticleRepository dishArticleRepository) {
        this.dishRepository = dishRepository;
        this.tenantContextRepository = tenantContextRepository;
        this.dishArticleRepository = dishArticleRepository;
    }

    @Transactional
    @Override
    public DomainDish saveDish(DomainDish domainDish) throws ServiceException {
        Dish dish = DishMapper.toEntity(domainDish);
        String tenantCode = domainDish.getTenantCode();
        TenantContext tenantContext = this.tenantContextRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new ServiceException(UUID.randomUUID().toString(), "No tenant  find with tenant_code=" + tenantCode));
        dish.setTenantContext(tenantContext);
        dish.setCreatedAt(LocalDateTime.now());
        final Dish saved = dishRepository.save(dish);


        List<DishArticle> dishArticles = domainDish.getDishArticles().stream()
                .map(x -> {
                    DishArticle dishArticle = DishArticleMapper.toEntity(x);
                    dishArticle.setDish(saved);
                    return dishArticle;
                }).toList();
       // saved.setDishArticles(dishArticles);

        this.dishArticleRepository.saveAll(dishArticles);

        return DishMapper.toDomain(saved);
    }

    @Override
    public List<DomainDish> findAllDishesByTenant(String tenantCode) {
        // dishRepository.findAllByTenantCode(tenantCode);
        return null;
    }
}
