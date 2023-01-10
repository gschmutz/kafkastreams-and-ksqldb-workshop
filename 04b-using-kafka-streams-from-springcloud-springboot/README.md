# Kafka Streams with Spring Boot & Spring Cloud Stream

In this workshop we will learn how to process messages using the [Kafka Streams Binder of Spring Cloud Stream](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-kafka.html#_kafka_streams_binder) from Spring Boot. 

We will create a Spring Boot application and implement the same basic processor from Workshop 8 which consumes messages from a topic, processes them and produces the result into a new topic. We will be using the High-Level DSL.

## Create the Spring Boot Project

First, let’s navigate to [Spring Initializr](https://start.spring.io/) to generate our project. Our project will need the Apache Kafka support. 

Select Generate a **Maven Project** with **Java** and Spring Boot **3.0.1**. Enter `com.trivadis.kafkaws.springboot` for the **Group**, `spring-cloudstream-kafkastreams` for the **Artifact** field, `Kafka Streams project with Spring Boot and Cloud Stream` for the **Description** field and adapt the **Package Name** to `com.trivadis.kafkaws.springboot.cloudstream.kafkastreams`. 

![Alt Image Text](./images/spring-initializr.png "Spring Initializr")

Click on **Add Dependencies** and search for the  **Cloud Stream** dependency. Select the dependency and hit the **Enter** key. Click on **Add Dependencies** once more and search for the  **Spring for Apache Kafka Streams** dependency and add it as well.
You should now see the dependency on the right side of the screen.

![Alt Image Text](./images/spring-initializr-with-kafka-dep.png "Spring Initializr")

Click on **Generate Project** and unzip the ZIP file to a convenient location for development. Once you have unzipped the project, you’ll have a very simple structure. 

Import the project as a Maven Project into your favourite IDE for further development. 

## Create the necessary Kafka Topic 

We will use the topic `test-kstream-spring-input-topic` and `test-kstream-spring-output-topic` in the KafkaStream processorcode below. Due to the fact that `auto.topic.create.enable` is set to `false`, we have to manually create the topic. 

Connect to the `kafka-1` container and execute the necessary kafka-topics command. 

```bash
docker exec -ti kafka-1 kafka-topics --create \
    --replication-factor 3 \
    --partitions 8 \
    --topic test-kstream-spring-cloudstream-input-topic \
    --bootstrap-server kafka-1:19092
    
docker exec -ti kafka-1 kafka-topics --create \
    --replication-factor 3 \
    --partitions 8 \
    --topic test-kstream-spring-cloudstream-output-topic \
    --bootstrap-server kafka-1:19092
```

Next we will implement the KafkaStreams Processor Topology using the DSL and second using the Processor API.

## Implementing the Kafka Streams Processor using the DSL

Create a new Java package `com.trivads.kafkaws.springboot.kafkastreamsspringcloudstream` and in it a Java class `KafkaStreamsRunnerDSL`. 

Add the following code for the implementation

```java
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
```

In standard Spring Boot way, you can see that using Kafka Streams is much simpler than with plain Java. But of course the configuration is still missing. The necessary settings will go into the `application.yml` file.

### Configure Kafka through application.yml configuration file

First let's rename the existing `application.properties` file to `application.yml` to use the `yml` format. 

Add the following settings to configure the Kafka cluster as well as the Kafka Streams application:

```yml
spring:
  application:
    name: "spring-boot-kafkastreams"

  cloud:
    stream:
      bindings:
        process-in-0:
          destination: test-kstream-spring-cloudstream-input-topic
        process-out-0:
          destination: test-kstream-spring-cloudstream-output-topic

      kafka:
        streams.binder:
          applicationId: spring-boot-springcloud-kafkastreams
          configuration:
            commit.interval.ms: 100
            cache.max.bytes.buffering: 0
            default.key.serde: org.apache.kafka.common.serialization.Serdes$VoidSerde
            default.value.serde: org.apache.kafka.common.serialization.Serdes$StringSerde

  kafka:
    bootstrap-servers: dataplatform:9092

logging:
  level:
    root: info
    com.trivadis.kafkws.springboot.kafkastreamsspringcloudstream: debug
```

For the IP address of the Kafka cluster we refer to an environment variable, which we have to declare before running the application.

```bash
export DATAPLATFORM_IP=nnn.nnn.nnn.nnn
```

## Build & Run the application

First lets build the application:

```bash
mvn package -Dmaven.test.skip=true
```

Now let's run the application

```bash
mvn spring-boot:run
```

## Use Console to test the application

Start the program and then first run a `kcat` consumer on the output topic

```bash
kcat -b dataplatform:9092 -t test-kstream-spring-cloudstream-output-topic
```

with that in place, in 2nd terminal produce some messages using `kcat` in producer mode on the input topic

```bash
kcat -b dataplatform:9092 -t test-kstream-spring-cloudstream-input-topic -P
```

All the values produced should arrive on the consumer in uppercase.

## Unit Testing Kafka Streams using `TopologyTestDriver`

Now let's add a unit test using the `kafka-streams-test-utils` library which is part of the [Apache Kafka](https://kafka.apache.org/33/documentation/streams/developer-guide/testing.html).

The test-utils package provides a `TopologyTestDriver` that can be used pipe data through a Topology that is either assembled manually using Processor API or via the DSL using StreamsBuilder. The test driver simulates the library runtime that continuously fetches records from input topics and processes them by traversing the topology. This way we can test the topology without having to run the Spring Boot application and without connectivity to a Kafka cluster.

Add the following two dependencies to the `pom.xml` 

```xml
		<dependency>
			<groupId>org.springframework.kafka</groupId>
			<artifactId>spring-kafka-test</artifactId>
			<scope>test</scope>
		</dependency>
		
		<dependency>
			<groupId>org.apache.kafka</groupId>
			<artifactId>kafka-streams-test-utils</artifactId>
			<version>${kafka.version}</version>
			<scope>test</scope>
		</dependency>
```

Now let's implement the unit test 

```java
package com.trivadis.kafkws.springboot.kafkastreamsspringcloudstream;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.test.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaStreamsRunnerDSLTest {

    private TopologyTestDriver testDriver;
    private static final String INPUT_TOPIC = "input-topic";
    private static final String OUTPUT_TOPIC = "output-topic";;
    private TestInputTopic inputTopic;
    private TestOutputTopic outputTopic;

    final Serde<String> stringSerde = Serdes.String();
    final Serde<Void> voidSerde = Serdes.Void();


    private static Properties getStreamsConfig(final Serde<?> keyDeserializer,
                                              final Serde<?> valueDeserializer) {
    final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, keyDeserializer.getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueDeserializer.getClass().getName());
        props.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());
        return props;
    }

    private void buildStreamProcessingPipeline(StreamsBuilder builder) {
        KStream<Void, String> input = builder.stream(INPUT_TOPIC, Consumed.with(voidSerde, stringSerde));
        KafkaStreamsRunnerDSL app = new KafkaStreamsRunnerDSL();
        final Function<KStream<Void, String>, KStream<Void, String>> process = app.process();
        final KStream<Void, String> output = process.apply(input);
        output.to(OUTPUT_TOPIC, Produced.with(voidSerde, stringSerde));
    }

    /**
     * Setup Stream topology
     * Add KStream based on @StreamListener annotation
     * Add to(topic) based @SendTo annotation
     */
    @BeforeEach
    public void setup() {
        final StreamsBuilder builder = new StreamsBuilder();
        buildStreamProcessingPipeline(builder);

        final Properties props = getStreamsConfig(Serdes.Void(), Serdes.String());
        testDriver = new TopologyTestDriver(builder.build(), props);
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC, voidSerde.serializer(), stringSerde.serializer());
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, voidSerde.deserializer(), stringSerde.deserializer());
    }


    @AfterEach
    public void tearDown() {
        try {
            testDriver.close();
        } catch (RuntimeException e) {
            // https://issues.apache.org/jira/browse/KAFKA-6647 causes exception when executed in Windows, ignoring it
            // Logged stacktrace cannot be avoided
            System.out.println("Ignoring exception, test failing in Windows due this exception:" + e.getLocalizedMessage());
        }
    }

    @Test
    void shouldUpperCaseOne() {
        final Void voidKey = null;
        String VALUE = "hello";

        inputTopic.pipeInput(voidKey, VALUE, 1L);

        final Object value = outputTopic.readValue();

        // assert that the output has a value in uppercase
        assertThat(value).isNotNull();
        assertThat(value).isEqualTo(VALUE.toUpperCase());

        // no more data in topic
        assertThat(outputTopic.isEmpty()).isTrue();
    }

    @Test
    void shouldUpperCaseMany() {
        final Void voidKey = null;
        String VALUE1 = "hello";
        String VALUE2 = "world";

        inputTopic.pipeInput(voidKey, VALUE1, 1L);
        inputTopic.pipeInput(voidKey, VALUE2, 2L);

        final List<KeyValue<Void,String>> values = outputTopic.readKeyValuesToList();

        // assert that the output has a value in uppercase
        assertThat(values).isNotNull();
        assertThat(values).contains(new KeyValue<>(voidKey, VALUE1.toUpperCase()));
        assertThat(values).contains(new KeyValue<>(voidKey, VALUE2.toUpperCase()));

        // no more data in topic
        assertThat(outputTopic.isEmpty()).isTrue();

    }
}
```

We also have to configure the logging. Create a file `test/resources/logback.xml` and add the following

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{ISO8601} %5p %t %c{2}:%L - %m%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="stdout"/>
    </root>
    <logger name="org.apache.kafka.streams.processor.internals" level="WARN"/>
</configuration>
```

Now run the unit test either from the Java IDE or by using `mvn test`. 


## Adding Health Indicator

The health indicator requires the dependency spring-boot-starter-actuator (see [here](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints) for the current documentation of Spring Boot Actuator)

Add the following dependency to the `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

If you prefer using webflux, you can then include `spring-boot-starter-webflux` instead of the standard web dependency.

By default, all endpoints except for `shutdown` are enabled but only the `health` endpoint is exposed over HTTP and JMX.

(Re)Start the service and navigate to <http://localhost:8080/actuator/health> to see the health indicator of the service:

```json
{"status":"UP"}
```

By default only the global status is visible (UP or DOWN). To show the details, the property `management.endpoint.health.show-details` must be set to `ALWAYS` or `WHEN_AUTHORIZED`

```yml
management:
  endpoint:
    health.show-details: ALWAYS
```

(Re)Start the service and navigate to <http://localhost:8080/actuator/health> to see the health indicator with the additional details: 

```json
{
   "status":"UP",
   "components":{
      "binders":{
         "status":"UP",
         "components":{
            "kstream":{
               "status":"UP",
               "details":{
                  "spring-boot-springcloud-kafkastreams":{
                     "threadDetails":{
                        "spring-boot-springcloud-kafkastreams-f534264a-cbae-4077-9341-974e4baecc53-StreamThread-1":{
                           "adminClientId":"spring-boot-springcloud-kafkastreams-f534264a-cbae-4077-9341-974e4baecc53-admin",
                           "restoreConsumerClientId":"spring-boot-springcloud-kafkastreams-f534264a-cbae-4077-9341-974e4baecc53-StreamThread-1-restore-consumer",
                           "threadState":"RUNNING",
                           "producerClientIds":[
                              "spring-boot-springcloud-kafkastreams-f534264a-cbae-4077-9341-974e4baecc53-StreamThread-1-producer"
                           ],
                           "standbyTasks":{
                              
                           },
                           "activeTasks":{
                              "partitions":[
                                 "partition=0, topic=test-kstream-spring-cloudstream-input-topic"
                              ],
                              "taskId":{
                                 "topicGroupId":0,
                                 "partition":0
                              }
                           },
                           "consumerClientId":"spring-boot-springcloud-kafkastreams-f534264a-cbae-4077-9341-974e4baecc53-StreamThread-1-consumer",
                           "threadName":"spring-boot-springcloud-kafkastreams-f534264a-cbae-4077-9341-974e4baecc53-StreamThread-1"
                        }
                     }
                  }
               }
            }
         }
      },
      "diskSpace":{
         "status":"UP",
         "details":{
            "total":494384795648,
            "free":39005593600,
            "threshold":10485760,
            "path":"/Users/guido.schmutz/Documents/GitHub/gschmutz/kafkastreams-and-ksqldb-workshop/04b-using-kafka-streams-from-springcloud-springboot/src/kafka-streams-spring-cloudstream/.",
            "exists":true
         }
      },
      "ping":{
         "status":"UP"
      }
   }
}
```

### Customize Prefix

Actuator allows for customizing the prefix for the management endpoints. So if you don't like the `/actuator` prefix, you can use the `management.endpoints.web.base-path` property to change it, i.e. to `management`

```yml
management:
  endpoints:
    web:
      base-path: "/management"
```

## Kafka Streams Metrics

Spring Cloud Stream Kafka Streams binder provides Kafka Streams metrics which can be exported through a Micrometer MeterRegistry.

First add metrics to the property `management.endpoints.web.exposure.include`:

```yml
management:
  endpoint:
    health.show-details: ALWAYS

  endpoints:
    web:
      exposure:
        include: metrics
```

You can also use * to expose all of the Actuator endpoints.

Now navigate to <http://localhost:8080/actuator/metrics> to get a list of all the available metrics or access them individually using the URI with the metric name (`/actuator/metrics/<meteric-name>`), e.g. using

<http://localhost:8080/actuator/metrics/kafka.consumer.fetch.manager.records.consumed.total>

you get an output similar to that (will be `0` if you haven't processed any messages yet)

```json
{
   "name":"kafka.consumer.fetch.manager.records.consumed.total",
   "description":"The total number of records consumed for a topic",
   "measurements":[
      {
         "statistic":"COUNT",
         "value":2.0
      }
   ],
   "availableTags":[
      {
         "tag":"topic",
         "values":[
            "test-kstream-spring-cloudstream-input-topic"
         ]
      },
      {
         "tag":"spring.id",
         "values":[
            "stream-builder-process"
         ]
      },
      {
         "tag":"kafka.version",
         "values":[
            "3.3.1"
         ]
      },
      {
         "tag":"client.id",
         "values":[
            "spring-boot-springcloud-kafkastreams-168c3229-13c1-4ce5-89dc-f13e17fa7b53-StreamThread-1-consumer"
         ]
      }
   ]
}
```

## Add Kafka Streams Topology visualization

Kafka Streams binder provides the following actuator endpoints for retrieving the topology description using which you can visualise the topology using external tools.

`/actuator/kafkastreamstopology`

`/actuator/kafkastreamstopology/<application-id of the processor>`


Add `kafkastreamstopology` to the `management.endpoints.web.exposure.include property`. 

```yml
management:
  endpoint:
    health.show-details: ALWAYS

  endpoints:
    web:
      exposure:
        include: metrics, kafkastreamstopology
```

(Re)Start the service and navigate to <http://localhost:8080/actuator/kafkastreamstopology> to see the topology. 

```json
["Topologies:\n   Sub-topology: 0\n    Source: KSTREAM-SOURCE-0000000000 (topics: [test-kstream-spring-cloudstream-input-topic])\n      --> KSTREAM-MAPVALUES-0000000002, KSTREAM-PEEK-0000000001\n    Processor: KSTREAM-MAPVALUES-0000000002 (stores: [])\n      --> KSTREAM-PEEK-0000000003, KSTREAM-SINK-0000000004\n      <-- KSTREAM-SOURCE-0000000000\n    Processor: KSTREAM-PEEK-0000000001 (stores: [])\n      --> none\n      <-- KSTREAM-SOURCE-0000000000\n    Processor: KSTREAM-PEEK-0000000003 (stores: [])\n      --> none\n      <-- KSTREAM-MAPVALUES-0000000002\n    Sink: KSTREAM-SINK-0000000004 (topic: test-kstream-spring-cloudstream-output-topic)\n      <-- KSTREAM-MAPVALUES-0000000002\n\n"]
```

Use curl with jq and sed to replace the newline characters (`\n`) with "real" newlines 

`curl localhost:8080/actuator/kafkastreamstopology | jq . | sed 's/\\n/\n/g'`

to make it more readable


```
[
  "Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [test-kstream-spring-cloudstream-input-topic])
      --> KSTREAM-MAPVALUES-0000000002, KSTREAM-PEEK-0000000001
    Processor: KSTREAM-MAPVALUES-0000000002 (stores: [])
      --> KSTREAM-PEEK-0000000003, KSTREAM-SINK-0000000004
      <-- KSTREAM-SOURCE-0000000000
    Processor: KSTREAM-PEEK-0000000001 (stores: [])
      --> none
      <-- KSTREAM-SOURCE-0000000000
    Processor: KSTREAM-PEEK-0000000003 (stores: [])
      --> none
      <-- KSTREAM-MAPVALUES-0000000002
    Sink: KSTREAM-SINK-0000000004 (topic: test-kstream-spring-cloudstream-output-topic)
      <-- KSTREAM-MAPVALUES-0000000002

"
]
```

You can now use <https://zz85.github.io/kafka-streams-viz/> or <https://gaetancollaud.github.io/kafka-streams-visualization/> to render a graphical visualization of the topology.

![](./images/kafkastreamstopology-vis.png)


## State Cleanup

By default, no local state is cleaned up when the binding is stopped.

To modify this behaviour simply add a single `CleanupConfig` `@Bean` to the application context:

```
    @Bean
    public CleanupConfig cleanupConfig() {
        // do not cleanup on start (false), but cleanup on stop (true)
        return new CleanupConfig(false, true);
    }
```