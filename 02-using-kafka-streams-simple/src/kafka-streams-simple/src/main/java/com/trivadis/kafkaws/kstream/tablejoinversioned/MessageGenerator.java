package com.trivadis.kafkaws.kstream.tablejoinversioned;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.LocalDateTime;
import java.util.Properties;

public class MessageGenerator {

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "dataplatform:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<String, String>("test-kstream-compacted-topic", "A", "Entity A"));
        producer.send(new ProducerRecord<String, String>("test-kstream-compacted-topic", "B", "Entity B"));

        long timestamp = System.currentTimeMillis();

        Thread.sleep(1000);

        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "A", "AAA"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "B", "BBB"));

        // produce an update for A + B
        producer.send(new ProducerRecord<String, String>("test-kstream-compacted-topic", "A", "Object A"));
        producer.send(new ProducerRecord<String, String>("test-kstream-compacted-topic", "B", "Object B"));

        Thread.sleep(1000);

        // produce 2 more records
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "A", "AAA"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "B", "BBB"));

        Thread.sleep(1000);

        // produce with old timestamp (produces Entity B in join)
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", null, timestamp, "B", "BBB"));

        producer.close();

    }
}