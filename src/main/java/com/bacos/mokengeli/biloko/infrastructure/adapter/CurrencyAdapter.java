package com.bacos.mokengeli.biloko.infrastructure.adapter;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.port.CurrencyPort;
import com.bacos.mokengeli.biloko.infrastructure.mapper.CurrencyMapper;
import com.bacos.mokengeli.biloko.infrastructure.model.Currency;
import com.bacos.mokengeli.biloko.infrastructure.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class CurrencyAdapter implements CurrencyPort {
    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyAdapter(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<DomainCurrency> getAllCurrencies() {
        List<Currency> currencies = this.currencyRepository.findAll();
        if (currencies.isEmpty()) {
            return Collections.emptyList();
        }
        return currencies.stream().map(CurrencyMapper::toDomain).toList();
    }
}
