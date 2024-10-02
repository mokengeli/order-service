package com.bacos.mokengeli.biloko.presentation.exception;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ResponseStatusExceptionBody {
    private List<String> message;
    private String uuidTechnique;
    private Date timeStamp = new Date();

    public ResponseStatusExceptionBody(List<String> message, String uuidTechnique) {
        this.message = message;
        this.uuidTechnique = uuidTechnique;
    }

}
