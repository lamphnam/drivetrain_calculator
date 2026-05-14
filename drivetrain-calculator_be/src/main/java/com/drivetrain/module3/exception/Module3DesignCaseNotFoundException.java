package com.drivetrain.module3.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class Module3DesignCaseNotFoundException extends RuntimeException {

    public Module3DesignCaseNotFoundException(Long designCaseId) {
        super("Design case not found: " + designCaseId);
    }
}
