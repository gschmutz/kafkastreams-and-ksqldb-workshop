package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class DLQTopicConsumer {
    private final Log logger = LogFactory.getLog(getClass());

    @KafkaListener(topics = "test-kstream-spring-cloudstream-error-input-topic", groupId = "dlq-consumer")
    public void listen(@Payload String value,
                       @Header(name = KafkaHeaders.RECEIVED_TOPIC, required = true) String topicName,
                       @Header(name = KafkaHeaders.DLT_ORIGINAL_OFFSET, required = false) Long originalOffset,
                       @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
                       @Header(KafkaHeaders.DLT_ORIGINAL_PARTITION) int originalPartition) {
        logger.info("DLQ Topic: " + topicName);
        logger.info("Exception: " + exceptionMessage);
        logger.info("Original Offset: " + originalOffset);
        logger.info("Original Partition: " + originalPartition);
        logger.info("Value: " + value);
    }
}
