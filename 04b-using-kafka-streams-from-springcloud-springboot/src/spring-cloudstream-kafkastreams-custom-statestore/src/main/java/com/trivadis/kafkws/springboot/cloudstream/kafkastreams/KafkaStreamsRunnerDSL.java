package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class KafkaStreamsRunnerDSL {
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private MyProcessorSupplier myProcessorSupplier;

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> process() {
        return input ->
        {
            // using peek() to write to debug
            input.peek((key, value) -> logger.debug("(Input) " + value));

            // transform the values to upper case
            KStream<String, List<String>> customAggregatedStream = input.process(myProcessorSupplier, KafkaStreamsSpringCloudstreamApplication.MY_STATE_STORE);

            // using peek() to write to debug
            customAggregatedStream.peek((key,value) -> logger.debug("(After Custom State Store Aggregation) " + value));

            return customAggregatedStream.mapValues(v -> v.toString());
        };
    }

    @Bean
    public CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, true);
    }
}

