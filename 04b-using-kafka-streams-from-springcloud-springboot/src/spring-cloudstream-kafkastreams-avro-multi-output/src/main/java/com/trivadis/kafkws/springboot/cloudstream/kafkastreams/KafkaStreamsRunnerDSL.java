package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import com.trivadis.kafkaws.avro.v1.MessageSentEvent;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class KafkaStreamsRunnerDSL {
    private final Log logger = LogFactory.getLog(getClass());

    @Bean
    public Function<KStream<Void, MessageSentEvent>, KStream<Void, SpecificRecord>[] > process() {

        Predicate<Void, SpecificRecord> isAlert = (k, v) ->


        return input ->
        {
            // using peek() to write to debug
            input.peek((key, value) -> logger.info("(Input) " + value));


            // transform the values to upper case
            KStream<Void, SpecificRecord> upperStream = input.flatMapValues(v -> new Arrays.asList)

            // using peek() to write to debug
            upperStream.peek((key,value) -> logger.debug("(After Transformation) " + value));

            final Map<Void, KStream<Void, SpecificRecord>> streamMap =

            return upperStream;
        };
    }

    @Bean
    public CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, true);
    }

}
