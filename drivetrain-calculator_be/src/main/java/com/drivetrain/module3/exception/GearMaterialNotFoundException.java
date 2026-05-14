package com.drivetrain.module3.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GearMaterialNotFoundException extends RuntimeException {

    public GearMaterialNotFoundException(Long materialId) {
        super("Gear material not found: " + materialId);
    }
}
