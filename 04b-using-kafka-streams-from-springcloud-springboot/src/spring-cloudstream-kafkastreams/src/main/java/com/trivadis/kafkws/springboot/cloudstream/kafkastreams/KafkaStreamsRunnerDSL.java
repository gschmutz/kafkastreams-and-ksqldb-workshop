package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class KafkaStreamsRunnerDSL {
    private final Log logger = LogFactory.getLog(getClass());

    @Bean
    public Function<KStream<Void, String>, KStream<Void, String>> process() {
        return input ->
        {
            // using peek() to write to debug
            input.peek((key, value) -> logger.debug("(Input) " + value));

            // transform the values to upper case
            KStream<Void, String> upperStream = input.mapValues(value -> value.toUpperCase());

            KGroupedStream<String,String> groupedByKey = upperStream.groupBy((key, value) -> value.toString());
            KTable<String, Long> counts = groupedByKey.count(Materialized.as("count"));


            // using peek() to write to debug
            upperStream.peek((key,value) -> logger.debug("(After Transformation) " + value));

            return upperStream;
        };
    }

    @Bean
    public CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, true);
    }

}
