package com.trivadis.kafkws.springboot.kafkastreamsspringcloudstream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.context.annotation.Bean;
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

            // using peek() to write to debug
            upperStream.peek((key,value) -> logger.debug("(After Transformation) " + value));

            return upperStream;
        };
    }
}
