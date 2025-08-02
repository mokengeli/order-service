package com.bacos.mokengeli.biloko.application.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DomainValidateDebtValidation {
    private String message;
    private boolean approved;
}