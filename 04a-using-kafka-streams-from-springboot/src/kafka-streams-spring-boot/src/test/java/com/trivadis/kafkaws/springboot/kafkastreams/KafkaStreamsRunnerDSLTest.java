package com.trivadis.kafkaws.springboot.kafkastreams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.test.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaStreamsRunnerDSLTest {

    private TopologyTestDriver testDriver = null;
    private static final String INPUT_TOPIC = "test-kstream-springboot-input-topic";
    private static final String OUTPUT_TOPIC = "test-kstream-springboot-output-topic";
    private TestInputTopic inputTopic;
    private TestOutputTopic outputTopic;

    final Serde<String> stringSerde = Serdes.String();
    final Serde<Void> voidSerde = Serdes.Void();
    private static Properties getStreamsConfig(final Serde<?> keyDeserializer,
                                               final Serde<?> valueDeserializer) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, keyDeserializer.getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueDeserializer.getClass().getName());
        props.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());
        return props;
    }

    private void buildStreamProcessingPipeline(StreamsBuilder builder) {
        KStream<Void, String> input = builder.stream(INPUT_TOPIC, Consumed.with(voidSerde, stringSerde));
        KafkaStreamsRunnerDSL app = new KafkaStreamsRunnerDSL();

        app.buildPipeline(builder);
    }

    /**
     * Setup Stream topology
     * Add KStream based on @StreamListener annotation
     * Add to(topic) based @SendTo annotation
     */
    @BeforeEach
    public void setup() {
        final StreamsBuilder builder = new StreamsBuilder();
        buildStreamProcessingPipeline(builder);

        final Properties props = getStreamsConfig(Serdes.Void(), Serdes.String());
        testDriver = new TopologyTestDriver(builder.build(), props);
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC, voidSerde.serializer(), stringSerde.serializer());
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, voidSerde.deserializer(), stringSerde.deserializer());
    }

    @AfterEach
    public void tearDown() {
        try {
            testDriver.close();
        } catch (RuntimeException e) {
            // https://issues.apache.org/jira/browse/KAFKA-6647 causes exception when executed in Windows, ignoring it
            // Logged stacktrace cannot be avoided
            System.out.println("Ignoring exception, test failing in Windows due this exception:" + e.getLocalizedMessage());
        }
    }

    @Test
    void shouldUpperCaseOne() {
        final Void voidKey = null;
        String VALUE = "hello";

        inputTopic.pipeInput(voidKey, VALUE, 1L);

        final Object value = outputTopic.readValue();

        // assert that the output has a value in uppercase
        assertThat(value).isNotNull();
        assertThat(value).isEqualTo(VALUE.toUpperCase());

        // no more data in topic
        assertThat(outputTopic.isEmpty()).isTrue();
    }

    @Test
    void shouldUpperCaseMany() {
        final Void voidKey = null;
        String VALUE1 = "hello";
        String VALUE2 = "world";

        inputTopic.pipeInput(voidKey, VALUE1, 1L);
        inputTopic.pipeInput(voidKey, VALUE2, 2L);

        final List<KeyValue<Void,String>> values = outputTopic.readKeyValuesToList();

        // assert that the output has a value in uppercase
        assertThat(values).isNotNull();
        assertThat(values).contains(new KeyValue<>(voidKey, VALUE1.toUpperCase()));
        assertThat(values).contains(new KeyValue<>(voidKey, VALUE2.toUpperCase()));

        // no more data in topic
        assertThat(outputTopic.isEmpty()).isTrue();

    }

}
