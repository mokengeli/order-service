package com.bacos.mokengeli.biloko.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedUser {
    private String tenantCode;
    private String employeeNumber;
    private List<String> roles;
    private List<String> permissions;
}
