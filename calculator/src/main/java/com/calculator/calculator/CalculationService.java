package com.calculator.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class CalculationService {

    private static final Logger logger = LoggerFactory.getLogger(CalculationService.class);
    private static final MathContext MATH_CONTEXT = new MathContext(34, RoundingMode.HALF_UP);

    public BigDecimal performCalculation(String operation, BigDecimal a, BigDecimal b) {
        logger.debug("Performing calculation: {} {} {}", a, operation, b);

        try {
            BigDecimal result = switch (operation.toUpperCase()) {
                case "SUM", "ADD" -> a.add(b, MATH_CONTEXT);
                case "SUBTRACT", "SUB" -> a.subtract(b, MATH_CONTEXT);
                case "MULTIPLY", "MUL" -> a.multiply(b, MATH_CONTEXT);
                case "DIVIDE", "DIV" -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        throw new ArithmeticException("Division by zero is not allowed");
                    }
                    yield a.divide(b, MATH_CONTEXT);
                }
                default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
            };

            logger.debug("Calculation result: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("Error performing calculation: {} {} {}", a, operation, b, e);
            throw e;
        }
    }
}