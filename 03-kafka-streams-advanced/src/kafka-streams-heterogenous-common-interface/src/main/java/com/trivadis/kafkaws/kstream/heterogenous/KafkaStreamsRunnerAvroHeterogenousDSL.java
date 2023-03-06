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

        // read from the source topic, "kstream-hetrogen-common-interface-input-topic"
        KStream<Void, ContextProvider> stream = builder.stream("kstream-hetrogen-common-interface-input-topic");

        // treat it as a ContextProvider
        stream.foreach(
                (key, value) -> {
                    ContextProvider contextProvider = (ContextProvider)value;
                    System.out.println("(ContextProvider) " + contextProvider.getContext());
                });

        // transform the values to upper case
        KStream<Void, Message> outputStream = stream.mapValues(value -> {
            Message message = null;
            if (value instanceof AlertSentEvent) {
                AlertSentEvent ase = (AlertSentEvent) value;
                message = Message.newBuilder()
                            .setMessage(ase.getAlert().getAlert().toString())
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

        outputStream.to("kstream-hetrogen-common-interface-output-topic");

        // set the required properties for running Kafka Streams
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "avro");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dataplatform:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Void().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde");
        config.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        config.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");

        // build the topology and start streaming
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
