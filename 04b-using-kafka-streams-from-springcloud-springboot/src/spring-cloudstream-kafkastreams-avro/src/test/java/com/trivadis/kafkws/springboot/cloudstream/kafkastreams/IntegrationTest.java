package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import com.trivadis.kafkaws.Notification;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    private KafkaTemplate<Void, Notification> kafkaTemplate;

    private String topicName = "test-kstream-spring-cloudstream-input-topic";

    @Test
    public void produceOneNotification() {
        Notification notification = Notification.newBuilder().setId(1L).setMessage("This is a notification").build();
        kafkaTemplate.send(topicName, notification);

    }

}
