package com.calculator.rest;

import com.calculator.rest.model.OperationRequest;
import com.calculator.rest.model.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    @Autowired
    private CalculatorKafkaService kafkaService;

    @GetMapping("/sum")
    public ResponseEntity<OperationResult> sum(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
       return performCalculation("SUM", a, b);
    }

    @GetMapping("/subtract")
    public ResponseEntity<OperationResult> subtract(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
        return performCalculation("SUBTRACT", a, b);
    }

    @GetMapping("/multiply")
    public ResponseEntity<OperationResult> multiply(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
        return performCalculation("MULTIPLY", a, b);
    }

    @GetMapping("/divide")
    public ResponseEntity<OperationResult> divide(@RequestParam("a") BigDecimal a, @RequestParam("b") BigDecimal b) {
        return performCalculation("DIVIDE", a, b);
    }

    private ResponseEntity<OperationResult> performCalculation(String operation, BigDecimal a, BigDecimal b) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            logger.info("Received {} request: a={}, b={}", operation, a, b);

            OperationRequest request = new OperationRequest();
            request.setA(a);
            request.setB(b);
            request.setCorrelationId(requestId);
            request.setOperation(operation);

            CompletableFuture<OperationResult> futureResponse = kafkaService.sendOperationRequest(request);
            OperationResult response = futureResponse.get(30, TimeUnit.SECONDS);

            logger.info("Calculation completed successfully: result={}", response.getResult());

            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .body(response);

        } catch (Exception e) {
            logger.error("Error processing calculation request", e);
            OperationResult errorResponse = OperationResult.error(requestId,
                    "Internal server error: " + e.getMessage());

            return ResponseEntity.status(500)
                    .header("X-Request-ID", requestId)
                    .body(errorResponse);
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Calculator REST API is healthy");
    }
}
