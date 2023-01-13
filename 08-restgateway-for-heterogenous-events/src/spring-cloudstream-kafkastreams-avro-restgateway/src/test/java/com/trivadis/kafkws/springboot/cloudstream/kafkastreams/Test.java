package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import com.trivadis.kafkaws.Alert;
import com.trivadis.kafkaws.Message;
import com.trivadis.kafkaws.Messages;
import com.trivadis.kafkaws.Notification;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Test {

    @org.junit.jupiter.api.Test
    public void testIt() {
        List<Object> value = Arrays.asList( Notification.newBuilder().setMessage("Hello").setId(1).build(),
                                            Alert.newBuilder().setMessage("Hello").setId(UUID.randomUUID().toString()).setSeverity("ERROR").setWhen(Instant.now()).build()
                                        );
        Messages msg = Messages.newBuilder().setItems(value).build();

        System.out.println(msg.toString());
    }
}

