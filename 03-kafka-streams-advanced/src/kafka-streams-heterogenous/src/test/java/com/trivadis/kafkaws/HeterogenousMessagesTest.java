package com.trivadis.kafkaws;

import com.trivadis.kafkaws.avro.v1.*;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class HeterogenousMessagesTest {

    private final static String TOPIC = "kstream-heterogenous-input-topic";
    private final static String BOOTSTRAP_SERVERS =
            "dataplatform:9092, dataplatform:9093, dataplatform:9094";
    private final static String SCHEMA_REGISTRY_URL = "http://dataplatform:8081";

    private final Producer producer = createProducer();

    private static Producer<String, SpecificRecord> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        //props.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "false");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);   // use constant for "schema.registry.url"
        props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class);
        return new KafkaProducer<>(props);
    }

    @Test
    public void testAlertV1() throws ExecutionException, InterruptedException {
        Alert alert = Alert.newBuilder()
                .setAlert("Alert Message v1")
                .setSeverity("HIGH")
                .build();
        AlertSentEvent alertSentEvent = AlertSentEvent.newBuilder()
                .setContext(Context.newBuilder().setId(UUID.randomUUID()).setWhen(Instant.now()).build())
                .setAlert(alert)
                .build();

        System.out.println(alertSentEvent);
        ProducerRecord<String, SpecificRecord> record = new ProducerRecord<>(TOPIC, alertSentEvent);
        producer.send(record).get();
    }

    @Test
    public void testNotifccationV1() throws ExecutionException, InterruptedException {
        Notification notification = Notification.newBuilder()
                .setMessage("Notification Message 1 - v1")
                .build();
        NotificationSentEvent notificationSentEvent = NotificationSentEvent.newBuilder()
                .setContext(Context.newBuilder().setId(UUID.randomUUID()).setWhen(Instant.now()).build())
                .setNotification(notification)
                .build();

        System.out.println(notificationSentEvent);
        ProducerRecord<String, SpecificRecord> record = new ProducerRecord<>(TOPIC, notificationSentEvent);
        producer.send(record).get();
    }

}
