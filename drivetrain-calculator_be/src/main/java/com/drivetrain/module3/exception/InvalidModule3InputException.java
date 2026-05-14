package com.drivetrain.module3.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidModule3InputException extends RuntimeException {

    public InvalidModule3InputException(String message) {
        super(message);
    }
}
