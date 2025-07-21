// OperationResult.java
package com.calculator.rest.model;

import java.math.BigDecimal;

public class OperationResult {
    private String operation;
    private BigDecimal a;
    private BigDecimal b;
    private BigDecimal result;
    private String correlationId;

    public OperationResult() {}
    public OperationResult(String operation, BigDecimal a, BigDecimal b, BigDecimal result,
                           String correlationId) {
        this.operation = operation;
        this.a = a;
        this.b = b;
        this.result = result;
        this.correlationId = correlationId;
    }

    public static OperationResult error(String requestId, String s) {
        return null;
    }


    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public BigDecimal getA() { return a; }
    public void setA(BigDecimal a) { this.a = a; }

    public BigDecimal getB() { return b; }
    public void setB(BigDecimal b) { this.b = b; }

    public BigDecimal getResult() { return result; }
    public void setResult(BigDecimal result) { this.result = result; }
}
