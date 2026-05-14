package com.drivetrain.module3.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class Module3ResultNotFoundException extends RuntimeException {

    public Module3ResultNotFoundException(Long designCaseId) {
        super("Module 3 result not found for design case: " + designCaseId);
    }
}
