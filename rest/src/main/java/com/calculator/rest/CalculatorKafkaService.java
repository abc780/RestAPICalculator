package com.calculator.rest;

import com.calculator.rest.model.OperationRequest;
import com.calculator.rest.model.OperationResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CalculatorKafkaService {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorKafkaService.class);

    private final KafkaTemplate<String, OperationRequest> requestKafkaTemplate;
    private final Map<String, CompletableFuture<OperationResult>> pendingRequests = new ConcurrentHashMap<>();

    private final String requestTopic = "calculator.requests";
    private final String replyTopic = "calculator.responses";

    public CalculatorKafkaService(KafkaTemplate<String, OperationRequest> requestKafkaTemplate) {
        this.requestKafkaTemplate = requestKafkaTemplate;
    }

    public CompletableFuture<OperationResult> sendOperationRequest(OperationRequest request) {
        String correlationId = request.getCorrelationId();

        // Create and store future
        CompletableFuture<OperationResult> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        // Create producer record with headers
        ProducerRecord<String, OperationRequest> record = new ProducerRecord<>(requestTopic, request);
        record.headers().add(new RecordHeader("correlationId", correlationId.getBytes(StandardCharsets.UTF_8)));

        logger.info("Sending Kafka request with correlationId={}", correlationId);

        requestKafkaTemplate.send(record);

        return future;
    }

    @KafkaListener(topics = replyTopic, groupId = "calculator-rest-group", containerFactory = "operationResultKafkaListenerContainerFactory")
    public void receiveResponse(OperationResult response) {
        String correlationId = response.getCorrelationId();
        logger.info("Received Kafka response for correlationId={}", correlationId);

        CompletableFuture<OperationResult> future = pendingRequests.remove(correlationId);
        if (future != null) {
            future.complete(response);
        } else {
            logger.warn("No pending request found for correlationId={}", correlationId);
        }
    }
}
