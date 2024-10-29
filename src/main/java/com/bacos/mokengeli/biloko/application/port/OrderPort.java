package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;
import com.bacos.mokengeli.biloko.application.domain.model.CreateOrder;
import com.bacos.mokengeli.biloko.application.exception.ServiceException;

import java.util.Optional;

public interface OrderPort {
    Optional<DomainOrder> createOrder(CreateOrder order) throws ServiceException;
}
