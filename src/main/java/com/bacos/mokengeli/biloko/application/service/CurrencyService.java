package com.bacos.mokengeli.biloko.application.service;


import com.bacos.mokengeli.biloko.application.domain.DomainCurrency;
import com.bacos.mokengeli.biloko.application.port.CurrencyPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {
    private final CurrencyPort currencyPort;

    @Autowired
    public CurrencyService(CurrencyPort currencyPort) {
        this.currencyPort = currencyPort;
    }

    public List<DomainCurrency> getAllCurrencies() {
        return this.currencyPort.getAllCurrencies();
    }
}
