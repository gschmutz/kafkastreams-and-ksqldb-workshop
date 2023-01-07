package com.trivadis.kafkaws.springboot.kafkastreams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
@EnableKafkaStreams
@Configuration
public class KafkaStreamsRunnerDSL {
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private StreamsBuilderFactoryBean factoryBean;

    private static final String INPUT_TOPIC_NAME = "test-kstream-springboot-input-topic";
    private static final String OUTPUT_TOPIC_NAME = "test-kstream-springboot-output-topic";

    @Bean
    public KStream buildPipeline (StreamsBuilder kStreamBuilder) {

        KStream<Void, String> stream = kStreamBuilder.stream(INPUT_TOPIC_NAME);

        // using peek() to write to debug
        stream.peek((key, value) -> logger.debug("(Input) " + value));

        // transform the values to upper case
        KStream<Void, String> upperStream = stream.mapValues(value -> value.toUpperCase());

        // using peek() to write to debug
        upperStream.peek((key,value) -> logger.debug("(After Transformation) " + value));

        upperStream.to(OUTPUT_TOPIC_NAME);
        return upperStream;
    }
}
