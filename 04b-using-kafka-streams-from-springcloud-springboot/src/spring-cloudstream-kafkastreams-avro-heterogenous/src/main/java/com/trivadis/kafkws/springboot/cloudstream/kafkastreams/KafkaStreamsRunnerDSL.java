package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import com.trivadis.kafkaws.Notification;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class KafkaStreamsRunnerDSL {
    private final Log logger = LogFactory.getLog(getClass());

    @Bean
    public Function<KStream<Void, SpecificRecord>, KStream<Void, SpecificRecord>> process() {
        return input ->
        {
            // using peek() to write to debug
            input.peek((key, value) -> logger.info("(Input) " + value));


            // transform the values to upper case
            KStream<Void, SpecificRecord> upperStream = input.mapValues(value -> value);

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
