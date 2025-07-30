package com.bacos.mokengeli.biloko.application.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DomainDailyDishStat {
    private final LocalDate day;
    private final long dishesPrepared;
}
