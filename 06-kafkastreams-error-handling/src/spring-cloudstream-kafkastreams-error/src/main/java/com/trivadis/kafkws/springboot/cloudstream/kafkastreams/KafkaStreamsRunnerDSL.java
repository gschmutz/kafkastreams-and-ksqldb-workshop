package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import io.streamthoughts.kc4streams.error.DLQProductionExceptionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer;
import org.springframework.cloud.stream.binder.kafka.utils.DlqPartitionFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaStreamsCustomizer;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class KafkaStreamsRunnerDSL {
    private final Log logger = LogFactory.getLog(getClass());

    @Bean
    public Function<KStream<Void, Long>, KStream<Void, String>> process() {
        return input -> input.mapValues(value -> {
            if (value > 10) {
                throw new RuntimeException("value is too large! > 10");
            } else {
                return String.valueOf(value * 2);
            }
        });
    }

    @Bean
    public DlqPartitionFunction partitionFunction() {
        return (group, record, ex) -> 0;
    }

    @Bean
    public StreamsBuilderFactoryBeanCustomizer customizer() {
        return fb -> {
            fb.getStreamsConfiguration().put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
                    DLQProductionExceptionHandler.class.getName());
        };
    }

    //@Bean
    public StreamsBuilderFactoryBeanCustomizer streamsBuilderFactoryBeanCustomizer() {
        return factoryBean -> {
            factoryBean.setKafkaStreamsCustomizer(new KafkaStreamsCustomizer() {
                @Override
                public void customize(KafkaStreams kafkaStreams) {

                    kafkaStreams.setUncaughtExceptionHandler(throwable -> {
                        logger.error("Exception occurred::", throwable);
                        return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
                    });

                }
            });
        };
    }
}
