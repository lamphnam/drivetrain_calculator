package com.drivetrain.module1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NoSuitableMotorFoundException extends RuntimeException {

    public NoSuitableMotorFoundException(BigDecimal requiredMotorPowerKw) {
        super("No active motor satisfies required motor power " + requiredMotorPowerKw.toPlainString() + " kW");
    }
}
