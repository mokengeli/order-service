package com.bacos.mokengeli.biloko.application.domain;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomainValidateDebtValidationRequest {
    private Long debtValidationId;
    private Integer validationCode; // required if approved == true
    private boolean approved;
    private String rejectionReason; // required if approved == false

}