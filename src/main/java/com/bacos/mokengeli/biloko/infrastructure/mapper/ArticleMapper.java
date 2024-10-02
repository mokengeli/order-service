package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainArticle;
import com.bacos.mokengeli.biloko.infrastructure.model.Article;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ArticleMapper {

    public Article toLigthEntity(DomainArticle domainArticle) {
        if (domainArticle == null) {
            return null;
        }

        return Article.builder()
                .id(domainArticle.getId())

                .build();
    }

    public DomainArticle toDomain(Article article) {
        if (article == null) {
            return null;
        }

        return DomainArticle.builder()
                .id(article.getId())
                .name(article.getName())
                .unitOfMeasure(article.getUnitOfMeasure())
                .tenantCode(article.getTenantContext().getTenantCode())
                .build();
    }
}
