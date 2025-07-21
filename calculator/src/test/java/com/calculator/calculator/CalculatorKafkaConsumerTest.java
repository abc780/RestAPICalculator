package com.calculator.calculator;

import com.calculator.calculator.model.OperationRequest;
import com.calculator.calculator.model.OperationResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CalculatorKafkaConsumerTest {

    @Mock
    private KafkaTemplate<String, OperationResult> kafkaTemplate;

    @InjectMocks
    private CalculatorKafkaConsumer calculatorKafkaConsumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    public void testSumOperation() {
        OperationRequest request = new OperationRequest();
        request.setOperation("sum");
        request.setA(BigDecimal.valueOf(5));
        request.setB(BigDecimal.valueOf(3));
        request.setCorrelationId("test-id");

        ConsumerRecord<String, OperationRequest> record =
                new ConsumerRecord<>("calculator.requests", 0, 0L, "key", request);

        calculatorKafkaConsumer.listenCalcRequests(record);

        ArgumentCaptor<OperationResult> resultCaptor = ArgumentCaptor.forClass(OperationResult.class);
        verify(kafkaTemplate).send(eq("calc-results"), eq("test-id"), resultCaptor.capture());

        OperationResult result = resultCaptor.getValue();
        assert result.getResult().equals(BigDecimal.valueOf(8));
    }

    @Test
    public void testDivisionByZero() {
        OperationRequest request = new OperationRequest();
        request.setOperation("division");
        request.setA(BigDecimal.TEN);
        request.setB(BigDecimal.ZERO);
        request.setCorrelationId("div-zero");

        ConsumerRecord<String, OperationRequest> record =
                new ConsumerRecord<>("calculator.requests", 0, 0L, "key", request);

        calculatorKafkaConsumer.listenCalcRequests(record);

        ArgumentCaptor<OperationResult> resultCaptor = ArgumentCaptor.forClass(OperationResult.class);
        verify(kafkaTemplate).send(eq("calc-results"), eq("div-zero"), resultCaptor.capture());

        OperationResult result = resultCaptor.getValue();
        assert result.getResult() == null;
    }

    @Test
    public void testUnsupportedOperation() {
        OperationRequest request = new OperationRequest();
        request.setOperation("power");
        request.setA(BigDecimal.ONE);
        request.setB(BigDecimal.TEN);
        request.setCorrelationId("unsupported-op");

        ConsumerRecord<String, OperationRequest> record =
                new ConsumerRecord<>("calculator.requests", 0, 0L, "key", request);

        calculatorKafkaConsumer.listenCalcRequests(record);

        ArgumentCaptor<OperationResult> resultCaptor = ArgumentCaptor.forClass(OperationResult.class);
        verify(kafkaTemplate).send(eq("calc-results"), eq("unsupported-op"), resultCaptor.capture());

        OperationResult result = resultCaptor.getValue();
        assert result.getResult() == null;
    }

    @Test
    public void testNullRequest() {
        ConsumerRecord<String, OperationRequest> record =
                new ConsumerRecord<>("calculator.requests", 0, 0L, "key", null);

        calculatorKafkaConsumer.listenCalcRequests(record);

        verifyNoInteractions(kafkaTemplate);
    }
}
