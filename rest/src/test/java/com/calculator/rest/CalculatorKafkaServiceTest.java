package com.calculator.rest;

import com.calculator.rest.model.OperationRequest;
import com.calculator.rest.model.OperationResult;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalculatorKafkaServiceTest {

    private KafkaTemplate<String, OperationRequest> kafkaTemplate;
    private CalculatorKafkaService kafkaService;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaService = new CalculatorKafkaService(kafkaTemplate);
    }

    @Test
    void testSendOperationRequest_sendsMessageAndStoresFuture() {
        OperationRequest request = new OperationRequest();
        request.setA(BigDecimal.TEN);
        request.setB(BigDecimal.ONE);
        request.setOperation("SUM");
        request.setCorrelationId("test-id");

        CompletableFuture<OperationResult> future = kafkaService.sendOperationRequest(request);

        assertNotNull(future);
        assertFalse(future.isDone());

        // Capture the sent record
        ArgumentCaptor<ProducerRecord<String, OperationRequest>> recordCaptor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, OperationRequest> sentRecord = recordCaptor.getValue();
        assertEquals("calculator.requests", sentRecord.topic());
        assertEquals(request, sentRecord.value());

        Headers headers = sentRecord.headers();
        RecordHeader header = (RecordHeader) headers.lastHeader("correlationId");
        assertNotNull(header);
        assertEquals("test-id", new String(header.value(), StandardCharsets.UTF_8));
    }

    @Test
    void testReceiveResponse_completesMatchingFuture() throws Exception {
        // Prepare and register a pending future
        OperationRequest request = new OperationRequest();
        request.setCorrelationId("match-id");
        CompletableFuture<OperationResult> future = kafkaService.sendOperationRequest(request);

        OperationResult response = new OperationResult();
        response.setCorrelationId("match-id");
        response.setResult(BigDecimal.valueOf(42));

        kafkaService.receiveResponse(response);

        assertTrue(future.isDone());
        assertEquals(BigDecimal.valueOf(42), future.get().getResult());
    }

    @Test
    void testReceiveResponse_withUnknownCorrelationId_logsWarning() {
        OperationResult response = new OperationResult();
        response.setCorrelationId("unknown-id");

        // Nothing registered with this ID — should not throw
        kafkaService.receiveResponse(response);
    }
}
