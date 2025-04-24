package com.bacos.mokengeli.biloko.application.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class DomainRefTable {
    private Long id;
    private String name;
    private String tenantCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
