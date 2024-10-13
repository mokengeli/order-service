package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DomainCategory {
    private Long id;
    private String name;
}
