package com.bacos.mokengeli.biloko.application.domain.dashboard;


import com.bacos.mokengeli.biloko.application.domain.OrderPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DomainPaymentStatusStat {
    private final OrderPaymentStatus status;
    private final long count;

}