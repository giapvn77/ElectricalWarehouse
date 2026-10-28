package com.electricalwarehouse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** A rule violation the client can act on; mapped to a 4xx response. */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
