package com.drivetrain.module1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class MissingConstantSetException extends RuntimeException {

    public MissingConstantSetException(String message) {
        super(message);
    }

    public MissingConstantSetException(Long constantSetId) {
        super("Constant set not found: " + constantSetId);
    }
}
