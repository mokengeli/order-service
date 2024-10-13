package com.bacos.mokengeli.biloko.application.port;

import com.bacos.mokengeli.biloko.application.domain.DomainOrder;

import java.util.Optional;

public interface OrderPort {
    DomainOrder saveOrder(DomainOrder order);
    Optional<DomainOrder> findById(Long orderId);
}
