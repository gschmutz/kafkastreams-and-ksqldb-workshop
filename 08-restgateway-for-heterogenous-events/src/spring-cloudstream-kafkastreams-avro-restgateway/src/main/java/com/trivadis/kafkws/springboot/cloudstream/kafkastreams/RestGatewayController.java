package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import com.trivadis.kafkaws.Messages;
import org.apache.avro.AvroTypeException;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import static com.vladkrava.converter.http.AbstractAvroHttpMessageConverter.AVRO_JSON;

@RestController
@RequestMapping("/gateway")
public class RestGatewayController {

    @Autowired
    private StreamBridge streamBridge;

    @PutMapping(produces = AVRO_JSON, consumes = AVRO_JSON)
    public ResponseEntity<Messages> handleJsonEmailData(@RequestBody final Messages data) {
        System.out.println("Data:" + data);
        System.out.println(data);

        for (Object item : data.getItems()) {
            streamBridge.send("send-binding-out-0", (SpecificRecord) item);
            //kafkaTemplate.send("test-kstream-spring-cloudstream-input-topic", (SpecificRecord) item);
        }

        return ResponseEntity.ok(data);
    }

    @ExceptionHandler(value = AvroTypeException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ErrorResponse> handleAvroTypeException(AvroTypeException avroTypeException, WebRequest request) {
        System.out.println("Exception handled: " + avroTypeException);
        return null;
    }

}
