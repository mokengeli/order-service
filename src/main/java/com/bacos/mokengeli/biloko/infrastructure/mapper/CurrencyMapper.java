package com.bacos.mokengeli.biloko.infrastructure.mapper;

import com.bacos.mokengeli.biloko.application.domain.DomainCategory;
import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.infrastructure.model.Category;
import com.bacos.mokengeli.biloko.infrastructure.model.Currency;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CurrencyMapper {

    public DomainCurrency toDomain(Currency currency) {
        return DomainCurrency.builder()
                .id(currency.getId())
                .label(currency.getLabel())
                .code(currency.getCode())
                .build();
    }


}
