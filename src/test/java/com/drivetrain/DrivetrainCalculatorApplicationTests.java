package com.drivetrain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DrivetrainCalculatorApplicationTests {

    @Test
    void applicationClassCanBeInstantiated() {
        assertDoesNotThrow(DrivetrainCalculatorApplication::new);
    }
}
