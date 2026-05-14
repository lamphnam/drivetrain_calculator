package com.drivetrain.module4.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class Module4ResultNotFoundException extends RuntimeException {

    public Module4ResultNotFoundException(Long designCaseId) {
        super("Module 4 result not found for design case: " + designCaseId);
    }
}
