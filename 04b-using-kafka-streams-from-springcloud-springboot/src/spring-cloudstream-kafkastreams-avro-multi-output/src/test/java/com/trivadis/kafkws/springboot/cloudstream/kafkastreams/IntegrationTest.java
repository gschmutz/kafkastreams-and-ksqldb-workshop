package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import com.trivadis.kafkaws.avro.v1.Alert;
import com.trivadis.kafkaws.avro.v1.AlertSentEvent;
import com.trivadis.kafkaws.avro.v1.Notification;
import com.trivadis.kafkaws.avro.v1.NotificationSentEvent;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    private KafkaTemplate<Void, SpecificRecord> kafkaTemplate;

    private String topicName = "test-kstream-spring-cloudstream-input-topic";

    @Test
    public void produceOneNotificationSentEvent() {
        long id = 1L;
        NotificationSentEvent notificationSentEvent = NotificationSentEvent.newBuilder()
                .setNotification(
                        Notification.newBuilder()
                                .setId(id)
                                .setMessage("[" + id + "] Hello Kafka")
                                .setCreatedAt(Instant.now()).build()
                ).build();
        kafkaTemplate.send(topicName, notificationSentEvent);
    }

    @Test
    public void produceOneAlertSentEvent() {
        long id = 1L;
        AlertSentEvent alertSentEvent = AlertSentEvent.newBuilder()
                .setAlert(
                        Alert.newBuilder()
                                .setId(UUID.randomUUID())
                                .setMessage("Alert")
                                .setSeverity("HIGH")
                                .setWhen(Instant.now()).build()
                ).build();
        kafkaTemplate.send(topicName, alertSentEvent);
    }
}
