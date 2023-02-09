package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import com.trivadis.kafkaws.avro.v1.AlertSentEvent;
import com.trivadis.kafkaws.avro.v1.NotificationSentEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class KafkaStreamsSpringCloudstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsSpringCloudstreamApplication.class, args);
	}

	@Bean
	public Serde<SpecificRecord> avroSerde(){
		final SpecificAvroSerde<SpecificRecord> avroInSerde = new SpecificAvroSerde<>();
		Map<String, Object> serdeProperties = new HashMap<>();
		return avroInSerde;
	}

}
