package com.calculator.calculator;

import com.calculator.calculator.model.OperationRequest;
import com.calculator.calculator.model.OperationResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculatorKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorKafkaConsumer.class);

    private final KafkaTemplate<String, OperationResult> kafkaTemplate;

    public CalculatorKafkaConsumer(KafkaTemplate<String, OperationResult> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "calculator.requests", groupId = "calculator-rest-group")
    public void listenCalcRequests(ConsumerRecord<String, OperationRequest> record) {

        logger.info("Received message from calc-requests topic, key: {}", record.key());

        OperationRequest request = record.value();
        if (request == null) {
            logger.warn("Received null request");
            return;
        }

        logger.info("Processing request - Operation: {}, A: {}, B: {}, CorrelationId: {}",
                request.getOperation(), request.getA(), request.getB(), request.getCorrelationId());

        BigDecimal resultValue;
        String error = null;

        try {
            resultValue = performCalculation(request.getOperation(), request.getA(), request.getB());
            logger.info("Calculation completed successfully. Result: {}", resultValue);
        } catch (ArithmeticException e) {
            logger.error("Arithmetic error during calculation: {}", e.getMessage());
            resultValue = null;
            error = "Arithmetic error: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Unexpected error during calculation: {}", e.getMessage(), e);
            resultValue = null;
            error = "Error: " + e.getMessage();
        }

        OperationResult resultPayload = new OperationResult();
        resultPayload.setOperation(request.getOperation());
        resultPayload.setA(request.getA());
        resultPayload.setB(request.getB());
        resultPayload.setResult(resultValue);
        resultPayload.setCorrelationId(request.getCorrelationId());

        if (error != null) {
            logger.warn("Sending result with error: {}", error);
        }

        try {
            logger.info("Sending result to calc-results topic for correlation ID: {}", request.getCorrelationId());
            kafkaTemplate.send("calc-results", request.getCorrelationId(), resultPayload).get();
            logger.info("Result sent successfully to Kafka");
        } catch (Exception e) {
            logger.error("Failed to send result to Kafka: {}", e.getMessage(), e);
        }
    }

    private BigDecimal performCalculation(String operation, BigDecimal a, BigDecimal b) {
        logger.debug("Performing calculation: {} {} {}", a, operation, b);

        switch (operation.toLowerCase()) {
            case "sum":
                return a.add(b);
            case "subtraction":
                return a.subtract(b);
            case "multiplication":
                return a.multiply(b);
            case "division":
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a.divide(b, 10, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }
}