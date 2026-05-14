package com.drivetrain.module4.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class Module4PrerequisiteMissingException extends RuntimeException {

    public Module4PrerequisiteMissingException(String message) {
        super(message);
    }
}
