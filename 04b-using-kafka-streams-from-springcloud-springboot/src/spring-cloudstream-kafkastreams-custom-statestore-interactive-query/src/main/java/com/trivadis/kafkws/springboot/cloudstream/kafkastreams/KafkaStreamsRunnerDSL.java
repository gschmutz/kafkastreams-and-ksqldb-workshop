package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class KafkaStreamsRunnerDSL {
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private MyProcessorSupplier myProcessorSupplier;

    @Autowired
    private InteractiveQueryService queryService;

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> collect() {
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
    public BiFunction<KStream<String, String>, KTable<String,String>, KStream<String, String>> enrich() {
//        final ReadOnlyKeyValueStore<String, List<String>> stateStore = queryService.getQueryableStore(KafkaStreamsSpringCloudstreamApplication.MY_STATE_STORE, QueryableStoreTypes.keyValueStore());

        return (input, table) -> (
                input.leftJoin(table, (s, t) -> t, Joined.with(Serdes.String(), Serdes.String(), null))
                );
    }


    @Bean
    public CleanupConfig cleanupConfig() {
        return new CleanupConfig(false, true);
    }
}

