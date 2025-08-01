package com.bacos.mokengeli.biloko.application.domain;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DomainCloseOrderWithDebt {
    private String message;
    private boolean validationRequired;
    private Long validationRequestId;
}