package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainHourlyOrderStat {
    private Integer hour;
    private Long orders;
}

