package com.bacos.mokengeli.biloko.application.exception;

import lombok.Getter;

@Getter
public class ServiceException extends Exception {

    private String technicalId;

    public ServiceException(String technicalId, String message) {
        super(message);
        this.technicalId = technicalId;
    }
}
