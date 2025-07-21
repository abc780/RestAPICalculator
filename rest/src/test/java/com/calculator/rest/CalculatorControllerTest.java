package com.calculator.rest;

import com.calculator.rest.model.OperationRequest;
import com.calculator.rest.model.OperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculatorKafkaService kafkaService;

    private OperationResult successfulResult;

    @BeforeEach
    void setup() {
        successfulResult = new OperationResult();
        successfulResult.setA(BigDecimal.TEN);
        successfulResult.setB(BigDecimal.ONE);
        successfulResult.setOperation("SUM");
        successfulResult.setCorrelationId(UUID.randomUUID().toString());
        successfulResult.setResult(BigDecimal.valueOf(11));
    }

    @Test
    void testSumEndpoint() throws Exception {
        Mockito.when(kafkaService.sendOperationRequest(any(OperationRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(successfulResult));

        mockMvc.perform(get("/api/sum")
                        .param("a", "10")
                        .param("b", "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.operation", is("SUM")))
                .andExpect(jsonPath("$.a", is(10)))
                .andExpect(jsonPath("$.b", is(1)))
                .andExpect(jsonPath("$.result", is(11)));
    }

    @Test
    void testInternalServerError() throws Exception {
        Mockito.when(kafkaService.sendOperationRequest(any(OperationRequest.class)))
                .thenThrow(new RuntimeException("Kafka down"));

        mockMvc.perform(get("/api/divide")
                        .param("a", "10")
                        .param("b", "2"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.error", is("Internal server error: Kafka down")));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Calculator REST API is healthy"));
    }
}
