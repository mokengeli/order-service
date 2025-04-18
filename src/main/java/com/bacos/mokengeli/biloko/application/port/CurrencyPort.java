package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;

import java.util.List;

public interface CurrencyPort {
    List<DomainCurrency> getAllCurrencies();
}
