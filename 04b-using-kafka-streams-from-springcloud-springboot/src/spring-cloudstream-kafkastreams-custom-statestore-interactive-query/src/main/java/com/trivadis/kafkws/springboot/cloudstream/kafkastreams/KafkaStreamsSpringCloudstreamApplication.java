package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;

@SpringBootApplication
public class KafkaStreamsSpringCloudstreamApplication {

	public static final String MY_STATE_STORE = "MyStateStore";

	@Bean
	public StoreBuilder myStore() {
		return Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(MY_STATE_STORE), Serdes.String(), Serdes.ListSerde(ArrayList.class, Serdes.String()));
	}

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsSpringCloudstreamApplication.class, args);
	}

}
