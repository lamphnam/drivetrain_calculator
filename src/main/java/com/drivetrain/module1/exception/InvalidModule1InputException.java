package com.drivetrain.module1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidModule1InputException extends RuntimeException {

    public InvalidModule1InputException(String message) {
        super(message);
    }
}
