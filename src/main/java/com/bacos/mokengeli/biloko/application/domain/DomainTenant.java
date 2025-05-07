package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class DomainTenant {
    private Long id;
    private String code;
    private String name;
}
