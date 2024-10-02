package com.bacos.mokengeli.biloko.presentation.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

public class ResponseStatusWrapperException extends ResponseStatusException {
    private String uuidTechnique;

    public ResponseStatusWrapperException(HttpStatusCode status, String reason) {
        super(status, reason);
    }

    public ResponseStatusWrapperException(HttpStatusCode rawStatusCode, @Nullable String reason, @Nullable String uuidTechnique) {
        super(rawStatusCode, reason);
        this.uuidTechnique = uuidTechnique;
    }

    public String getUuidTechnique() {
        return uuidTechnique;
    }

    public void setUuidTechnique(String uuidTechnique) {
        this.uuidTechnique = uuidTechnique;
    }
}
