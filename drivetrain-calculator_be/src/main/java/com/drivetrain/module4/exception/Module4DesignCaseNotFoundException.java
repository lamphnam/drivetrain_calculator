package com.drivetrain.module4.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class Module4DesignCaseNotFoundException extends RuntimeException {

    public Module4DesignCaseNotFoundException(Long designCaseId) {
        super("Design case not found: " + designCaseId);
    }
}
