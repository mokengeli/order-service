package com.bacos.mokengeli.biloko.presentation.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ResponseStatusWrapperExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusWrapperException.class)
    protected ResponseEntity<Object> handleResponseStatusException(ResponseStatusWrapperException ex, WebRequest request) {
        return handleExceptionInternal(ex, makeBody(ex), new HttpHeaders(), ex.getStatusCode(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
//        ErrorMessage errorDetails = new ErrorMessage(new Date(), ex.getMessage(), request.getDescription(false));
//        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
        return handleExceptionInternal(ex, makeBody(new ResponseStatusWrapperException(HttpStatusCode.valueOf(403),ex.getMessage())), new HttpHeaders(), HttpStatusCode.valueOf(403), request);
    }


    private ResponseStatusExceptionBody makeBody(ResponseStatusWrapperException ex) {
        List<String> messages = new ArrayList<>();
        messages.add(ex.getReason());
        return new ResponseStatusExceptionBody(messages, ex.getUuidTechnique());
    }
}
