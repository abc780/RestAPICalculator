// OperationRequest.java
package com.calculator.calculator.model;

import java.math.BigDecimal;

public class OperationRequest {
    private String operation;
    private BigDecimal a;
    private BigDecimal b;
    private String correlationId;

    public OperationRequest() {}
    public OperationRequest(String operation, BigDecimal a, BigDecimal b, String correlationId) {
        this.operation = operation;
        this.a = a;
        this.b = b;
        this.correlationId = correlationId;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public BigDecimal getA() { return a; }
    public void setA(BigDecimal a) { this.a = a; }

    public BigDecimal getB() { return b; }
    public void setB(BigDecimal b) { this.b = b; }
}
