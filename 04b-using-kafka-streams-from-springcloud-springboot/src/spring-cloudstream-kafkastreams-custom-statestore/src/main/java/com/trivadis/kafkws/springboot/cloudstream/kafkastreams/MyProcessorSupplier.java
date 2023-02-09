package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.api.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class MyProcessorSupplier implements ProcessorSupplier<String, String, String, List<String>> {

    private StoreBuilder<?> storeBuilder;

    public MyProcessorSupplier(StoreBuilder<?> storeBuilder) {
        this.storeBuilder = storeBuilder;
    }

    @Override
    public Processor<String, String, String, List<String>> get() {
        return new MyProcessorSupplier.MyProcessor();
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return ProcessorSupplier.super.stores();
    }

    class MyProcessor extends ContextualProcessor<String, String, String, List<String>> {

        private KeyValueStore<String, List<String>> stateStore;

        @Override
        public void init(ProcessorContext<String, List<String>> context) {
            super.init(context);
            stateStore = (KeyValueStore) context.getStateStore(KafkaStreamsSpringCloudstreamApplication.MY_STATE_STORE);

            // context().schedule(Duration.ofMillis(2000), PunctuationType.WALL_CLOCK_TIME, timestamp -> flushOldWindow(timestamp));
        }

        @Override
        public void process(org.apache.kafka.streams.processor.api.Record<String, String> record) {
            if (stateStore.get(record.key()) == null) {
                stateStore.put(record.key(), Collections.singletonList(record.value()));
            } else {
                List entries = stateStore.get(record.key());
                entries.add(record.value());
                stateStore.put(record.key(), entries);
            }

            context().forward(new Record<String, List<String>>(record.key(), stateStore.get(record.key()), record.timestamp()));
        }

    }
}
