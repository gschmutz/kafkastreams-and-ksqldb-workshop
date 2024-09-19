package com.trivadis.kafkaws.kstream.countwindowed;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class MessageGenerator {

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "dataplatform:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);

        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "A", "AAA"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "B", "BBB"));

        Thread.sleep(1000);

        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "A", "AAA"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "C", "CCC"));

        Thread.sleep(1000);

        // produce 2 more records
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "A", "AAA"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "B", "BBB"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "A", "AAA"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "B", "BBB"));

        Thread.sleep(1000);

        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "A", "AAA"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", "C", "CCC"));

        // assuming 8 partitions, we produce a record in each of the 8 partitions to advance stream time
        Thread.sleep(60000);
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 0, "0", "000"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 1, "0", "000"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 2, "0", "000"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 3, "0", "000"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 4, "0", "000"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 5, "0", "000"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 6, "0", "000"));
        producer.send(new ProducerRecord<String, String>("test-kstream-input-topic", 7, "0", "000"));

        producer.close();

    }
}