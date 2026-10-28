package com.electricalwarehouse.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String entity, Object id) {
        super(HttpStatus.NOT_FOUND, entity + " không tồn tại: " + id);
    }
}
