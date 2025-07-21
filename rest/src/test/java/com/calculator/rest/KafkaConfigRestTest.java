package com.calculator.rest;

import com.calculator.rest.model.OperationRequest;
import com.calculator.rest.model.OperationResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = KafkaConfigRest.class)
class KafkaConfigRestTest {

    @Autowired
    private KafkaConfigRest kafkaConfigRest;

    @Test
    void testOperationRequestProducerFactory() {
        ProducerFactory<String, OperationRequest> factory = kafkaConfigRest.operationRequestProducerFactory();
        assertNotNull(factory);

        Producer<String, OperationRequest> producer = factory.createProducer();
        assertNotNull(producer);
        producer.close();
    }

    @Test
    void testOperationRequestKafkaTemplate() {
        KafkaTemplate<String, OperationRequest> template = kafkaConfigRest.operationRequestKafkaTemplate();
        assertNotNull(template);
    }

    @Test
    void testOperationResultConsumerFactory() {
        ConsumerFactory<String, OperationResult> factory = kafkaConfigRest.operationResultConsumerFactory();
        assertNotNull(factory);

        Consumer<String, OperationResult> consumer = factory.createConsumer();
        assertNotNull(consumer);
        consumer.close();
    }

    @Test
    void testOperationResultKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationResult> factory =
                kafkaConfigRest.operationResultKafkaListenerContainerFactory();
        assertNotNull(factory);
        assertNotNull(factory.getConsumerFactory());
    }
}
