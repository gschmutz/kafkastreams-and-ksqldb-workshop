# Reset Kafka Streams Application

In this workshop we will learn how to reset a Kafka Stream Application using the `kafka-streams-application-reset` command line utility.

We will reuse a basic processor which consumes messages from a topic, processes them and produces the result into a new topic. 

## Create the project in your Java IDE

In your favorite IDE, create a new Maven project and use `com.trivadis.kafkaws` for the **Group Id** and `kafka-streams-simple` for the **Artifact Id**.

Navigate to the **pom.xml** and double-click on it. The POM Editor will be displayed. 

Let's add some initial dependencies for our project. We will add some more dependencies to the POM throughout this workshop.

First add the following property into the already existing `<properties>` section


```xml
   <properties>
       ...
       <confluent.version>6.2.0</confluent.version>
       <kafka.version>2.8.0</kafka.version>
       <slf4j.version>1.7.32</slf4j.version>
    </properties>
```

Copy the following block right after the `<properties>` tag, before the closing `</project>` tag.
    
```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-streams</artifactId>
            <version>${kafka.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>${slf4j.version}</version>
        </dependency>        
    </dependencies>
    
	<build>
		<defaultGoal>install</defaultGoal>

		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>2.5</version>
				<configuration>
					<source>15</source>
					<target>15</target>
					<maxmem>256M</maxmem>
					<showDeprecation>true</showDeprecation>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>exec-maven-plugin</artifactId>
				<version>3.0.0</version>
				<executions>
					<execution>
						<id>producer</id>
						<goals>
							<goal>java</goal>
						</goals>
						<configuration>
							<mainClass>com.trivadis.kafkaws.processor.KafkaStreamsRunner</mainClass>
						</configuration>						
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```

## Create a log4j.properties file

In the `resources` folder, create a `log4j.properties` file and add the following content:

```properties
log4j.rootLogger=WARN, stdout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=[%d] %p [%t] %m (%c)%n

# Enable for debugging if need be
#log4j.logger.org.apache.kafka.streams=DEBUG, stdout
#log4j.additivity.org.apache.kafka.streams=false

# Squelch expected error messages like:
#     java.lang.IllegalStateException: This consumer has already been closed.
log4j.logger.org.apache.kafka.streams.processor.internals.StreamThread=WARN, stdout
log4j.additivity.org.apache.kafka.streams.processor.internals.StreamThread=false

#Enable info for Microservices
log4j.logger.io.confluent.examples.streams.microservices=INFO, stdout
log4j.additivity.io.confluent.examples.streams.microservices=false

# Enable for debugging if need be
#log4j.logger.io.confluent=DEBUG, stdout
#log4j.additivity.io.confluent=false
```

## Creating the necessary Kafka Topic 

We will use the topics `test-kstream-input-topic` and `test-kstream-output-topic` as the input/output topics. 

Due to the fact that `auto.topic.create.enable` is set to `false`, we have to manually create the topic. 

Connect to the `kafka-1` container and execute the following `kafka-topics` command

```bash
docker exec -ti kafka-1 kafka-topics --create \
    --replication-factor 3 \
    --partitions 8 \
    --topic test-kstream-input-topic \
    --bootstrap-server kafka-1:19092

docker exec -ti kafka-1 kafka-topics --create \
    --replication-factor 3 \
    --partitions 8 \
    --topic test-kstream-output-topic \
    --bootstrap-server kafka-1:19092
```

This finishes the setup steps and our new project is ready to be used. Next we will implement the KafkaStreams Processor Topology, first using the DSL and second using the Processor API.

## Implementing the Kafka Streams Processor using the DSL

Create a new Java package `com.trivadis.kafkaws.kstream.simple` and in it a Java class `KafkaStreamsRunnerDSL`. 

Add the following code for the implementation

```java
package com.trivadis.kafkaws.kstream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Properties;

public class KafkaStreamsRunnerCountWindowedDSL {

    public static void main(String[] args) {
        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        // read from the source topic, "test-kstream-input-topic"
        KStream<String, String> stream = builder.stream("test-kstream-input-topic");

        // create a tumbling window of 60 seconds
        TimeWindows tumblingWindow =
                TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(60));

        KTable<Windowed<String>, Long> counts = stream.groupByKey()
                .windowedBy(tumblingWindow)
                .count(Materialized.as("countWindowed"));

        counts.toStream().print(Printed.<Windowed<String>, Long>toSysOut().withLabel("counts"));

        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();
        counts.toStream( (wk,v) -> wk.key() + " : " + wk.window().startTime().atZone(ZoneId.of("Europe/Zurich")) + " to " + wk.window().endTime().atZone(ZoneId.of("Europe/Zurich")) + " : " + v)
                .to("test-kstream-output-topic", Produced.with(stringSerde, longSerde));

        // set the required properties for running Kafka Streams
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "applicationResetSample");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dataplatform:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        // build the topology and start streaming
        Topology topology = builder.build();
        System.out.println(topology.describe());
        KafkaStreams streams = new KafkaStreams(topology, config);

        // Delete the application's local state on reset
        if (args.length > 0 && args[0].equals("--reset")) {
            streams.cleanUp();
        }

        streams.start();

        // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
```

Start the program 

```bash
mvn exec:java@producer
```

Then first run a `kcat` consumer on the output topic

```bash
kcat -b dataplatform:9092 -t test-kstream-output-topic
```

with that in place, in 2nd terminal produce some messages using `kcat` in producer mode on the input topic

```bash
kcat -b dataplatform:9092 -t test-kstream-input-topic -P -K , 
```

produce some values with `<key>,<value>` syntax, such as

```bash
A,AAA
B,BBB
C,CCC
A,AAA
D,DDD
E,EEE
A,AAA
```

Now let's reset the application using the `kafka-streams-application-reset` tool.

## Reset Application

You can reset an application and force it to reprocess its data from scratch by using the application reset tool. This can be useful for development and testing, or when fixing bugs.

The application reset tool handles the Kafka Streams user topics (input, output, and intermediate topics) and internal topics differently when resetting the application.

* **Input topics:** Reset offsets to specified position. By default they are reset to the beginning of the topic.
* **Intermediate topics:** Skip to the end of the topic, i.e., set the application’s committed consumer offsets for all partitions to each partition’s logSize (for consumer group application.id).
* **Internal topics:** Delete the internal topic (this automatically deletes any committed offsets).

The application reset tool does not:

* **Reset output topics of an application:** if any output (or intermediate) topics are consumed by downstream applications, it is your responsibility to adjust those downstream applications as appropriate when you reset the upstream application.
* **Reset the local environment of your application instances:**  it is your responsibility to delete the local state on any machine on which an application instance was run. 
* **Delete schemas in Schema Registry for internal topics:** you must delete schemas for internal topics manually if you reset an app that uses Schema Registry. 

Let's see the help page of the `kafka-streams-application-reset` tool

```bash
docker exec -ti kafka-1 kafka-streams-application-reset

[2023-04-05 20:02:42,967] INFO Registered kafka:type=kafka.Log4jController MBean (kafka.utils.Log4jControllerRegistration$)
Missing required option(s) [application-id]
Option (* = required)                 Description
---------------------                 -----------
--application-id <String: id>       The Kafka Streams application ID
                                        (application.id).
--bootstrap-servers <String: urls>    Comma-separated list of broker urls with
                                        format: HOST1:PORT1,HOST2:PORT2
                                        (default: localhost:9092)
--by-duration <String>                Reset offsets to offset by duration from
                                        current timestamp. Format: 'PnDTnHnMnS'
--config-file <String: file name>     Property file containing configs to be
                                        passed to admin clients and embedded
                                        consumer.
--dry-run                             Display the actions that would be
                                        performed without executing the reset
                                        commands.
--force                               Force the removal of members of the
                                        consumer group (intended to remove
                                        stopped members if a long session
                                        timeout was used). Make sure to shut
                                        down all stream applications when this
                                        option is specified to avoid unexpected
                                        rebalances.
--from-file <String>                  Reset offsets to values defined in CSV
                                        file.
--help                                Print usage information.
--input-topics <String: list>         Comma-separated list of user input
                                        topics. For these topics, the tool by
                                        default will reset the offset to the
                                        earliest available offset. Reset to
                                        other offset position by appending
                                        other reset offset option, ex: --input-
                                        topics foo --shift-by 5
--intermediate-topics <String: list>  Comma-separated list of intermediate user
                                        topics (topics that are input and
                                        output topics, e.g., used in the
                                        deprecated through() method). For these
                                        topics, the tool will skip to the end.
--internal-topics <String: list>      Comma-separated list of internal topics
                                        to delete. Must be a subset of the
                                        internal topics marked for deletion by
                                        the default behaviour (do a dry-run
                                        without this option to view these
                                        topics).
--shift-by <Long: number-of-offsets>  Reset offsets shifting current offset by
                                        'n', where 'n' can be positive or
                                        negative
--to-datetime <String>                Reset offsets to offset from datetime.
                                        Format: 'YYYY-MM-DDTHH:mm:SS.sss'
--to-earliest                         Reset offsets to earliest offset.
--to-latest                           Reset offsets to latest offset.
--to-offset <Long>                    Reset offsets to a specific offset.
--version                             Print version information and exit.
```

### Reset to Earliest

Let's reset the application we run before to the earliest offset. We first use the `--dry-run` option to only display the actions that would be performed without executing the reset commands.

```bash
docker exec -ti kafka-1 kafka-streams-application-reset --application-id applicationResetSample --bootstrap-servers kafka-1:19092 --input-topics test-kstream-input-topic --to-earliest --dry-run 
```

we should see an output similar to the one below

```bash
[2023-04-18 18:10:13,448] INFO Kafka version: 7.3.1-ccs (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:10:13,448] INFO Kafka commitId: 8628b0341c3c46766f141043367cc0052f75b090 (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:10:13,448] INFO Kafka startTimeMs: 1681841413445 (org.apache.kafka.common.utils.AppInfoParser)
ERROR: java.lang.IllegalStateException: Consumer group 'applicationResetSample' is still active and has following members: [(memberId=applicationResetSample-9ec315a1-76b4-4779-98c8-6658a8c4e12b-StreamThread-1-consumer-84806732-ba4d-45c6-9125-f8820cd06491, groupInstanceId=null, clientId=applicationResetSample-9ec315a1-76b4-4779-98c8-6658a8c4e12b-StreamThread-1-consumer, host=/172.18.0.1, assignment=(topicPartitions=test-kstream-input-topic-1,test-kstream-input-topic-2,test-kstream-input-topic-0))]. Make sure to stop all running application instances before running the reset tool. You can use option '--force' to remove active members from the group.
java.lang.IllegalStateException: Consumer group 'applicationResetSample' is still active and has following members: [(memberId=applicationResetSample-9ec315a1-76b4-4779-98c8-6658a8c4e12b-StreamThread-1-consumer-84806732-ba4d-45c6-9125-f8820cd06491, groupInstanceId=null, clientId=applicationResetSample-9ec315a1-76b4-4779-98c8-6658a8c4e12b-StreamThread-1-consumer, host=/172.18.0.1, assignment=(topicPartitions=test-kstream-input-topic-1,test-kstream-input-topic-2,test-kstream-input-topic-0))]. Make sure to stop all running application instances before running the reset tool. You can use option '--force' to remove active members from the group.
	at kafka.tools.StreamsResetter.maybeDeleteActiveConsumers(StreamsResetter.java:201)
	at kafka.tools.StreamsResetter.run(StreamsResetter.java:161)
	at kafka.tools.StreamsResetter.run(StreamsResetter.java:140)
	at kafka.tools.StreamsResetter.main(StreamsResetter.java:695)
[2023-04-18 18:10:13,659] INFO App info kafka.admin.client for adminclient-1 unregistered (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:10:13,664] INFO Metrics scheduler closed (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:10:13,664] INFO Closing reporter org.apache.kafka.common.metrics.JmxReporter (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:10:13,664] INFO Metrics reporters closed (org.apache.kafka.common.metrics.Metrics)
```

we can see that it complains that the application is still running. We have to stop all instances to avoid inconsistencies. Let's stop the application and then re-try the same command.

```bash
docker exec -ti kafka-1 kafka-streams-application-reset --application-id applicationResetSample --bootstrap-servers kafka-1:19092 --input-topics test-kstream-input-topic --to-earliest --dry-run 
```

now we can see that all 8 partitions will be reset to offset 0: 

```bash
----Dry run displays the actions which will be performed when running Streams Reset Tool----
Reset-offsets for input topics [test-kstream-input-topic]

[2023-04-18 18:20:08,461] INFO Kafka version: 7.3.1-ccs (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:20:08,461] INFO Kafka commitId: 8628b0341c3c46766f141043367cc0052f75b090 (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:20:08,461] INFO Kafka startTimeMs: 1681842008461 (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:20:08,469] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Cluster ID: 5XHMHrp5TQ63BZMpW5v4FA (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,471] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Assigned to partition(s): test-kstream-input-topic-0, test-kstream-input-topic-5, test-kstream-input-topic-4, test-kstream-input-topic-1, test-kstream-input-topic-6, test-kstream-input-topic-7, test-kstream-input-topic-2, test-kstream-input-topic-3 (org.apache.kafka.clients.consumer.KafkaConsumer)
Following input topics offsets will be reset to (for consumer group applicationResetSample)
[2023-04-18 18:20:08,473] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-1 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,473] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-2 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,473] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-3 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,473] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-4 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,473] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-5 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,473] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-6 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,474] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-7 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,474] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Seeking to earliest offset of partition test-kstream-input-topic-0 (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,481] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-0 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,481] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-5 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,481] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-4 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,482] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-1 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,482] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-6 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,482] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-7 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,482] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-2 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,482] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting the last seen epoch of partition test-kstream-input-topic-3 to 0 since the associated topicId changed from null to MOPWrXtNTJqfkpxkyPzDXg (org.apache.kafka.clients.Metadata)
[2023-04-18 18:20:08,487] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-1 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-3:19094 (id: 3 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,487] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-4 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-3:19094 (id: 3 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,488] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-7 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-3:19094 (id: 3 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
Topic: test-kstream-input-topic Partition: 1 Offset: 0
[2023-04-18 18:20:08,491] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-2 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-1:19092 (id: 1 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,491] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-5 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-1:19092 (id: 1 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,492] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-3 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-2:19093 (id: 2 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,492] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-6 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-2:19093 (id: 2 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
[2023-04-18 18:20:08,492] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-0 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-2:19093 (id: 2 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
Topic: test-kstream-input-topic Partition: 2 Offset: 0
Topic: test-kstream-input-topic Partition: 3 Offset: 0
Topic: test-kstream-input-topic Partition: 4 Offset: 0
Topic: test-kstream-input-topic Partition: 5 Offset: 0
Topic: test-kstream-input-topic Partition: 6 Offset: 0
Topic: test-kstream-input-topic Partition: 7 Offset: 0
Topic: test-kstream-input-topic Partition: 0 Offset: 0
[2023-04-18 18:20:08,492] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting generation and member id due to: consumer pro-actively leaving the group (org.apache.kafka.clients.consumer.internals.ConsumerCoordinator)
[2023-04-18 18:20:08,492] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Request joining group due to: consumer pro-actively leaving the group (org.apache.kafka.clients.consumer.internals.ConsumerCoordinator)
[2023-04-18 18:20:08,493] INFO Metrics scheduler closed (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:20:08,493] INFO Closing reporter org.apache.kafka.common.metrics.JmxReporter (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:20:08,493] INFO Metrics reporters closed (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:20:08,498] INFO App info kafka.consumer for consumer-applicationResetSample-1 unregistered (org.apache.kafka.common.utils.AppInfoParser)
Done.
Deleting inferred internal topics [applicationResetSample-countWindowed-changelog]
Done.
[2023-04-18 18:20:08,499] INFO App info kafka.admin.client for adminclient-1 unregistered (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:20:08,503] INFO Metrics scheduler closed (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:20:08,504] INFO Closing reporter org.apache.kafka.common.metrics.JmxReporter (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:20:08,504] INFO Metrics reporters closed (org.apache.kafka.common.metrics.Metrics)
```

Now let's run it without the `--dry-run` option

```bash
docker exec -ti kafka-1 kafka-streams-application-reset --application-id applicationResetSample --bootstrap-servers kafka-1:19092 --input-topics test-kstream-input-topic --to-earliest 
```

and we can from the log that the reset has been done

```bash
[2023-04-18 18:22:12,504] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting offset for partition test-kstream-input-topic-7 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka-3:19094 (id: 3 rack: rack1)], epoch=0}}. (org.apache.kafka.clients.consumer.internals.SubscriptionState)
Topic: test-kstream-input-topic Partition: 1 Offset: 0
Topic: test-kstream-input-topic Partition: 2 Offset: 0
Topic: test-kstream-input-topic Partition: 3 Offset: 0
Topic: test-kstream-input-topic Partition: 4 Offset: 0
Topic: test-kstream-input-topic Partition: 5 Offset: 0
Topic: test-kstream-input-topic Partition: 6 Offset: 0
Topic: test-kstream-input-topic Partition: 7 Offset: 0
Topic: test-kstream-input-topic Partition: 0 Offset: 0
[2023-04-18 18:22:12,506] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Discovered group coordinator kafka-1:19092 (id: 2147483646 rack: null) (org.apache.kafka.clients.consumer.internals.ConsumerCoordinator)
[2023-04-18 18:22:12,518] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Resetting generation and member id due to: consumer pro-actively leaving the group (org.apache.kafka.clients.consumer.internals.ConsumerCoordinator)
[2023-04-18 18:22:12,518] INFO [Consumer clientId=consumer-applicationResetSample-1, groupId=applicationResetSample] Request joining group due to: consumer pro-actively leaving the group (org.apache.kafka.clients.consumer.internals.ConsumerCoordinator)
[2023-04-18 18:22:12,518] INFO Metrics scheduler closed (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:22:12,518] INFO Closing reporter org.apache.kafka.common.metrics.JmxReporter (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:22:12,519] INFO Metrics reporters closed (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:22:12,526] INFO App info kafka.consumer for consumer-applicationResetSample-1 unregistered (org.apache.kafka.common.utils.AppInfoParser)
Done.
Deleting inferred internal topics [applicationResetSample-countWindowed-changelog]
Done.
[2023-04-18 18:22:12,548] INFO App info kafka.admin.client for adminclient-1 unregistered (org.apache.kafka.common.utils.AppInfoParser)
[2023-04-18 18:22:12,557] INFO Metrics scheduler closed (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:22:12,558] INFO Closing reporter org.apache.kafka.common.metrics.JmxReporter (org.apache.kafka.common.metrics.Metrics)
[2023-04-18 18:22:12,558] INFO Metrics reporters closed (org.apache.kafka.common.metrics.Metrics)
```

we can also see that the internal (state changelog) topic has been deleted. 

let's restart the application to see that the data is reprocessed

```bash
mvn exec:java@producer -Dexec.args="--reset"
```

we use the `--reset` argument to force running the `cleanUp()` method before starting the Kafka Streams topology (causing the local state date to be deleted).