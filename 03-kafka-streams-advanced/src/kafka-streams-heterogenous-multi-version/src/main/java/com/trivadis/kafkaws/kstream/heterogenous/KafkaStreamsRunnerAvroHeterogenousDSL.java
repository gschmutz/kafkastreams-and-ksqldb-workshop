package com.trivadis.kafkaws.kstream.heterogenous;

import com.trivadis.kafkaws.avro.v1.AlertSentEvent;
import com.trivadis.kafkaws.avro.v1.Message;
import com.trivadis.kafkaws.avro.v1.NotificationSentEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class KafkaStreamsRunnerAvroHeterogenousDSL {

    private static <VT extends SpecificRecord> SpecificAvroSerde<VT> createSerde(String schemaRegistryUrl) {
        SpecificAvroSerde<VT> serde = new SpecificAvroSerde<>();
        Map<String, String> serdeConfig = Collections
                .singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        serde.configure(serdeConfig, false);
        return serde;
    }

    public static void main(String[] args) {
        final String schemaRegistryUrl = "http://dataplatform:8081";
        //final SpecificAvroSerde<MyData> myDataSerde = createSerde(schemaRegistryUrl);

        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        // read from the source topic, "test-kstream-input-topic"
        KStream<Void, SpecificRecord> stream = builder.stream("test-kstream-input-topic");

        // for each record that appears in the source topic,
        // print the value
        stream.foreach(
                (key, value) -> {
                    System.out.println("(From Avro DSL) " + value);
                });

        // transform the values to upper case
        KStream<Void, SpecificRecord> outputStream = stream.mapValues(value -> {
            Message message = null;
            if (value instanceof AlertSentEvent) {
                AlertSentEvent ase = (AlertSentEvent) value;
                message = Message.newBuilder()
                        .setMessage(ase.getAlert().getAlert().toString())
                        .setContext(ase.getContext())
                        .build();
            } else if (value instanceof com.trivadis.kafkaws.avro.v2.AlertSentEvent) {
                    com.trivadis.kafkaws.avro.v2.AlertSentEvent ase = (com.trivadis.kafkaws.avro.v2.AlertSentEvent) value;
                    message = Message.newBuilder()
                            .setMessage(String.join(",", ase.getAlert().getAlerts()))
                            .setContext(ase.getContext())
                            .build();
            } else if (value instanceof NotificationSentEvent) {
                NotificationSentEvent nse = (NotificationSentEvent) value;
                message = Message.newBuilder()
                        .setMessage(nse.getNotification().getMessage().toString())
                        .setContext(nse.getContext())
                        .build();
            }
            return message;
        });

        outputStream.to("test-kstream-output-topic");

        // set the required properties for running Kafka Streams
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "avro");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dataplatform:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Void().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde");
        config.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        config.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, io.confluent.kafka.serializers.subject.TopicRecordNameStrategy.class);

        // build the topology and start streaming
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
