package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainDishArticle;
import com.bacos.mokengeli.biloko.infrastructure.model.DishArticle;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DishArticleMapper {

    public DishArticle toEntity(DomainDishArticle domainDishArticle) {
        if (domainDishArticle == null) {
            return null;
        }

        return DishArticle.builder()

                .dish(DishMapper.toEntity(domainDishArticle.getDish()))
                .article(ArticleMapper.toLigthEntity(domainDishArticle.getArticle()))
                .quantity(domainDishArticle.getQuantity())
                .removable(domainDishArticle.getRemovable())
                .build();
    }

    public DomainDishArticle toDomain(DishArticle dishArticle) {
        if (dishArticle == null) {
            return null;
        }

        return DomainDishArticle.builder()
                .dish(DishMapper.toDomain(dishArticle.getDish()))
                .article(ArticleMapper.toDomain(dishArticle.getArticle()))
                .quantity(dishArticle.getQuantity())
                .removable(dishArticle.getRemovable())
                .build();
    }
}
