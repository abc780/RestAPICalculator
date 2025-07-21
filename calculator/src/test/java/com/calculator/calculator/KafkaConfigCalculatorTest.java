package com.calculator.calculator;

import com.calculator.calculator.model.OperationRequest;
import com.calculator.calculator.model.OperationResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KafkaConfigCalculatorTest {

    @Autowired
    private KafkaConfigCalculator kafkaConfigCalculator;

    @Test
    void operationResultProducerFactory() {
        ProducerFactory<String, OperationResult> factory = kafkaConfigCalculator.operationResultProducerFactory();
        assertNotNull(factory);
        Producer<String, OperationResult> producer = factory.createProducer();
        assertNotNull(producer);
        producer.close();
    }

    @Test
    void operationResultKafkaTemplate() {
        KafkaTemplate<String, OperationResult> template = kafkaConfigCalculator.operationResultKafkaTemplate();
        assertNotNull(template);
    }

    @Test
    void operationRequestConsumerFactory() {
        ConsumerFactory<String, OperationRequest> factory = kafkaConfigCalculator.operationRequestConsumerFactory();
        assertNotNull(factory);
        Consumer<String, OperationRequest> consumer = factory.createConsumer();
        assertNotNull(consumer);
        consumer.close();
    }

    @Test
    void operationRequestKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationRequest> factory =
                kafkaConfigCalculator.operationRequestKafkaListenerContainerFactory();
        assertNotNull(factory);
        assertNotNull(factory.getConsumerFactory());
    }
}
