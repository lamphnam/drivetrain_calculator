package com.drivetrain.module1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DesignCaseNotFoundException extends RuntimeException {

    public DesignCaseNotFoundException(Long designCaseId) {
        super("Design case not found: " + designCaseId);
    }
}
